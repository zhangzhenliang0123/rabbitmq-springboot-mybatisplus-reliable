package xyz.zhenliang.rabbitmq.rescue.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.zhenliang.rabbitmq.constant.RabbitmqConsumeStatus;
import xyz.zhenliang.rabbitmq.constant.RabbitmqSaveBy;
import xyz.zhenliang.rabbitmq.constant.RabbitmqSendStatus;
import xyz.zhenliang.rabbitmq.dto.QueueProcessResult;
import xyz.zhenliang.rabbitmq.dto.RabbitmqMsgDTO;
import xyz.zhenliang.rabbitmq.entity.RabbitmqMessage;
import xyz.zhenliang.rabbitmq.rescue.IRabbitmqRescue;
import xyz.zhenliang.rabbitmq.sender.IRabbitmqSender;
import xyz.zhenliang.rabbitmq.sender.impl.RabbitmqSenderImpl;
import xyz.zhenliang.rabbitmq.service.IRabbitmqMessageService;
import org.springframework.amqp.core.MessageProperties;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import xyz.zhenliang.rabbitmq.utils.JsonUtils;
import xyz.zhenliang.rabbitmq.utils.RabbitmqUtils;

/**
 * RabbitMQ消息救援服务实现类
 * 提供消息重发、死信队列处理、消息统计等功能
 */
@Service
public class RabbitmqRescueImpl implements IRabbitmqRescue {
    private static final Logger log = LoggerFactory.getLogger(RabbitmqRescueImpl.class);
    @Autowired
    private AmqpAdmin amqpAdmin;
    @Autowired
    private IRabbitmqSender rabbitmqSender;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private IRabbitmqMessageService rabbitmqMessageService;
    private String encoding = "UTF-8";

    private MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();

    /**
     * 重新发送消息
     *
     * @param messageId 消息ID
     */
    @Override
    public void resendMessage(String messageId) {
        rabbitmqSender.resendMsg(messageId);
    }

    /**
     * 删除消息
     *
     * @param messageId 消息ID
     */
    @Override
    public void deleteMessage(String messageId) {
        rabbitmqMessageService.removeById(messageId);
    }

    /**
     * 获取死信队列中的消息数量
     *
     * @param queueName 队列名称
     * @return 消息数量，-1表示队列不存在或无法访问
     */
    @Override
    public long getDeadLetterCount(String queueName) {
        Properties queueProperties = amqpAdmin.getQueueProperties(queueName);
        if (queueProperties != null) {
            // 关键属性：QUEUE_MESSAGE_COUNT
            Object messageCount = queueProperties.get("QUEUE_MESSAGE_COUNT");
            return (messageCount != null) ? (Integer) messageCount : 0;
        } else {
            // 队列不存在或无法访问
            return -1;
        }
    }

    /**
     * 将队列中的消息移动到消息表中
     *
     * @param deadLetterQueueName 死信队列名称
     * @return 队列处理结果
     */
    @Override
    public QueueProcessResult moveQueueMessagesToMsgTable(String deadLetterQueueName) {
        log.info("Start processing dead letter queue: {}", deadLetterQueueName);
        final QueueProcessResult result = new QueueProcessResult();
        rabbitTemplate.execute(channel -> {

            GetResponse response;
            while ((response = channel.basicGet(deadLetterQueueName, false)) != null) {
                result.setProcessedCount(result.getProcessedCount() + 1);
                String body = null;
                String messageId = null;
                long deliveryTag = response.getEnvelope().getDeliveryTag();
                try {
                    body = RabbitmqUtils.toString(response.getBody());
                    Message message = buildMessageFromResponse(response);
                    messageId = message.getMessageProperties().getMessageId();

                    log.info("Processing dead letter message.\nmessageId:{}\nbody:{}", messageId, body);

                    // 处理消息
                    boolean success = rabbitmqMessageService.processDeadLetterMessage(messageId, body);

                    if (success) {
                        // 确认消息
                        result.setSuccessCount(result.getSuccessCount() + 1);

                        log.info("Message processed successfully.\nmessageId:{}\nbody:{}", messageId, body);
                    } else {
                        result.setFailureCount(result.getFailureCount() + 1);
                        if (messageId != null) result.getUnprocessableMessageIds().add(messageId);
                        log.error("Message processing failed.\nmessageId:{}\nbody:{}", messageId, body);
                    }
                } catch (Exception e) {
                    log.error("Exception occurred while processing message. \nmessageId:{}\nbody:{}", messageId, body, e);
                    if (messageId != null) result.getUnprocessableMessageIds().add(messageId);
                    result.setFailureCount(result.getFailureCount() + 1);
                } finally {
                    // 所有消息全部消费
                    channel.basicAck(deliveryTag, false);
                }
            }
            log.error("Dead letter queue processing completed: {}", JsonUtils.toJson(result));
            return null;
        });
        return result;
    }


    /**
     * 根据RabbitMQ的响应构建Message对象
     *
     * @param response RabbitMQ的GetResponse响应
     * @return 构建的Message对象
     */
    private Message buildMessageFromResponse(GetResponse response) {
        return this.buildMessage(response.getEnvelope(), response.getProps(), response.getBody(), response.getMessageCount());
    }

    /**
     * 根据RabbitMQ的响应参数构建Message对象
     *
     * @param envelope   消息的信封信息，包含交换机、路由键等
     * @param properties 消息属性
     * @param body       消息体
     * @param msgCount   消息计数
     * @return 构建的Message对象
     */
    private Message buildMessage(Envelope envelope, AMQP.BasicProperties properties, byte[] body, int msgCount) {

        MessageProperties messageProps = this.messagePropertiesConverter.toMessageProperties(properties, envelope, this.encoding);
        if (msgCount >= 0) {
            messageProps.setMessageCount(msgCount);
        }

        Message message = new Message(body, messageProps);

        return message;
    }

    /**
     * 清空队列中的所有消息
     *
     * @param queueName 队列名称
     */
    @Override
    public void purgeQueue(String queueName) {
        amqpAdmin.purgeQueue(queueName);
    }

    /**
     * 获取指定时间范围内发送失败的消息数量
     *
     * @param startSecondsAgo 起始时间(秒前)
     * @param endSecondsAgo   结束时间(秒前)
     * @return 发送失败的消息数量
     */
    @Override
    public long getSendFailedCount(long startSecondsAgo, long endSecondsAgo) {
        LocalDateTime endTime = LocalDateTime.now().minusSeconds(startSecondsAgo);
        LocalDateTime startTime = LocalDateTime.now().minusSeconds(endSecondsAgo);

        LambdaUpdateWrapper<RabbitmqMessage> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.ge(RabbitmqMessage::getCreatedAt, startTime);
        queryWrapper.le(RabbitmqMessage::getCreatedAt, endTime);
        queryWrapper.ne(RabbitmqMessage::getSendStatus, RabbitmqSendStatus.SUCCESS.getValue());
        queryWrapper.eq(RabbitmqMessage::getSavedBy, RabbitmqSaveBy.SENDER_SAVE.getValue());
        return rabbitmqMessageService.count(queryWrapper);
    }

    /**
     * 获取指定时间范围内发送失败的消息分页数据
     *
     * @param page            分页对象
     * @param startSecondsAgo 起始时间(秒前)
     * @param endSecondsAgo   结束时间(秒前)
     * @return 发送失败的消息分页数据
     */
    @Override
    public Page<RabbitmqMessage> getSendFailedPage(Page<RabbitmqMessage> page, long startSecondsAgo, long endSecondsAgo) {
        LocalDateTime endTime = LocalDateTime.now().minusSeconds(startSecondsAgo);
        LocalDateTime startTime = LocalDateTime.now().minusSeconds(endSecondsAgo);

        LambdaQueryWrapper<RabbitmqMessage> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.ge(RabbitmqMessage::getCreatedAt, startTime);
        queryWrapper.le(RabbitmqMessage::getCreatedAt, endTime);
        queryWrapper.ne(RabbitmqMessage::getSendStatus, RabbitmqSendStatus.SUCCESS.getValue());
        queryWrapper.eq(RabbitmqMessage::getSavedBy, RabbitmqSaveBy.SENDER_SAVE.getValue());

        queryWrapper.orderByDesc(RabbitmqMessage::getCreatedAt);

        return rabbitmqMessageService.page(page, queryWrapper);
    }

    /**
     * 获取指定时间范围内消费失败的消息数量
     *
     * @param startSecondsAgo 起始时间(秒前)
     * @param endSecondsAgo   结束时间(秒前)
     * @return 消费失败的消息数量
     */
    @Override
    public long getConsumeFailedCount(long startSecondsAgo, long endSecondsAgo) {
        LocalDateTime endTime = LocalDateTime.now().minusSeconds(startSecondsAgo);
        LocalDateTime startTime = LocalDateTime.now().minusSeconds(endSecondsAgo);

        LambdaUpdateWrapper<RabbitmqMessage> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.ge(RabbitmqMessage::getCreatedAt, startTime);
        queryWrapper.le(RabbitmqMessage::getCreatedAt, endTime);
        queryWrapper.ne(RabbitmqMessage::getConsumeStatus, RabbitmqConsumeStatus.SUCCESS.getValue());
        return rabbitmqMessageService.count(queryWrapper);
    }

    /**
     * 获取指定时间范围内消费失败的消息分页数据
     *
     * @param page            分页对象
     * @param startSecondsAgo 起始时间(秒前)
     * @param endSecondsAgo   结束时间(秒前)
     * @return 消费失败的消息分页数据
     */
    @Override
    public Page<RabbitmqMessage> getConsumeFailedPage(Page<RabbitmqMessage> page, long startSecondsAgo, long endSecondsAgo) {
        LocalDateTime endTime = LocalDateTime.now().minusSeconds(startSecondsAgo);
        LocalDateTime startTime = LocalDateTime.now().minusSeconds(endSecondsAgo);

        LambdaQueryWrapper<RabbitmqMessage> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.ge(RabbitmqMessage::getCreatedAt, startTime);
        queryWrapper.le(RabbitmqMessage::getCreatedAt, endTime);
        queryWrapper.ne(RabbitmqMessage::getConsumeStatus, RabbitmqConsumeStatus.SUCCESS.getValue());

        queryWrapper.orderByDesc(RabbitmqMessage::getCreatedAt);

        return rabbitmqMessageService.page(page, queryWrapper);
    }

    /**
     * 根据消息ID获取消息
     *
     * @param messageId 消息ID
     * @return 消息实体对象
     */
    public RabbitmqMessage getMessageById(String messageId) {
        return rabbitmqMessageService.getById(messageId);
    }
}