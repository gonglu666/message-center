-- ======================================
-- 系统配置表
-- ======================================

-- 4.1 系统配置表 (system_config)
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
