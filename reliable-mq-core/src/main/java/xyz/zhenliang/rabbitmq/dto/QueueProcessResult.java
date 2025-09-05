package xyz.zhenliang.rabbitmq.dto;

import java.util.Set;
import java.util.HashSet;

/**
 * 队列处理结果封装类
 * 用于统计和记录消息队列处理的相关数据
 */
public class QueueProcessResult {
    /**
     * 已处理的消息总数
     */
    int processedCount = 0;

    /**
     * 处理成功的消息数量
     */
    int successCount = 0;

    /**
     * 处理失败的消息数量
     */
    int failureCount = 0;

    /**
     * 无法处理的消息ID集合
     * 这些消息可能由于格式错误或其他原因无法被正常处理
     */
    private Set<String> unprocessableMessageIds = new HashSet<>();

    public int getProcessedCount() {
        return processedCount;
    }

    public void setProcessedCount(int processedCount) {
        this.processedCount = processedCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public Set<String> getUnprocessableMessageIds() {
        return unprocessableMessageIds;
    }

    public void setUnprocessableMessageIds(Set<String> unprocessableMessageIds) {
        this.unprocessableMessageIds = unprocessableMessageIds;
    }
}