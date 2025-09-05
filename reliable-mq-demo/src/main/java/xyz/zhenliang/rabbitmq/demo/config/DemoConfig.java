package xyz.zhenliang.rabbitmq.demo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ配置类
 * 配置交换机、队列、绑定关系以及消息转换器
 */
@Configuration
public class DemoConfig {
    /**
     * 交换机名称
     */
    public static final String EXCHANGE_NAME = "order.exchange";
    
    /**
     * 队列名称
     */
    public static final String QUEUE_NAME = "order.queue";
    
    /**
     * 路由键
     */
    public static final String ROUTING_KEY = "order.routing.key";

    /**
     * 死信交换机名称
     */
    public static final String DLX_EXCHANGE_NAME = "order.dlx.exchange";

    /**
     * 死信队列名称
     */
    public static final String DLX_QUEUE_NAME = "order.dlx.queue";

    /**
     * 死信路由键
     */
    public static final String DLX_ROUTING_KEY = "order.dlx.routing.key";

    /**
     * 创建持久化直连交换机
     * @return DirectExchange对象
     */
    @Bean
    public DirectExchange orderExchange() {
        // 创建持久化交换机,设置持久化（durable）,自动删除（如果没有队列绑定到该交换机会被自动删除）
        return new DirectExchange(EXCHANGE_NAME, true, true);
    }

    /**
     * 创建死信交换机
     * @return DirectExchange对象
     */
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE_NAME, true, false);
    }

    /**
     * 创建持久化队列
     * @return Queue对象
     */
    @Bean
    public Queue orderQueue() {
        // 创建持久化队列,设置持久化（durable）,队列是否具有排他性（允许多个连接消费），自动删除（没有消费者连接的时候被自动删除）
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", DLX_EXCHANGE_NAME);
        arguments.put("x-dead-letter-routing-key", DLX_ROUTING_KEY);
        return new Queue(QUEUE_NAME, true, false, false, arguments);
    }

    /**
     * 创建死信队列
     * @return Queue对象
     */
    @Bean
    public Queue dlxQueue() {
        return new Queue(DLX_QUEUE_NAME, true, false, false);
    }

    /**
     * 绑定交换机和队列
     * @param orderQueue 队列对象
     * @param orderExchange 交换机对象
     * @return Binding对象
     */
    @Bean
    public Binding orderBinding(Queue orderQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderQueue).to(orderExchange).with(ROUTING_KEY);
    }

    /**
     * 绑定死信交换机和死信队列
     * @param dlxQueue 死信队列对象
     * @param dlxExchange 死信交换机对象
     * @return Binding对象
     */
    @Bean
    public Binding dlxBinding(Queue dlxQueue, DirectExchange dlxExchange) {
        return BindingBuilder.bind(dlxQueue).to(dlxExchange).with(DLX_ROUTING_KEY);
    }

    /**
     * 配置JSON消息转换器
     * @return Jackson2JsonMessageConverter对象
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}