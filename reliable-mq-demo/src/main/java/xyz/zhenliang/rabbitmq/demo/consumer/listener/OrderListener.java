package xyz.zhenliang.rabbitmq.demo.consumer.listener;

import com.rabbitmq.client.Channel;
import xyz.zhenliang.rabbitmq.demo.consumer.handler.OrderHandler;
import xyz.zhenliang.rabbitmq.demo.dto.OrderDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.zhenliang.rabbitmq.demo.config.DemoConfig;
import xyz.zhenliang.rabbitmq.consumer.AbstractRabbitmqListener;
import xyz.zhenliang.rabbitmq.dto.RabbitmqMsgDTO;

import java.io.IOException;

/**
 * 订单消息监听器
 * 处理订单相关的RabbitMQ消息
 */
@Component
public class OrderListener extends AbstractRabbitmqListener<OrderDTO> {
    private static final Logger log= LoggerFactory.getLogger(OrderListener.class);
    @Autowired
    private OrderHandler orderHandler;
    /**
     * 处理订单消息
     * @param message 消息对象
     * @param channel 通道对象
     */
    @RabbitListener(queues = DemoConfig.QUEUE_NAME)
    public void handleOrder(Message message, Channel channel) {
        this.consume(message, channel);
    }
    @Override
    public void handleData(RabbitmqMsgDTO<OrderDTO> msgDTO) {
        OrderDTO orderDTO=msgDTO.getData();
        //if(true) throw new RuntimeException("xxxx");
        orderHandler.handleOrder(orderDTO);
    }
}