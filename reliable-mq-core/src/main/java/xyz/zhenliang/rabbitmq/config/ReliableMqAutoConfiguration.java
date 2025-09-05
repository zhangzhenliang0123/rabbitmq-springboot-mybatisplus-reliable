package xyz.zhenliang.rabbitmq.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;


@Configuration
@EnableAsync
@ConditionalOnProperty(name = "spring.rabbitmq.template.retry.enabled", havingValue = "true", matchIfMissing = false)
@ComponentScan(basePackages = "xyz.zhenliang.rabbitmq")
public class ReliableMqAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(ReliableMqAutoConfiguration.class);

    public ReliableMqAutoConfiguration() {
        log.info("rabbitmq-springboot-mybatisplus-reliable initialized");
    }
}
