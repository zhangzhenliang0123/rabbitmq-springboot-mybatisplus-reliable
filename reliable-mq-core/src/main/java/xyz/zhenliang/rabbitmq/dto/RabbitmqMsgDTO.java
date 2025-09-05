package xyz.zhenliang.rabbitmq.dto;

import java.time.LocalDateTime;

/**
 * 统一的消息对象
 *
 * @param <T>
 */
public class RabbitmqMsgDTO<T> {
    private String businessId; //业务id
    private String messageId;
    private String exchange;
    private String routingKey;
    private String extraInfo;
    private LocalDateTime createTime = LocalDateTime.now();
    private T data;

    public RabbitmqMsgDTO() {
    }

    public RabbitmqMsgDTO(String messageId, String exchange, String routingKey, T data, String businessId, String extraInfo) {
        this.messageId = messageId;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.businessId = businessId;
        this.data = data;
        this.extraInfo = extraInfo;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
