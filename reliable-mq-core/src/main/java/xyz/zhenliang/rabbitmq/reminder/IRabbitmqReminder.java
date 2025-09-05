package xyz.zhenliang.rabbitmq.reminder;

public interface IRabbitmqReminder {
    /**
     * 发送失败提醒方法
     * 当消息发送失败时调用此方法进行提醒
     */
    default void sendFailedReminder(String messageId, String messageBody, String errorMessage) {

    }

    /**
     * 消费失败提醒方法
     * 当消息消费失败时调用此方法进行提醒
     */
    default void consumeFailedReminder(String messageId, String messageBody, String errorMessage) {

    }
}