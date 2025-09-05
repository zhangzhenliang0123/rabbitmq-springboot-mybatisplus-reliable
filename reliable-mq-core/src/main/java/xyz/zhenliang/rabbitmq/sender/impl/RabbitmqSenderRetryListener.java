package xyz.zhenliang.rabbitmq.sender.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;
import xyz.zhenliang.rabbitmq.context.CorrelationDataContext;
import xyz.zhenliang.rabbitmq.entity.RabbitmqMessage;
import xyz.zhenliang.rabbitmq.service.IRabbitmqMessageService;
import xyz.zhenliang.rabbitmq.service.impl.RabbitmqReminderService;

/**
 * RabbitMQ消息发送重试监听器
 * 用于监听消息发送过程中的重试事件，并在重试结束后更新消息状态
 */
@Component
public class RabbitmqSenderRetryListener implements RetryListener {
    private static final Logger log = LoggerFactory.getLogger(RabbitmqSenderRetryListener.class);
    @Autowired
    private IRabbitmqMessageService rabbitmqMessageService;
    @Autowired
    private RabbitmqReminderService rabbitmqReminderService;

    /**
     * 从重试上下文中获取消息ID
     *
     * @param context 重试上下文
     * @return 消息ID，如果获取失败则返回null
     */
    public String getMessageId(RetryContext context) {
        CorrelationData correlationData = CorrelationDataContext.get();
        if (correlationData != null) {
            return correlationData.getId();
        }
        return null;
    }

//    @Override
//    public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
//        boolean flag=RetryListener.super.open(context, callback);
//        log.error("sender retry open {}",flag);
//        String messageId=getMessageId(context);
//        return flag;
//    }
//
//    @Override
//    public <T, E extends Throwable> void onSuccess(RetryContext context, RetryCallback<T, E> callback, T result) {
//        log.error("sender retry success{}",context.getRetryCount());
//
//        String messageId=getMessageId(context);
//
//        RetryListener.super.onSuccess(context, callback, result);
//    }
//
//    @Override
//    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
//        String messageId=getMessageId(context);
//        log.error("sender retry error:{}",context.getRetryCount());
//        RetryListener.super.onError(context, callback, throwable);
//    }

    /**
     * 重试结束后的回调方法
     * 在重试结束后，根据是否有异常来判断消息发送成功或失败，并更新数据库中的消息状态
     *
     * @param context   重试上下文
     * @param callback  重试回调
     * @param throwable 异常信息，如果为null表示重试成功
     */
    @Override
    public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        RetryListener.super.close(context, callback, throwable);

        // 获取消息ID
        String messageId = getMessageId(context);
        // 清除当前线程的CorrelationData上下文
        CorrelationDataContext.clear();

        if (messageId != null) {
            if (throwable != null) {
                // 发送失败情况：更新消息为发送失败状态，记录重试次数和失败原因
                rabbitmqMessageService.updateSendFailById(messageId, context.getRetryCount(), throwable.getMessage());

                rabbitmqReminderService.sendFailedReminder(messageId, throwable);
            } else {
                // 发送成功情况：更新消息为记录重试次数.发送成功情况下，可能已经收到确认信息
                // context.getRetryCount()是失败重试的次数，没有算成功的次数,所以多+1
                rabbitmqMessageService.updateSendSuccessById(messageId, context.getRetryCount() + 1);
            }
        }
    }
}