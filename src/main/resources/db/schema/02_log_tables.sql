-- ======================================
-- 消息日志表
-- ======================================

-- 2.1 生产者消息日志表 (producer_message_log)
-- 注意：此表记录基础信息，详细日志存储在Elasticsearch中
CREATE TABLE IF NOT EXISTS producer_message_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    message_id VARCHAR(64) NOT NULL UNIQUE COMMENT '消息唯一ID',
    template_id VARCHAR(64) NOT NULL COMMENT '模板ID',
    topic_name VARCHAR(255) NOT NULL COMMENT '主题名称',
    producer_app VARCHAR(128) COMMENT '生产者应用',
    request_body JSON COMMENT '原始请求体（简要信息，完整信息存ES）',
    response_result JSON COMMENT '返回结果（简要信息，完整信息存ES）',
    status TINYINT NOT NULL COMMENT '状态：0-失败，1-成功',
    error_message TEXT COMMENT '错误信息',
    kafka_offset BIGINT COMMENT 'Kafka偏移量',
    kafka_partition INT COMMENT 'Kafka分区',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_template_id (template_id),
    INDEX idx_topic_name (topic_name),
    INDEX idx_message_id (message_id),
    INDEX idx_created_time (created_time),
    INDEX idx_producer_app (producer_app)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产者消息日志表';

-- 2.2 消费者推送日志表 (consumer_push_log)
-- 注意：此表记录基础信息，详细日志存储在Elasticsearch中
CREATE TABLE IF NOT EXISTS consumer_push_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    message_id VARCHAR(64) NOT NULL COMMENT '消息ID',
    template_id VARCHAR(64) NOT NULL COMMENT '模板ID',
    consumer_id BIGINT NOT NULL COMMENT '消费者ID',
    consumer_url VARCHAR(500) NOT NULL COMMENT '消费者URL',
    request_body JSON COMMENT '推送请求体（简要信息，完整信息存ES）',
    response_body TEXT COMMENT '消费者响应体（简要信息，完整信息存ES）',
    http_status INT COMMENT 'HTTP状态码',
    status TINYINT NOT NULL COMMENT '推送状态：0-失败，1-成功，2-重试中',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    error_message TEXT COMMENT '错误信息',
    push_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '推送时间',
    next_retry_time DATETIME COMMENT '下次重试时间',
    completed_time DATETIME COMMENT '完成时间',
    INDEX idx_message_id (message_id),
    INDEX idx_template_id (template_id),
    INDEX idx_consumer_id (consumer_id),
    INDEX idx_status (status),
    INDEX idx_next_retry_time (next_retry_time),
    INDEX idx_push_time (push_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费者推送日志表';
