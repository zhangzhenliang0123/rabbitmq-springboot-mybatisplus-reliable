package xyz.zhenliang.rabbitmq.demo.service;

import xyz.zhenliang.rabbitmq.demo.config.DemoConfig;
import xyz.zhenliang.rabbitmq.demo.dto.OrderDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.zhenliang.rabbitmq.dto.RabbitmqMsgDTO;
import xyz.zhenliang.rabbitmq.rescue.IRabbitmqRescue;
import xyz.zhenliang.rabbitmq.sender.IRabbitmqSender;

/**
 * 消息发送服务类
 * 提供消息发送和重发功能
 */
@Service
public class DemoService {
    @Autowired
    private IRabbitmqSender rabbitmqSender;

    @Autowired
    private IRabbitmqRescue rabbitmqRescue;
    
    /**
     * 发送订单消息到RabbitMQ
     * @param orderDTO 订单数据传输对象
     * @return RabbitMQ消息对象
     */
    public RabbitmqMsgDTO<OrderDTO> send(OrderDTO orderDTO){
        return rabbitmqSender.sendMessage(DemoConfig.EXCHANGE_NAME, DemoConfig.ROUTING_KEY,
                orderDTO, orderDTO.getOrderId(), null);
    }

    /**
     * 根据消息ID重新发送消息
     * @param messageId 消息唯一标识
     */
    public void resendMsg(String messageId){
        rabbitmqSender.resendMsg(messageId);
    }
}