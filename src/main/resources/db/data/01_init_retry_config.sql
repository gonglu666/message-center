-- ======================================
-- 初始化数据
-- ======================================

-- 重试配置初始数据
INSERT INTO retry_config (config_key, config_value, description) VALUES
('retry.intervals', '[10, 30]', '重试间隔时间(分钟)'),
('retry.max_count', '2', '最大重试次数'),
('retry.enabled', 'true', '是否启用重试机制')
ON DUPLICATE KEY UPDATE 
    config_value = VALUES(config_value),
    description = VALUES(description);
