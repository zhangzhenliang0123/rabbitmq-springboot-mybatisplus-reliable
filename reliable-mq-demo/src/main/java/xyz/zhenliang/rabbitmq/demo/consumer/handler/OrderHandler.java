package xyz.zhenliang.rabbitmq.demo.consumer.handler;

import xyz.zhenliang.rabbitmq.demo.dto.OrderDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xyz.zhenliang.rabbitmq.utils.JsonUtils;

@Component
public class OrderHandler {
    private static final Logger log = LoggerFactory.getLogger(OrderHandler.class);
    public void handleOrder(OrderDTO orderDTO) {
        log.info("Order processed successfully: " + JsonUtils.toJson(orderDTO));
    }
}