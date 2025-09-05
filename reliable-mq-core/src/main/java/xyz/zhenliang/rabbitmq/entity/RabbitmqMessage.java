package xyz.zhenliang.rabbitmq.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * rabbitmq消息表
 * </p>
 *
 * @author zzl
 * @since 2025-09-06
 */
@TableName("rabbitmq_message")
public class RabbitmqMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息id
     */
    @TableId(value = "id")
    private String id;

    /**
     * 业务键（用于关联业务数据）
     */
    private String businessId;

    /**
     * RabbitMQ交换机名称
     */
    private String exchange;

    /**
     * RabbitMQ路由键
     */
    private String routingKey;

    /**
     * 消息内容（JSON格式）
     */
    private String messageBody;

    /**
     * 消息发送状态：0-初始, 1-发送中, 2-发送确认成功, 3-发送失败
     */
    private Integer sendStatus;

    /**
     * 最后一次Broker发送确认时间
     */
    private LocalDateTime confirmLastTime;

    /**
     * 发送次数
     */
    private Integer sendCount;

    /**
     * 最后一次发送时间
     */
    private LocalDateTime sendLastTime;

    /**
     * 最后一次发送错误信息
     */
    private String sendErrorMessage;

    /**
     * 消息消费状态:1-未消费,2-消费中,3-消费成功,4-消费失败
     */
    private Integer consumeStatus;

    /**
     * 消费成功时间
     */
    private LocalDateTime consumeSuccessTime;

    /**
     * 消费次数
     */
    private Integer consumeCount;

    /**
     * 最后一次消费时间
     */
    private LocalDateTime consumeLastTime;

    /**
     * 最后一次消费错误信息
     */
    private String consumeErrorMessage;

    /**
     * 消息保存方式:1-发送者保存,2-消费者保存
     */
    private Integer savedBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public Integer getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(Integer sendStatus) {
        this.sendStatus = sendStatus;
    }

    public LocalDateTime getConfirmLastTime() {
        return confirmLastTime;
    }

    public void setConfirmLastTime(LocalDateTime confirmLastTime) {
        this.confirmLastTime = confirmLastTime;
    }

    public Integer getSendCount() {
        return sendCount;
    }

    public void setSendCount(Integer sendCount) {
        this.sendCount = sendCount;
    }

    public LocalDateTime getSendLastTime() {
        return sendLastTime;
    }

    public void setSendLastTime(LocalDateTime sendLastTime) {
        this.sendLastTime = sendLastTime;
    }

    public String getSendErrorMessage() {
        return sendErrorMessage;
    }

    public void setSendErrorMessage(String sendErrorMessage) {
        this.sendErrorMessage = sendErrorMessage;
    }

    public Integer getConsumeStatus() {
        return consumeStatus;
    }

    public void setConsumeStatus(Integer consumeStatus) {
        this.consumeStatus = consumeStatus;
    }

    public LocalDateTime getConsumeSuccessTime() {
        return consumeSuccessTime;
    }

    public void setConsumeSuccessTime(LocalDateTime consumeSuccessTime) {
        this.consumeSuccessTime = consumeSuccessTime;
    }

    public Integer getConsumeCount() {
        return consumeCount;
    }

    public void setConsumeCount(Integer consumeCount) {
        this.consumeCount = consumeCount;
    }

    public LocalDateTime getConsumeLastTime() {
        return consumeLastTime;
    }

    public void setConsumeLastTime(LocalDateTime consumeLastTime) {
        this.consumeLastTime = consumeLastTime;
    }

    public String getConsumeErrorMessage() {
        return consumeErrorMessage;
    }

    public void setConsumeErrorMessage(String consumeErrorMessage) {
        this.consumeErrorMessage = consumeErrorMessage;
    }

    public Integer getSavedBy() {
        return savedBy;
    }

    public void setSavedBy(Integer savedBy) {
        this.savedBy = savedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "RabbitmqMessage{" +
        "id = " + id +
        ", businessId = " + businessId +
        ", exchange = " + exchange +
        ", routingKey = " + routingKey +
        ", messageBody = " + messageBody +
        ", sendStatus = " + sendStatus +
        ", confirmLastTime = " + confirmLastTime +
        ", sendCount = " + sendCount +
        ", sendLastTime = " + sendLastTime +
        ", sendErrorMessage = " + sendErrorMessage +
        ", consumeStatus = " + consumeStatus +
        ", consumeSuccessTime = " + consumeSuccessTime +
        ", consumeCount = " + consumeCount +
        ", consumeLastTime = " + consumeLastTime +
        ", consumeErrorMessage = " + consumeErrorMessage +
        ", savedBy = " + savedBy +
        ", createdAt = " + createdAt +
        "}";
    }
}
