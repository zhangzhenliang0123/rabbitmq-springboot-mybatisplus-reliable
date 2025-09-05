package xyz.zhenliang.rabbitmq.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.amqp.RabbitRetryTemplateCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.zhenliang.rabbitmq.consumer.RabbitmqConsumerRetryListener;
import xyz.zhenliang.rabbitmq.sender.impl.RabbitmqSenderRetryListener;

@MapperScan("xyz.zhenliang.rabbitmq.mapper")
@Configuration
public class RabbitMqConfiguration {
    /**
     * RabbitRetryTemplateCustomizer专门用于在 Spring Boot 自动配置 RetryTemplate 之后，但在将其应用到 RabbitTemplate 或监听器容器之前，对 RetryTemplate 进行额外的自定义配置。
     *
     * @param rabbitmqSenderRetryListener
     * @param rabbitmqConsumerRetryListener
     * @return
     */
    @Bean
    @ConditionalOnClass({RabbitRetryTemplateCustomizer.class})
    public RabbitRetryTemplateCustomizer retryTemplateCustomizer(RabbitmqSenderRetryListener rabbitmqSenderRetryListener, RabbitmqConsumerRetryListener rabbitmqConsumerRetryListener) {
        // 返回一个 RabbitRetryTemplateCustomizer 的 Lambda 表达式实现
        return (target, template) -> {
            // target 参数表示这个 RetryTemplate 是用于哪个目标的
            // Target.SENDER -> 用于消息发送 (RabbitTemplate)
            // Target.LISTENER -> 用于消息消费 (监听器容器)
            if (target == RabbitRetryTemplateCustomizer.Target.SENDER) {

                // template 参数是 Spring Boot 已经自动配置好的 RetryTemplate
                // 这个模板已经包含了 application.yml 中配置的所有重试参数
                template.registerListener(rabbitmqSenderRetryListener);

            } else if (target == RabbitRetryTemplateCustomizer.Target.LISTENER) {
                template.registerListener(rabbitmqConsumerRetryListener);
            }
        };
    }
}
