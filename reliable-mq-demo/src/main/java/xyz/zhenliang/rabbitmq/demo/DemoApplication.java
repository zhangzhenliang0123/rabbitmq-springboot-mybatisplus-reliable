package xyz.zhenliang.rabbitmq.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DemoApplication {
    private static Logger log = LoggerFactory.getLogger(DemoApplication.class);
    public static void main(String[] args) {
        try {
            ConfigurableApplicationContext ctx = SpringApplication.run(DemoApplication.class, args);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }
}
