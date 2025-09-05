DROP TABLE IF EXISTS `rabbitmq_message` ;
CREATE TABLE `rabbitmq_message` (
    `id` varchar(64) NOT NULL COMMENT '消息id',
    `business_id` varchar(128) DEFAULT NULL COMMENT '业务键（用于关联业务数据）',
    `exchange` varchar(128) NOT NULL COMMENT 'RabbitMQ交换机名称',
    `routing_key` varchar(128) NOT NULL COMMENT 'RabbitMQ路由键',
    `message_body` text NOT NULL COMMENT '消息内容（JSON格式）',
    `send_status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '消息发送状态：1-初始, 2-发送中, 3-发送确认成功, 4-发送失败',
    `confirm_last_time` datetime DEFAULT NULL COMMENT '最后一次Broker发送确认时间',
    `send_count` int(11) NOT NULL DEFAULT 0 COMMENT '发送次数',
    `send_last_time` datetime DEFAULT NULL COMMENT '最后一次发送时间',
    `send_error_message` text COMMENT '最后一次发送错误信息',
    `consume_status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '消息消费状态:1-未消费,2-消费中,3-消费成功,4-消费失败',
    `consume_success_time` datetime DEFAULT NULL COMMENT '消费成功时间',
    `consume_count` int(11) NOT NULL DEFAULT 0 COMMENT '消费次数',
    `consume_last_time` datetime DEFAULT NULL COMMENT '最后一次消费时间',
    `consume_error_message` text COMMENT '最后一次消费错误信息',
    `saved_by` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '消息保存方式:1-发送者保存,2-消费者保存,3-死信处理保存',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='rabbitmq消息表';
ALTER TABLE `rabbitmq_message` ADD INDEX `ix_rabbitmq_message_1`(`business_id`);
ALTER TABLE `rabbitmq_message` ADD INDEX `ix_rabbitmq_message_2`(`created_at` desc,`send_status`);
ALTER TABLE `rabbitmq_message` ADD INDEX `ix_rabbitmq_message_3`(`created_at` desc,`consume_status`);

