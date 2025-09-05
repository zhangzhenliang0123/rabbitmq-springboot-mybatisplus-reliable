package xyz.zhenliang.rabbitmq.demo.dto;

import java.time.Instant;

/**
 * 订单数据传输对象
 */
public class OrderDTO {
    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 订单名称
     */
    private String orderName;

    /**
     * 订单价格
     */
    private Double orderPrice = 0.0;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public Double getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(Double orderPrice) {
        this.orderPrice = orderPrice;
    }
}