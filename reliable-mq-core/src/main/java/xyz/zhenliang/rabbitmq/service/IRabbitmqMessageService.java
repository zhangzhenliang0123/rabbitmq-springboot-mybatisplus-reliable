package xyz.zhenliang.rabbitmq.service;

import xyz.zhenliang.rabbitmq.dto.RabbitmqMsgDTO;
import xyz.zhenliang.rabbitmq.entity.RabbitmqMessage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * RabbitMQ消息表服务接口
 * 提供对RabbitMQ消息的全生命周期管理，包括消息创建、发送状态管理、消费状态管理等
 * </p>
 *
 * @author zzl
 * @since 2025-09-06
 */
public interface IRabbitmqMessageService extends IService<RabbitmqMessage> {
    /**
     * 重新发送消息
     * 将消息状态重置为待发送状态，发送次数加1，并重置消费状态为未消费
     *
     * @param messageId 消息ID
     * @return 是否更新成功
     */
    boolean resendMsg(String messageId);

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
    RabbitmqMessage createMessageBySender(String messageId, String businessId, String exchange, String routingKey, String messageBody);

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
    RabbitmqMessage createMessageByConsumer(String messageId, String businessId, String exchange, String routingKey, String messageBody);


    /**
     * 更新消息发送成功状态
     * 将消息发送状态更新为成功状态
     *
     * @param messageId 消息ID
     * @param sendCount 发送次数
     * @return 是否更新成功
     */
    boolean updateSendSuccessById(String messageId, int sendCount);


    /**
     * 更新消息发送失败状态
     * 将消息发送状态更新为失败状态，并记录错误信息
     *
     * @param messageId    消息ID
     * @param sendCount    发送次数
     * @param errorMessage 错误信息
     * @return 是否更新成功
     */
    boolean updateSendFailById(String messageId, int sendCount, String errorMessage);

    /**
     * 设置消息发送确认成功
     * 更新消息发送状态为成功，并记录确认时间
     *
     * @param messageId 消息ID
     * @return 是否更新成功
     */
    boolean updateSendConfirmSuccessById(String messageId);

    /**
     * 设置消息发送确认失败
     * 更新消息发送状态为失败，记录确认时间和失败原因
     *
     * @param messageId    消息ID
     * @param errorMessage 失败原因
     * @return 是否更新成功
     */
    boolean updateSendConfirmFailById(String messageId, String errorMessage);

    /**
     * 设置消息消费成功
     * 更新消息消费状态为成功，并记录消费成功时间
     *
     * @param messageId 消息ID
     * @return 是否更新成功
     */
    boolean updateConsumeSuccessById(String messageId);

    /**
     * 设置消息消费失败
     * 更新消息消费状态为失败，并记录失败原因
     *
     * @param messageId    消息ID
     * @param errorMessage 失败原因
     * @return 是否更新成功
     */
    boolean updateConsumeFailById(String messageId, String errorMessage);

    /**
     * 幂等消息判断接口
     * 检查消息是否已消费，用于实现消息消费的幂等性控制，防止重复消费
     *
     * @param msgDTO 消息DTO对象，包含消息ID等信息
     * @return true-消息已消费或正在消费未超时，false-消息可被消费
     */
    <T> boolean checkAndLockMessageConsume(RabbitmqMsgDTO<T> msgDTO);

    /**
     * 处理死信消息
     * 当消息成为死信消息时进行处理，记录相关信息
     *
     * @param messageId   消息ID
     * @param messageBody 消息体内容
     * @return 是否处理成功
     */
    boolean processDeadLetterMessage(String messageId, String messageBody);


}