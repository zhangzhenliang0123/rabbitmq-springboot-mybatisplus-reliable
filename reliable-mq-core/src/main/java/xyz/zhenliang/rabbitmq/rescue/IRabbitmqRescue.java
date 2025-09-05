package xyz.zhenliang.rabbitmq.rescue;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import xyz.zhenliang.rabbitmq.dto.QueueProcessResult;
import xyz.zhenliang.rabbitmq.entity.RabbitmqMessage;

import java.util.List;
import java.util.Set;

public interface IRabbitmqRescue {


    /**
     * 根据消息id重发消息
     *
     * @param messageId 消息ID
     * @return 是否重发成功
     */
    void resendMessage(String messageId);

    /**
     * 删除消息表中的一个消息
     *
     * @param messageId 消息ID
     * @return 是否删除成功
     */
    void deleteMessage(String messageId);

    /**
     * 获取指定队列中消息数量
     *
     * @param queueName 队列名称
     * @return 消息数量，如果队列不存在或无法访问则返回-1
     */
    long getDeadLetterCount(String queueName);


    /**
     * 将死信队列中的消息移动到消息表中
     *
     * @param deadLetterQueueName 死信队列名称
     * @return 队列处理结果，包含处理数量、成功数量、失败数量等信息
     */
    QueueProcessResult moveQueueMessagesToMsgTable(String deadLetterQueueName);

    /**
     * 清空指定队列中的所有消息
     *
     * @param queueName 队列名称
     */
    void purgeQueue(String queueName);

    /**
     * 获取发送失败消息总数
     *
     * @return 发送失败消息总数
     */
    long getSendFailedCount(long startSecondsAgo, long endSecondsAgo);

    /**
     * 分页获取发送失败的消息
     *
     * @param page            分页参数
     * @param startSecondsAgo 起始时间（秒前）
     * @param endSecondsAgo   结束时间（秒前）
     * @return 发送失败的消息分页数据
     */
    Page<RabbitmqMessage> getSendFailedPage(Page<RabbitmqMessage> page, long startSecondsAgo, long endSecondsAgo);

    /**
     * 获取消费不成功消息总数
     *
     * @return 消费不成功消息总数
     */
    long getConsumeFailedCount(long startSecondsAgo, long endSecondsAgo);

    /**
     * 分页获取消费失败的消息
     *
     * @param page            分页参数
     * @param startSecondsAgo 起始时间（秒前）
     * @param endSecondsAgo   结束时间（秒前）
     * @return 消费失败的消息分页数据
     */
    Page<RabbitmqMessage> getConsumeFailedPage(Page<RabbitmqMessage> page, long startSecondsAgo, long endSecondsAgo);

    /**
     * 根据消息ID获取消息
     *
     * @param messageId 消息ID
     * @return 消息实体对象
     */
    RabbitmqMessage getMessageById(String messageId);

}