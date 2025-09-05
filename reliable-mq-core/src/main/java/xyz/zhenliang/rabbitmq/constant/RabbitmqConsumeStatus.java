package xyz.zhenliang.rabbitmq.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ消息消费状态枚举类
 * 用于标识消息的消费状态
 */
public enum RabbitmqConsumeStatus {

    UNCONSUMED(1, "未消费"),
    CONSUMING(2, "消费中"),
    SUCCESS(3, "消费成功"),
    FAILED(4, "消费失败");

    /**
     * 状态值
     */
    private final int value;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 用于快速根据值查找枚举的静态映射表
     */
    private static final Map<Integer, RabbitmqConsumeStatus> VALUE_MAP = new HashMap<>();

    // 初始化静态映射表
    static {
        for (RabbitmqConsumeStatus status : RabbitmqConsumeStatus.values()) {
            VALUE_MAP.put(status.value, status);
        }
    }

    /**
     * 构造方法
     *
     * @param value       状态值
     * @param description 状态描述
     */
    RabbitmqConsumeStatus(int value, String description) {
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
     * 根据状态值获取对应的枚举对象
     *
     * @param value 状态值
     * @return 对应的枚举对象
     * @throws IllegalArgumentException 当找不到匹配的状态值时抛出异常
     */
    public static RabbitmqConsumeStatus fromValue(int value) {
        RabbitmqConsumeStatus status = VALUE_MAP.get(value);
        if (status == null) {
            throw new IllegalArgumentException("No matching RabbitmqConsumeStatus for value: " + value);
        }
        return status;
    }

    /**
     * 重写toString方法，返回状态的详细信息
     *
     * @return 包含状态值和描述的字符串
     */
    @Override
    public String toString() {
        return "RabbitmqConsumeStatus{" +
                "value=" + value +
                ", description='" + description + '\'' +
                '}';
    }
}