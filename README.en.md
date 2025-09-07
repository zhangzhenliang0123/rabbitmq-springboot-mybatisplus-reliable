# rabbitmq-springboot-mybatisplus-reliable

## Introduction
This framework is designed for scenarios where "a single message is consumed by only one node in the cluster", solving the reliability issues of RabbitMQ sending and consuming in this mode: ensuring messages are delivered and each message is consumed idempotently only once.
Currently, it only supports the Spring Boot + MyBatis-Plus technology stack. Supports JDK 17 and above.

