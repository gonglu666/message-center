-- ======================================
-- 消息中心数据库完整脚本
-- 创建数据库、表结构和初始化数据
-- ======================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS message_center 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_general_ci;

USE message_center;

-- ======================================
-- 1. 核心配置表
-- ======================================

-- 1.1 消息模板表
CREATE TABLE IF NOT EXISTS message_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    template_id VARCHAR(64) NOT NULL UNIQUE COMMENT '模板唯一标识',
    template_name VARCHAR(128) NOT NULL COMMENT '模板名称',
    topic_name VARCHAR(255) NOT NULL UNIQUE COMMENT 'Kafka主题名称',
    description VARCHAR(500) COMMENT '模板描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    partition_count INT DEFAULT 3 COMMENT '分区数量',
    replication_factor INT DEFAULT 1 COMMENT '副本因子',
    retention_ms BIGINT DEFAULT 604800000 COMMENT '消息保留时间(ms)',
    created_by VARCHAR(64) COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_template_id (template_id),
    INDEX idx_topic_name (topic_name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息模板表';

-- 1.2 模板字段定义表
CREATE TABLE IF NOT EXISTS template_field (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    template_id VARCHAR(64) NOT NULL COMMENT '模板ID',
    field_name VARCHAR(128) NOT NULL COMMENT '字段名称',
    field_alias VARCHAR(128) COMMENT '字段别名',
    field_type VARCHAR(32) NOT NULL COMMENT '字段类型：STRING, INTEGER, LONG, DOUBLE, BOOLEAN, DATE, OBJECT, ARRAY',
    data_format VARCHAR(128) COMMENT '数据格式，如日期格式',
    max_length INT COMMENT '最大长度',
    is_required TINYINT DEFAULT 1 COMMENT '是否必填：0-否，1-是',
    default_value VARCHAR(500) COMMENT '默认值',
    description VARCHAR(500) COMMENT '字段描述',
    sort_order INT DEFAULT 0 COMMENT '排序字段',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_template_id (template_id),
    INDEX idx_field_name (field_name),
    UNIQUE KEY uk_template_field (template_id, field_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板字段定义表';

-- 1.3 消费者注册表
CREATE TABLE IF NOT EXISTS consumer_registry (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    template_id VARCHAR(64) NOT NULL COMMENT '模板ID',
    consumer_name VARCHAR(128) NOT NULL COMMENT '消费者名称',
    consumer_url VARCHAR(500) NOT NULL COMMENT '消费者URL',
    http_method VARCHAR(10) DEFAULT 'POST' COMMENT 'HTTP方法',
    headers JSON COMMENT '请求头',
    timeout_ms INT DEFAULT 30000 COMMENT '超时时间(ms)',
    retry_count INT DEFAULT 3 COMMENT '重试次数',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    group_id VARCHAR(255) COMMENT 'Kafka消费者组ID',
    created_by VARCHAR(64) COMMENT '注册人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_template_id (template_id),
    INDEX idx_consumer_url (consumer_url(255)),
    INDEX idx_status (status),
    UNIQUE KEY uk_template_consumer (template_id, consumer_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费者注册表';

-- ======================================
-- 2. 消息日志表
-- ======================================

-- 2.1 生产者消息日志表
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

-- 2.2 消费者推送日志表
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

-- ======================================
-- 3. 重试任务表
-- ======================================

-- 3.1 重试配置表
CREATE TABLE IF NOT EXISTS retry_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    config_key VARCHAR(128) NOT NULL UNIQUE COMMENT '配置键',
    config_value VARCHAR(500) NOT NULL COMMENT '配置值',
    description VARCHAR(500) COMMENT '配置描述',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='重试配置表';

-- 3.2 失败消息重试表
CREATE TABLE IF NOT EXISTS failed_message_retry (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    push_log_id BIGINT NOT NULL COMMENT '推送日志ID',
    message_id VARCHAR(64) NOT NULL COMMENT '消息ID',
    template_id VARCHAR(64) NOT NULL COMMENT '模板ID',
    consumer_id BIGINT NOT NULL COMMENT '消费者ID',
    retry_count INT DEFAULT 0 COMMENT '当前重试次数',
    max_retry_count INT DEFAULT 2 COMMENT '最大重试次数',
    next_retry_time DATETIME NOT NULL COMMENT '下次重试时间',
    retry_intervals JSON COMMENT '重试间隔配置，如[10,30]表示10分钟和30分钟',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待重试，1-重试成功，2-重试失败',
    last_error_message TEXT COMMENT '最后错误信息',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_push_log_id (push_log_id),
    INDEX idx_message_id (message_id),
    INDEX idx_next_retry_time (next_retry_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='失败消息重试表';

-- ======================================
-- 4. 系统配置表
-- ======================================

-- 4.1 系统配置表
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    config_key VARCHAR(128) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type VARCHAR(32) DEFAULT 'STRING' COMMENT '配置类型：STRING, JSON, NUMBER, BOOLEAN',
    description VARCHAR(500) COMMENT '配置描述',
    is_apollo_sync TINYINT DEFAULT 0 COMMENT '是否与Apollo同步：0-否，1-是',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ======================================
-- 5. 初始化数据
-- ======================================

-- 重试配置初始数据
INSERT INTO retry_config (config_key, config_value, description) VALUES
('retry.intervals', '[10, 30]', '重试间隔时间(分钟)'),
('retry.max_count', '2', '最大重试次数'),
('retry.enabled', 'true', '是否启用重试机制')
ON DUPLICATE KEY UPDATE 
    config_value = VALUES(config_value),
    description = VALUES(description);

-- 完成提示
SELECT '消息中心数据库初始化完成！' AS message;
SELECT COUNT(*) AS table_count FROM information_schema.tables 
WHERE table_schema = 'message_center';
