package xyz.zhenliang.rabbitmq.rescue.retry;

import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * RabbitMQ重试管理器
 * 用于判断当前是否为最后一次重试机会，提供重试次数获取功能
 */
@Service
public class RabbitmqRetryManager {

    @Autowired
    private RabbitProperties rabbitProperties;

    /**
     * 判断是否最后一次发送
     *
     * @return boolean 是否最后一次发送
     */
    public boolean isLastSend() {
        // 检查发送端重试是否启用
        boolean retryEnabled = rabbitProperties.getTemplate().getRetry().isEnabled();
        if (retryEnabled) {
            // 获取最大重试次数
            int maxAttempts = rabbitProperties.getTemplate().getRetry().getMaxAttempts();

            // 获取当前重试次数
            int retryCount = getRetryCount();
            return retryCount >= maxAttempts;
        } else {
            // 未启用重试则视为最后一次
            return true;
        }
    }

    /**
     * 判断是否最后一次消费
     *
     * @return boolean 是否最后一次消费
     */
    public boolean isLastConsume() {
        // 检查消费端重试是否启用
        boolean retryEnabled = rabbitProperties.getListener().getSimple().getRetry().isEnabled();
        if (retryEnabled) {
            // 获取最大重试次数
            int maxAttempts = rabbitProperties.getListener().getSimple().getRetry().getMaxAttempts();

            // 获取当前重试次数
            int retryCount = getRetryCount();
            return retryCount >= maxAttempts;
        } else {
            // 未启用重试则视为最后一次
            return true;
        }
    }

    /**
     * 获取当前重试次数
     *
     * @return int 当前重试次数（从1开始计数）
     */
    public int getRetryCount() {
        RetryContext retryContext = RetrySynchronizationManager.getContext();
        int retryCount = 0;
        if (retryContext != null) {
            retryCount = retryContext.getRetryCount();
        }
        // 重试次数从0开始，+1后从1开始计数
        return retryCount + 1;
    }
}