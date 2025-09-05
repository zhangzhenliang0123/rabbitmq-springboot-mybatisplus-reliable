package xyz.zhenliang.rabbitmq.sender;


import xyz.zhenliang.rabbitmq.dto.RabbitmqMsgDTO;

/**
 * RabbitMQ消息发送接口
 * 定义了发送消息和重新发送消息的标准方法
 */
public interface IRabbitmqSender {
    /**
     * 发送消息到指定的交换机和路由键
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param data       消息数据
     * @param businessId 业务ID
     * @param extraInfo  额外信息
     * @param <T>        消息数据的泛型类型
     * @return 包含消息信息的RabbitmqMsgDTO对象
     */
    public <T> RabbitmqMsgDTO<T> sendMessage(String exchange, String routingKey, T data, String businessId, String extraInfo);

    /**
     * 根据消息ID重新发送消息
     *
     * @param messageId 消息ID
     */
    public void resendMsg(String messageId);
}