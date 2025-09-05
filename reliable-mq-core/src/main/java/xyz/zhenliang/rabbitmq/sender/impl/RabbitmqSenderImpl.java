package xyz.zhenliang.rabbitmq.sender.impl;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import xyz.zhenliang.rabbitmq.constant.RabbitMQConstant;
import xyz.zhenliang.rabbitmq.context.CorrelationDataContext;
import xyz.zhenliang.rabbitmq.dto.RabbitmqMsgDTO;
import xyz.zhenliang.rabbitmq.entity.RabbitmqMessage;
import xyz.zhenliang.rabbitmq.exception.TechException;
import xyz.zhenliang.rabbitmq.sender.IRabbitmqSender;
import xyz.zhenliang.rabbitmq.service.IRabbitmqMessageService;
import xyz.zhenliang.rabbitmq.service.impl.RabbitmqReminderService;
import xyz.zhenliang.rabbitmq.utils.JsonUtils;
import xyz.zhenliang.rabbitmq.utils.RabbitmqUtils;
import xyz.zhenliang.rabbitmq.utils.SpringUtils;
import xyz.zhenliang.rabbitmq.utils.UUIDUtils;

/**
 * RabbitMQ消息发送器实现类
 * 提供可靠的消息发送机制，确保消息在发送前持久化到数据库，并在事务提交后发送消息
 */
@Service
public class RabbitmqSenderImpl implements IRabbitmqSender {
    private static final Logger log = LoggerFactory.getLogger(RabbitmqSenderImpl.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private IRabbitmqMessageService rabbitmqMessageService;
    @Autowired
    private RabbitmqSenderRetryListener retryListener;

    @Autowired
    private RabbitmqReminderService reminderService;

    @Value("${mq.id-prefix:}")
    private String idPrefix;

    private static final String SEND_SUCCESS = "Message sent successfully to Exchange: {}";
    private static final String SEND_FAILED = "Failed to send message to Exchange: {}, cause: {}";

    /**
     * 初始化RabbitTemplate回调函数
     * 设置消息发送确认回调和消息返回回调
     */
    @PostConstruct
    public void init() {
        // 设置消息发送确认回调，处理消息发送到Broker的成功/失败情况
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (correlationData != null) {
                String mid = correlationData.getId();
                if (mid == null) return;
                if (ack) {
                    log.info(SEND_SUCCESS, mid);
                    // 更新消息发送确认状态为成功
                    rabbitmqMessageService.updateSendConfirmSuccessById(mid);
                } else {
                    log.error(SEND_FAILED, mid, cause);
                    // 更新消息发送确认状态为失败
                    rabbitmqMessageService.updateSendConfirmFailById(mid, cause);
                    reminderService.sendFailedReminder(mid, cause);
                }
            }
        });
        // 设置消息返回回调，处理消息无法路由到队列的情况
        rabbitTemplate.setReturnsCallback(returned -> {
            returned.getMessage().getMessageProperties().getCorrelationId();
            if (returned.getMessage().getMessageProperties() != null) {
                String mid = returned.getMessage().getMessageProperties().getCorrelationId();
                if (mid != null) {
                    // 更新消息发送确认状态为失败，原因为无法路由到队列
                    rabbitmqMessageService.updateSendConfirmFailById(mid, "Message routing to queue failed.No suitable queue can be routed to.");
                    reminderService.sendFailedReminder(mid, "Message routing to queue failed.No suitable queue can be routed to.");
                }
            }
            log.error("Message routing to queue failed.No suitable queue can be routed to. \nReply code: {}, Reason: {}, Exchange: {}, Routing key: {}, Message: {}",
                    returned.getReplyCode(),
                    returned.getReplyText(),
                    returned.getExchange(),
                    returned.getRoutingKey(),
                    RabbitmqUtils.toString(returned.getMessage().getBody()));
        });

    }

    /**
     * 发送消息接口实现
     * 该方法在事务中执行，确保消息先持久化到数据库，再在事务提交后发送到MQ
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param data       消息数据
     * @param businessId 业务ID
     * @param extraInfo  额外信息
     * @param <T>        消息数据类型
     * @return 消息DTO对象
     */
    @Transactional
    @Override
    public <T> RabbitmqMsgDTO<T> sendMessage(String exchange, String routingKey, T data, String businessId, String extraInfo) {
        String messageId = idPrefix+UUIDUtils.generate32UUID();
        RabbitmqMsgDTO<T> msgDTO = new RabbitmqMsgDTO<>(messageId, exchange, routingKey, data, businessId, extraInfo);
        try {
            // 1.新增消息数据到数据库，确保消息持久化
            rabbitmqMessageService.createMessageBySender(messageId, msgDTO.getBusinessId(), exchange, routingKey
                    , JsonUtils.toJson(msgDTO));
            // 2.注册事务同步，在事务提交后发送消息
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // 在事务提交后发送消息，确保数据库操作成功后再发送消息
                    send(exchange, routingKey, msgDTO);
                }
            });

            return msgDTO;
        } catch (Exception e) {
            handleException(messageId, exchange, routingKey, msgDTO, "Failed to send message.", e);
        }
        return null;
    }

    /**
     * 发送消息方法
     * 通过Spring容器获取当前Bean实例并调用异步发送方法
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param msgDTO     消息DTO对象
     * @param <T>        消息数据类型
     */
    public <T> void send(String exchange, String routingKey, RabbitmqMsgDTO<T> msgDTO) {
        String messageId = msgDTO.getMessageId();
        RabbitmqSenderImpl sender = SpringUtils.getBean(RabbitmqSenderImpl.class);
        // 异步发送消息
        sender.send(exchange, routingKey, messageId, msgDTO.getBusinessId(), JsonUtils.toJson(msgDTO));
    }

    /**
     * 异步发送消息方法
     * 构造AMQP消息并发送到指定的交换机和路由键
     *
     * @param exchange    交换机名称
     * @param routingKey  路由键
     * @param messageId   消息ID
     * @param businessId  业务ID
     * @param messageBody 消息体内容
     */
    @Async
    public void send(String exchange, String routingKey, String messageId, String businessId, String messageBody) {
        // 1.设置消息属性，确保消息持久化
        MessageProperties properties = new MessageProperties();
        properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT); // 消息持久化
        properties.setMessageId(messageId); // 设置消息ID
        properties.setCorrelationId(messageId); // 设置关联ID用于确认机制
        if (businessId != null)
            properties.setHeader(RabbitMQConstant.BUSINESS_ID, businessId); // 设置业务ID头部信息

        Message amqpMessage = MessageBuilder.withBody(
                        RabbitmqUtils.toByteArray(messageBody))
                .andProperties(properties)
                .build();

        CorrelationData correlationData = new CorrelationData(messageId);
        CorrelationDataContext.set(correlationData);

        // 2.消息发送
        rabbitTemplate.send(exchange, routingKey, amqpMessage, correlationData);
    }

    /**
     * 异常处理方法
     * 记录异常日志并抛出技术异常
     *
     * @param messageId  消息ID
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param msgDTO     消息DTO对象
     * @param message    错误信息
     * @param e          异常对象
     * @param <T>        消息数据类型
     */
    private static <T> void handleException(String messageId, String exchange, String routingKey, RabbitmqMsgDTO<T> msgDTO, String message, Exception e) {
        log.error("{}\nexchange: {}\nroutingKey: {}\ndata: {}\n{}",
                message, exchange, routingKey, JsonUtils.toJson(msgDTO), e.getMessage(), e);
        throw new TechException(message, e);
    }

    /**
     * 重新发送消息接口实现
     * 将消息状态重置为未消费状态，并在事务提交后重新发送消息
     *
     * @param messageId 消息ID
     */
    @Transactional
    @Override
    public void resendMsg(String messageId) {
        RabbitmqMessage msg = rabbitmqMessageService.getById(messageId);
        //将消息改为未消费
        rabbitmqMessageService.resendMsg(messageId);
        // 2.注册事务同步，在事务提交后发送消息
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // 在事务提交后发送消息，确保数据库操作成功后再同步发送消息
                send(msg.getExchange(), msg.getRoutingKey(), messageId, msg.getBusinessId(), msg.getMessageBody());
            }
        });
    }
}