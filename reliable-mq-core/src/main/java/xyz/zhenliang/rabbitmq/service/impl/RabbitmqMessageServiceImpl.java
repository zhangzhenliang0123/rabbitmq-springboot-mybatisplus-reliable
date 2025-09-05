package xyz.zhenliang.rabbitmq.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import xyz.zhenliang.rabbitmq.constant.RabbitmqConsumeStatus;
import xyz.zhenliang.rabbitmq.constant.RabbitmqSaveBy;
import xyz.zhenliang.rabbitmq.constant.RabbitmqSendStatus;
import xyz.zhenliang.rabbitmq.dto.RabbitmqMessageMeta;
import xyz.zhenliang.rabbitmq.dto.RabbitmqMsgDTO;
import xyz.zhenliang.rabbitmq.entity.RabbitmqMessage;
import xyz.zhenliang.rabbitmq.mapper.RabbitmqMessageMapper;
import xyz.zhenliang.rabbitmq.service.IRabbitmqMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.zhenliang.rabbitmq.utils.JsonUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * <p>
 * RabbitMQ消息表服务实现类
 * 实现消息的全生命周期管理，确保消息的可靠传递和状态跟踪
 * </p>
 *
 * @author zzl
 * @since 2025-09-06
 */
@Service
public class RabbitmqMessageServiceImpl extends ServiceImpl<RabbitmqMessageMapper, RabbitmqMessage> implements IRabbitmqMessageService {

    private static final Logger log = LoggerFactory.getLogger(RabbitmqMessageServiceImpl.class);

    /**
     * 消息消费超时时间（毫秒），默认3000ms
     * 可通过配置项mq.maxConsumeTimeoutMillis进行自定义
     */
    @Value("${mq.maxConsumeTimeoutMillis:3000}")
    private int maxConsumeTimeoutMillis = 3000;

    /**
     * 创建消息记录
     * 在消息发送前创建消息记录，初始化消息状态为发送中
     *
     * @param messageId   消息唯一标识
     * @param businessId  业务ID
     * @param exchange    交换机名称
     * @param routingKey  路由键
     * @param messageBody 消息体内容
     * @return 消息实体对象
     */
    @Transactional
    @Override
    public RabbitmqMessage createMessageBySender(String messageId, String businessId, String exchange, String routingKey, String messageBody) {
        RabbitmqMessage message = new RabbitmqMessage();
        message.setId(messageId);
        message.setBusinessId(businessId);
        message.setExchange(exchange);
        message.setRoutingKey(routingKey);
        message.setMessageBody(messageBody);
        message.setSendStatus(RabbitmqSendStatus.SENDING.getValue()); // 表示未发送或发送中
        message.setSendCount(0);
        message.setConsumeStatus(RabbitmqConsumeStatus.UNCONSUMED.getValue());
        message.setSendLastTime(LocalDateTime.now());
        message.setSavedBy(RabbitmqSaveBy.SENDER_SAVE.getValue());
        message.setCreatedAt(LocalDateTime.now());

        this.save(message);
        return message;
    }

    /**
     * 消费端创建消息记录
     * 当消费者接收到消息时创建消息记录，初始化消息状态为消费中
     *
     * @param messageId   消息唯一标识
     * @param businessId  业务ID
     * @param exchange    交换机名称
     * @param routingKey  路由键
     * @param messageBody 消息体内容
     * @return 消息实体对象
     */
    public RabbitmqMessage createMessageByConsumer(String messageId, String businessId, String exchange, String routingKey, String messageBody) {
        RabbitmqMessage message = new RabbitmqMessage();
        message.setId(messageId);
        message.setBusinessId(businessId);
        message.setExchange(exchange);
        message.setRoutingKey(routingKey);
        message.setMessageBody(messageBody);
        message.setSendStatus(RabbitmqSendStatus.SUCCESS.getValue()); // 消费端创建的消息默认为已发送成功
        message.setSendCount(0);
        message.setConsumeStatus(RabbitmqConsumeStatus.CONSUMING.getValue()); // 设置为消费中状态
        message.setConsumeCount(1);
        message.setConsumeLastTime(LocalDateTime.now());
        message.setSavedBy(RabbitmqSaveBy.CONSUMER_SAVE.getValue()); // 标识为消费者保存
        message.setCreatedAt(LocalDateTime.now());

        this.save(message);
        return message;
    }

    /**
     * 更新消息发送成功状态
     * 增加发送次数并更新最后发送时间
     *
     * @param messageId 消息ID
     * @param sendCount 发送次数增量
     * @return 是否更新成功
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public boolean updateSendSuccessById(String messageId, int sendCount) {
        LambdaUpdateWrapper<RabbitmqMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(RabbitmqMessage::getId, messageId);
        // updateWrapper.set(RabbitmqMessage::getSendStatus, RabbitmqSendStatus.SENDING.getValue());
        updateWrapper.setSql("send_count=send_count+" + sendCount);
        updateWrapper.set(RabbitmqMessage::getSendLastTime, LocalDateTime.now());
        return this.update(updateWrapper);
    }

    /**
     * 更新消息发送失败状态
     * 设置发送状态为失败，增加发送次数，更新最后发送时间和错误信息
     *
     * @param messageId    消息ID
     * @param sendCount    发送次数增量
     * @param errorMessage 错误信息
     * @return 是否更新成功
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public boolean updateSendFailById(String messageId, int sendCount, String errorMessage) {
        LambdaUpdateWrapper<RabbitmqMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(RabbitmqMessage::getId, messageId);
        updateWrapper.set(RabbitmqMessage::getSendStatus, RabbitmqSendStatus.FAILED.getValue());
        updateWrapper.setSql("send_count=send_count+" + sendCount);
        updateWrapper.set(RabbitmqMessage::getSendLastTime, LocalDateTime.now());
        updateWrapper.set(RabbitmqMessage::getSendErrorMessage, errorMessage);
        return this.update(updateWrapper);
    }

    /**
     * 设置消息发送成功
     * 更新消息发送状态为成功，并记录确认时间
     *
     * @param messageId 消息ID
     * @return 是否更新成功
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public boolean updateSendConfirmSuccessById(String messageId) {
        LambdaUpdateWrapper<RabbitmqMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(RabbitmqMessage::getId, messageId);
        updateWrapper.set(RabbitmqMessage::getSendStatus, RabbitmqSendStatus.SUCCESS.getValue());
        updateWrapper.set(RabbitmqMessage::getConfirmLastTime, LocalDateTime.now());
        updateWrapper.set(RabbitmqMessage::getSendErrorMessage, null);

        return this.update(updateWrapper);
    }

    /**
     * 设置消息发送失败
     * 更新消息发送状态为失败，记录确认时间和失败原因
     *
     * @param messageId    消息ID
     * @param errorMessage 失败原因
     * @return 是否更新成功
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public boolean updateSendConfirmFailById(String messageId, String errorMessage) {
        LambdaUpdateWrapper<RabbitmqMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(RabbitmqMessage::getId, messageId);
        updateWrapper.set(RabbitmqMessage::getSendStatus, RabbitmqSendStatus.FAILED.getValue());
        updateWrapper.set(RabbitmqMessage::getConfirmLastTime, LocalDateTime.now());
        updateWrapper.set(RabbitmqMessage::getSendErrorMessage, errorMessage);
        return this.update(updateWrapper);
    }

    /**
     * 重新发送消息
     * 将消息状态重置为待发送状态，发送次数加1，并重置消费状态为未消费
     *
     * @param messageId 消息ID
     * @return 是否更新成功
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public boolean resendMsg(String messageId) {
        LambdaUpdateWrapper<RabbitmqMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(RabbitmqMessage::getId, messageId);
        updateWrapper.set(RabbitmqMessage::getConsumeStatus, RabbitmqConsumeStatus.UNCONSUMED.getValue());
        return this.update(updateWrapper);
    }

    /**
     * 设置消息消费成功
     * 更新消息消费状态为成功，并记录消费成功时间
     *
     * @param messageId 消息ID
     * @return 是否更新成功
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public boolean updateConsumeSuccessById(String messageId) {
        LambdaUpdateWrapper<RabbitmqMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(RabbitmqMessage::getId, messageId);
        updateWrapper.set(RabbitmqMessage::getConsumeStatus, RabbitmqConsumeStatus.SUCCESS.getValue());
        updateWrapper.set(RabbitmqMessage::getConsumeSuccessTime, LocalDateTime.now());
        updateWrapper.set(RabbitmqMessage::getConsumeErrorMessage, null);
        return this.update(updateWrapper);
    }

    /**
     * 设置消息消费失败
     * 更新消息消费状态为失败，并记录失败原因
     *
     * @param messageId    消息ID
     * @param errorMessage 失败原因
     * @return 是否更新成功
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public boolean updateConsumeFailById(String messageId, String errorMessage) {
        LambdaUpdateWrapper<RabbitmqMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(RabbitmqMessage::getId, messageId);
        updateWrapper.set(RabbitmqMessage::getConsumeStatus, RabbitmqConsumeStatus.FAILED.getValue());
        updateWrapper.set(RabbitmqMessage::getConsumeErrorMessage, errorMessage);
        return this.update(updateWrapper);
    }

    /**
     * 处理死信消息
     * 检查消息是否存在，如果不存在则从消息体中提取元数据创建新记录
     *
     * @param messageId   消息ID
     * @param messageBody 消息体内容
     * @return 是否处理成功
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public boolean processDeadLetterMessage(String messageId, String messageBody) {
        // 检查消息是否已存在
        LambdaUpdateWrapper<RabbitmqMessage> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.eq(RabbitmqMessage::getId, messageId);
        if (this.count(queryWrapper) > 0) {
            // 消息已存在，直接返回
            return true;
        }

        // 消息不存在，从messageBody中提取属性
        RabbitmqMessageMeta messageMeta = JsonUtils.fromJson(messageBody, RabbitmqMessageMeta.class);

        // 创建死信消息记录
        RabbitmqMessage message = new RabbitmqMessage();
        message.setId(messageId);
        message.setBusinessId(messageMeta.getBusinessId());
        message.setExchange(messageMeta.getExchange());
        message.setRoutingKey(messageMeta.getRoutingKey());
        message.setMessageBody(messageBody);
        message.setSendStatus(RabbitmqSendStatus.SUCCESS.getValue()); // 消费端创建的消息默认为已发送成功
        message.setSendCount(0);
        message.setConsumeStatus(RabbitmqConsumeStatus.FAILED.getValue()); // 设置为消费失败
        message.setConsumeCount(1);
        message.setConsumeLastTime(LocalDateTime.now());
        message.setSavedBy(RabbitmqSaveBy.DEAD_LETTER_SAVE.getValue()); // 标识为死信处理者保存
        message.setCreatedAt(LocalDateTime.now());

        return this.save(message);
    }

    /**
     * 检查并锁定消息消费状态
     * 用于实现消息消费的幂等性控制，防止重复消费
     * 1. 消息不存在，返回true表示不可消费
     * 2. 消息已成功消费，返回true表示不可重复消费
     * 3. 消息正在消费且未超时，返回true表示不可重复消费
     * 4. 其他情况，将消息状态设置为消费中，返回false表示可消费
     *
     * @param msgDTO 消息DTO对象，包含消息ID等信息
     * @return true-消息已消费或正在消费未超时，false-消息可被消费
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public <T> boolean checkAndLockMessageConsume(RabbitmqMsgDTO<T> msgDTO) {
        String messageId = msgDTO.getMessageId();
        RabbitmqMessage message = this.getById(messageId);
        if (message == null) {
            //消息不存在,新增
            this.createMessageByConsumer(messageId, msgDTO.getBusinessId()
                    , msgDTO.getExchange(), msgDTO.getRoutingKey(), JsonUtils.toJson(msgDTO));
            return false;
        } else {
            if (message.getConsumeStatus() == RabbitmqConsumeStatus.SUCCESS.getValue()) {
                // 消息已成功消费
                return true;
            } else {
                if (message.getConsumeStatus() == RabbitmqConsumeStatus.CONSUMING.getValue()) {
                    if (message.getConsumeLastTime() == null
                            || message.getConsumeLastTime().plus(maxConsumeTimeoutMillis, ChronoUnit.MILLIS).isBefore(LocalDateTime.now())) {
                        //消息已超时，情况非常少
                        log.debug("消息消费已超时，messageId: {}", messageId);
                    } else {
                        // 未超时，返回消费成功
                        return true;
                    }
                }
                //设置消息为消费中
                LambdaUpdateWrapper<RabbitmqMessage> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(RabbitmqMessage::getId, messageId);
                updateWrapper.set(RabbitmqMessage::getConsumeStatus, RabbitmqConsumeStatus.CONSUMING.getValue());
                updateWrapper.setSql("consume_count=consume_count+1");
                updateWrapper.set(RabbitmqMessage::getConsumeLastTime, LocalDateTime.now());

                this.update(updateWrapper);
                return false;
            }
        }

    }
}