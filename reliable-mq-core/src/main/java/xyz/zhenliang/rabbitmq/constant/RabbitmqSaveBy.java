package xyz.zhenliang.rabbitmq.constant;

/**
 * RabbitMQ消息发送状态枚举类
 * 用于标识消息在RabbitMQ中的发送状态
 */
public enum RabbitmqSaveBy {
    SENDER_SAVE(1, "发送者保存"),
    CONSUMER_SAVE(2, "消费者保存"),
    DEAD_LETTER_SAVE(3, "死信处理者保存");

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
    RabbitmqSaveBy(int value, String description) {
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
        return "RabbitmqSaveBy{" +
                "value=" + value +
                ", description='" + description + '\'' +
                '}';
    }
}