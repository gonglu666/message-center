-- ======================================
-- 重试任务表
-- ======================================

-- 3.1 重试配置表 (retry_config)
CREATE TABLE IF NOT EXISTS retry_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    config_key VARCHAR(128) NOT NULL UNIQUE COMMENT '配置键',
    config_value VARCHAR(500) NOT NULL COMMENT '配置值',
    description VARCHAR(500) COMMENT '配置描述',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='重试配置表';

-- 3.2 失败消息重试表 (failed_message_retry)
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
