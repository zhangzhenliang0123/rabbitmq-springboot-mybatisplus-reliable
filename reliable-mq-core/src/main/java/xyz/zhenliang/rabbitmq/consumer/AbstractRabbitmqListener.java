package xyz.zhenliang.rabbitmq.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import xyz.zhenliang.rabbitmq.dto.RabbitmqMsgDTO;
import xyz.zhenliang.rabbitmq.exception.TechException;
import xyz.zhenliang.rabbitmq.rescue.retry.RabbitmqRetryManager;
import xyz.zhenliang.rabbitmq.service.IRabbitmqMessageService;
import xyz.zhenliang.rabbitmq.service.impl.RabbitmqReminderService;
import xyz.zhenliang.rabbitmq.utils.JsonUtils;
import xyz.zhenliang.rabbitmq.utils.MqSpringUtils;
import xyz.zhenliang.rabbitmq.utils.RabbitmqUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * RabbitMQ消息监听器抽象类
 * 提供消息消费的基础实现，包括消息确认、异常处理等通用逻辑
 *
 * @param <T> 消息数据的泛型类型
 */
public abstract class AbstractRabbitmqListener<T> {
    protected static final Logger log = LoggerFactory.getLogger(AbstractRabbitmqListener.class);

    /**
     * 用于反序列化消息的TypeReference实例
     */
    protected TypeReference<RabbitmqMsgDTO<T>> typeReference;

    /**
     * 消息数据的类型Class对象
     */
    protected Class<T> messageType;

    /**
     * 获取消息数据类型Class对象
     *
     * @return 消息数据类型Class对象
     */
    protected Class<T> getMessageType() {
        return messageType;
    }

    /**
     * 获取消息反序列化TypeReference对象
     *
     * @return TypeReference对象
     */
    protected TypeReference<RabbitmqMsgDTO<T>> getTypeReference() {
        return typeReference;
    }

    @SuppressWarnings("unchecked")
    public AbstractRabbitmqListener() {
        // 通过反射获取泛型类型参数
        Type genericSuperclass = getClass().getGenericSuperclass();

        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
            Type[] typeArguments = parameterizedType.getActualTypeArguments();

            if (typeArguments.length > 0 && typeArguments[0] instanceof Class) {
                this.messageType = (Class<T>) typeArguments[0];
            }
        }

        // 如果无法通过反射获取类型，则抛出异常
        if (this.messageType == null) {
            throw new IllegalStateException("无法确定泛型类型参数，请确保子类正确指定了泛型类型");
        }
        this.typeReference = new TypeReference<RabbitmqMsgDTO<T>>() {
            @Override
            public Type getType() {
                return new ParameterizedType() {
                    @Override
                    public Type[] getActualTypeArguments() {
                        return new Type[]{getMessageType()};
                    }

                    @Override
                    public Type getRawType() {
                        return RabbitmqMsgDTO.class;
                    }

                    @Override
                    public Type getOwnerType() {
                        return null;
                    }
                };
            }
        };
    }

    /**
     * 消息消费方法
     * 处理RabbitMQ消息，包括消息解析、重复检查、业务处理和状态更新
     *
     * @param message RabbitMQ消息对象
     * @param channel RabbitMQ通道对象，用于手动确认消息
     */
    public void consume(Message message, Channel channel) {

        // 获取重试管理器实例
        RabbitmqRetryManager retryManager = MqSpringUtils.getBean(RabbitmqRetryManager.class);

        // 获取消息的deliveryTag，用于消息确认
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        String messageId = null;
        // 获取消息服务实例
        IRabbitmqMessageService rabbitmqMessageService = MqSpringUtils.getBean(IRabbitmqMessageService.class);
        RabbitmqMsgDTO<T> msgDTO = null;
        String messageBody = null;

        // 判断是否为最后一次重试
        boolean retryLastFlag = retryManager.isLastConsume();

        try {
            // 解析消息内容为RabbitmqMsgDTO对象
            msgDTO = RabbitmqUtils.getMessageData(message, this.getTypeReference());
            messageId = msgDTO.getMessageId();
            messageBody = JsonUtils.toJson(msgDTO);
            log.debug("Starting to process message, messageId: {}", messageId);

            // 检查消息是否已消费，避免重复处理
            if (rabbitmqMessageService.checkAndLockMessageConsume(msgDTO)) {
                log.debug("Message already consumed, directly confirm, messageId: {}", messageId);
                channel.basicAck(deliveryTag, false);
                return;
            }
        } catch (Exception e) {
            // 数据库异常，导致消息没有被保存到数据库中，则直接拒绝消息
            log.error("Failed to save consume info, messageId: {}", messageId, e);
            // 如果是最后一次重试，则放死信队列
            if (retryLastFlag) {
                this.reject(channel, deliveryTag, messageId);
                this.consumeFailedReminder(messageId, messageBody, e);
            } else {
                throw new TechException(e.getMessage(), e);
            }
        }

        try {
            // 调用消息处理器处理消息
            this.handleData(msgDTO);

            // 更新消息消费状态为成功
            rabbitmqMessageService.updateConsumeSuccessById(messageId);
            log.debug("Message processed successfully, messageId: {}", messageId);

        } catch (Exception e) {
            log.error("Failed to process message, messageId: {}", messageId, e);
            // 更新消息消费状态为失败
            if (messageId != null) rabbitmqMessageService.updateConsumeFailById(messageId, e.getMessage());
            if (retryLastFlag) {
                this.consumeFailedReminder(messageId, messageBody, e);
            } else {
                throw new TechException(e.getMessage(), e);
            }
        } finally {
            // 手动确认消息已消费
            if (retryLastFlag) ack(channel, deliveryTag, messageId);
        }
    }

    /**
     * 确认消息已消费
     *
     * @param channel     RabbitMQ通道对象
     * @param deliveryTag 消息的deliveryTag
     * @param messageId   消息ID
     */
    public void ack(Channel channel, long deliveryTag, String messageId) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to ack message, messageId: {}", messageId, e);
        }
    }

    public void consumeFailedReminder(String messageId, String messageBody, Exception e) {
        RabbitmqReminderService rabbitmqReminder = MqSpringUtils.getBean(RabbitmqReminderService.class);
        rabbitmqReminder.consumeFailedReminder(messageId, messageBody, e);
    }

    /**
     * 拒绝消息消费
     *
     * @param channel     RabbitMQ通道对象
     * @param deliveryTag 消息的deliveryTag
     * @param messageId   消息ID
     */
    public void reject(Channel channel, long deliveryTag, String messageId) {
        try {
            channel.basicReject(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to reject message, messageId: {}", messageId, e);
        }
    }

    /**
     * 处理消息DTO数据的钩子方法
     * 子类可以重写此方法来实现具体的业务逻辑
     *
     * @param msgDTO 消息DTO对象，包含消息ID、业务数据等信息
     */
    public void handleData(RabbitmqMsgDTO<T> msgDTO) {

    }

}