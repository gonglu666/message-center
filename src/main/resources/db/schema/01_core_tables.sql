-- ======================================
-- 核心配置表
-- ======================================

-- 1.1 消息模板表 (message_template)
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

-- 1.2 模板字段定义表 (template_field)
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

-- 1.3 消费者注册表 (consumer_registry)
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
