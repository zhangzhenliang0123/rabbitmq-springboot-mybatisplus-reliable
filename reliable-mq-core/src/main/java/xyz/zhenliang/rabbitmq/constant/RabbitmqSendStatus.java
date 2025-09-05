package xyz.zhenliang.rabbitmq.constant;

/**
 * RabbitMQ消息发送状态枚举类
 * 用于标识消息在RabbitMQ中的发送状态
 */
public enum RabbitmqSendStatus {
    INIT(1, "初始"),
    SENDING(2, "发送中"),
    SUCCESS(3, "发送确认成功"),
    FAILED(4, "发送失败");

    /**
     * 状态值
     */
    private final int value;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 构造方法
     *
     * @param value       状态值
     * @param description 状态描述
     */
    RabbitmqSendStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 获取状态值
     *
     * @return 状态值
     */
    public int getValue() {
        return value;
    }

    /**
     * 获取状态描述
     *
     * @return 状态描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 重写toString方法，用于输出状态信息
     *
     * @return 包含状态值和描述的字符串
     */
    @Override
    public String toString() {
        return "RabbitmqSendStatus{" +
                "value=" + value +
                ", description='" + description + '\'' +
                '}';
    }
}