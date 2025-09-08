# rabbitmq-springboot-mybatisplus-reliable

## 介绍
本框架专为 “单条消息仅被集群内某一节点消费” 的场景设计，解决 RabbitMQ 在此模式下的发送可靠性及消费可靠性问题：确保消息必达、且同一消息仅幂等消费一次。
目前仅支持 Spring Boot + MyBatis-Plus 技术栈。支持jdk17及以上。

## 术语
消息ID：本文中所说的消息ID为自定义消息ID(即对应数据库表中的id字段)，与RabbitMQ自身的消息ID不同。生成规则为 前缀（发送端配置mq.id-prefix）+32位uuid。
消息的ID会输出到报错日志中。在消费时候，不使用同一个消息表情况，可以根据消息ID判断消息来自哪里，以便重发。

## 解决方案
#### 发送问题
- 问题：
  1. 消息调用了发送但没有发送成功（应发未发），因为调用发送接口只意味着消息抵达了broker，并不代表 RabbitMQ Broker 路由并持久化了这条消息，此时消息处于“在途”状态。但如果改为同步发送性能会很差。
  2. 业务失败但消息被发出去了（不发却发）。
- 解决：用户通过统一接口发送xyz.zhenliang.rabbitmq.sender.IRabbitmqSender.sendMessage。
  - 发送：本地消息表（事务中保存）+事务提交后发送消息+spring自带发送重试+异步确认+发送失败提醒接口。
  - 监控：发送失败消息数量查询。
  - 补救：发送失败消息查询（消息表）接口+重发消息接口+删除消息接口
- 过程：
  1. 发送时候将消息保存到消息表,使用自定义的消息ID（可以对消息ID设置前缀，以便根据消息id可以判断来自哪个服务）
  2. 对消息异步确认,将确认结果保存到消息表
  3. 在数据库的事务提交后，进行消息发送操作。目的两个，一、保证与业务在同一事务，防止业务失败但消息被发出去了；二、确保消息被保存到数据后才能收到确认消息
  4. 如果消息发送失败（消息没有发送到broker，调用发送接口会失败）,使用springboot机制重发，如果重试失败，记录为发送失败
  5. 如果消息确认失败，记录为发送失败
  6. 提醒,转人工处理
     - 批量提醒，统计对超过指定时间未发送成功的消息,框架提供查询接口。
     - 单条发送失败，系统提供通知接口，实现接口bean，在失败时候通知。
  7. 人工补救： 提供对消息的重发、查询失败消息条数、查询发送失败消息接口
  8. 日志，会包含消息的id。消息发送失败，级别日志是error。消息发送成功，日志级别是info。

#### 消费问题
- 问题：
  1. 同一个消息可能会被消费多次。rabbitmq保证消息“至少被投递一次”而不是“精确投递一次”
  2. 消息消费失败，可能重试一次就好了。 
- 解决：用户消费继承统一的抽象类xyz.zhenliang.rabbitmq.demo.consumer.listener.AbstractRabbitmqConsumer，实现里面的handleData方法。
  - 消费：本地消息表幂等消费（收到消息保存，已有不保存；消费成功失败记录）+死信队列（数据库宕机场景需人工处理）+spring自带消费重试+消费失败提醒接口。
  - 监控：死信队列消息数量查询+消费失败消息数量查询。
  - 补救：死信队列转换为消息表（恢复）+删除死信队列消息+消费失败查询（消息表）接口+重发消息接口+删除消息接口。
- 过程：
  1. 对消息进行幂等判断，根据自定义消息id判断消息是否已经处理过，如果处理过忽略。如果消息表不存在消息（消费者和发送者不是同一个库），新增。
  2. 如果消息在幂等判断时候，出现异常（数据库宕机），将会把消息转到死信队列
  3. 使用spring-boot自带的重试消费机制重试
  4. 提醒,转人工处理
     - 批量提醒，统计超过指定时间段未消费成功的消息，框架提供查询接口。
     - 消费失败提醒，需实现提醒接口。
  5. 人工补救：提供对消息的重发的接口、将死信队列消息转到消息表（在数据库宕机情况下，会将消息转到死信队列）、获取消费失败消息的接口、删除消息接口
  6. 日志，会包含消息的id。消息发送失败，级别日志是error。消息消费成功，日志级别是info。

## 使用说明
1. 初始化数据库:执行脚本sql/mysql_init.sql，其他数据库脚本请自行编写；框架源代码中没有使用手写的sql
2. 引入依赖:在pom.xml增加如下内容,如使用1.0.0版本号
```
<dependency>
    <groupId>xyz.zhenliang.rabbitmq</groupId>
    <artifactId>rabbitmq-springboot-mybatisplus-reliable</artifactId>
    <version>1.0.0</version>
</dependency>
```
但需要之前有mybatis-plus和springboot-amqp依赖
```
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>${mybatis-plus.version}</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```
3. springboot配置 
其中spring.template.retry.enabled 必须为true。否则框架不会加载。
```
mq:
  id-prefix: demo  # 消息id统一的前缀
spring:
    rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: Demo123
    # 虚拟主机，默认是"/"
    virtual-host: /
    # 开启消息确认机制
    publisher-confirm-type: correlated
    publisher-returns: true
    listener:
      simple:
        # 手动确认消息
        acknowledge-mode: manual
        # 单个消费者预取消息个数，springboot默认250
        prefetch: 10
        # 消费者并发设置
        concurrency: 1
        max-concurrency: 10
        # 消费重试配置
        retry:
          enabled: true
          max-attempts: 3
          initial-interval: 1000ms
          multiplier: 2.0
          max-interval: 10000ms
    template:
      # 发送重试配置
      retry:
        enabled: true       # 总开关：是否启用重试机制
        initial-interval: 1000ms  # 首次重试的间隔时间（第一次失败后等1秒再试）
        multiplier: 2       # 间隔乘数（下一次的间隔 = 上一次间隔 * multiplier）
        max-attempts: 3     # 最大重试次数（包括第一次发送，总共尝试3次）
        max-interval: 10000ms # 最大重试间隔（无论乘数计算出的值多大，都不会超过10秒）
      # 无法路由到队列，返回处理，会执行ReturnCallback
      mandatory: true
```
4. 消息发送：注入xyz.zhenliang.rabbitmq.sender.IRabbitmqSender的bean,调用统一的发送接口。接口会将
```
@Autowired
private IRabbitmqRescue rabbitmqRescue;
```
接口
```
public interface IRabbitmqSender {
    /**
     * 发送消息到指定的交换机和路由键
     *
     * @param exchange 交换机名称
     * @param routingKey 路由键
     * @param data 消息数据
     * @param businessId 业务ID
     * @param extraInfo 额外信息
     * @param <T> 消息数据的泛型类型
     * @return 包含消息信息的RabbitmqMsgDTO对象
     */
    public <T> RabbitmqMsgDTO<T> sendMessage(String exchange, String routingKey, T data, String businessId, String extraInfo);
```
5. 消息消费：
继承xyz.zhenliang.rabbitmq.demo.consumer.listener.AbstractRabbitmqConsumer抽象类，并实现handleData方法。
继承接口示例：
```
public class OrderListener extends AbstractRabbitmqListener<OrderDTO>
```
实现方法：
```
/**
 * 处理消息DTO数据业务方法
 *
 * @param msgDTO 消息DTO对象，包含消息ID、业务数据等信息
 */
public void handleData(RabbitmqMsgDTO<T> msgDTO)
```
6. 通知接口实现
需要自己实现接口xyz.zhenliang.rabbitmq.reminder.IRabbitmqReminder，并注册为bean.
接口方法
```
    /**
     * 发送失败提醒方法
     * 当消息发送失败时调用此方法进行提醒
     */
    default void sendFailedReminder(String messageId, String messageBody,String errorMessage) {

    }
    
    /**
     * 消费失败提醒方法
     * 当消息消费失败时调用此方法进行提醒
     */
    default void consumeFailedReminder(String messageId,String messageBody,String errorMessage) {

    }
```
实现接口示例,通过@Service注册为bean:
```
@Service
public XXX implements IRabbitmqReminder {}
```

7. 人工补救接口，见xyz.zhenliang.rabbitmq.rescue.IRabbitmqRescue
注入到自己的代码就可以使用
```
@Autowired
private IRabbitmqRescue rabbitmqRescue;
```
包含如下方法：
```
    /**
     * 根据消息id重发消息
     * @param messageId 消息ID
     * @return 是否重发成功
     */
    void resendMessage(String messageId);

    /**
     * 删除消息表中的一个消息
     * @param messageId 消息ID
     * @return 是否删除成功
     */
    void deleteMessage(String messageId);
    
    /**
     * 获取指定队列中消息数量
     * @param queueName 队列名称
     * @return 消息数量，如果队列不存在或无法访问则返回-1
     */
    long getDeadLetterCount(String queueName);


    /**
     * 将死信队列中的消息移动到消息表中
     * @param deadLetterQueueName 死信队列名称
     * @return 队列处理结果，包含处理数量、成功数量、失败数量等信息
     */
    QueueProcessResult moveQueueMessagesToMsgTable(String deadLetterQueueName);

    /**
     * 清空指定队列中的所有消息
     * @param queueName 队列名称
     */
    void purgeQueue(String queueName);
    
    /**
     * 获取发送失败消息总数
     * @return 发送失败消息总数
     */
    long getSendFailedCount(long startSecondsAgo,long endSecondsAgo);
    
    /**
     * 分页获取发送失败的消息
     * @param page 分页参数
     * @param startSecondsAgo 起始时间（秒前）
     * @param endSecondsAgo 结束时间（秒前）
     * @return 发送失败的消息分页数据
     */
    Page<RabbitmqMessage> getSendFailedPage(Page<RabbitmqMessage> page, long startSecondsAgo, long endSecondsAgo);
    
    /**
     * 获取消费不成功消息总数
     * @return 消费不成功消息总数
     */
    long getConsumeFailedCount(long startSecondsAgo,long endSecondsAgo);
    
    /**
     * 分页获取消费失败的消息
     * @param page 分页参数
     * @param startSecondsAgo 起始时间（秒前）
     * @param endSecondsAgo 结束时间（秒前）
     * @return 消费失败的消息分页数据
     */
    Page<RabbitmqMessage> getConsumeFailedPage(Page<RabbitmqMessage> page, long startSecondsAgo, long endSecondsAgo);
    /**
     * 根据消息ID获取消息
     *
     * @param messageId 消息ID
     * @return 消息实体对象
     */
    RabbitmqMessage getMessageById(String messageId);
```

## 示例说明

1.  示例依赖中间件部署
```
cd deploy
docker compose up -d
```
2.  初始化数据库
  - 创建数据库
```
CREATE DATABASE IF NOT EXISTS demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
use demo;
```
  - 初始化数据，执行脚本sql/mysql_init.sql

3.  启动reliable-mq-demo应用
```
com.zzl.rabbitmq.demo.DemoApplication
```
4.  接口调用
  - 访问 http://localhost:10000/doc.html

5. rabbitmq管理界面
   http://localhost:15672/
用户名：admin
密码：Demo123

## 其他
数据如果太多，请自行删除消费成功的数据
