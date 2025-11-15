# 消息中心数据库设计文档

## 概述

本文档描述了消息中心系统的数据库设计，包括MySQL表结构和Elasticsearch索引设计。

## 技术栈

- Spring Boot: 2.7.10
- MySQL: 8.0+
- MyBatis-Plus: 3.5.3.1
- Elasticsearch: 7.17.9
- Kafka
- Redis
- Apollo

## MySQL表结构

### 1. 核心配置表

#### 1.1 消息模板表 (message_template)

用于存储消息模板的基本信息和Kafka主题配置。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| template_id | VARCHAR(64) | 模板唯一标识 |
| template_name | VARCHAR(128) | 模板名称 |
| topic_name | VARCHAR(255) | Kafka主题名称 |
| description | VARCHAR(500) | 模板描述 |
| status | TINYINT | 状态：0-禁用，1-启用 |
| partition_count | INT | 分区数量，默认3 |
| replication_factor | INT | 副本因子，默认1 |
| retention_ms | BIGINT | 消息保留时间(ms)，默认7天 |
| created_by | VARCHAR(64) | 创建人 |
| created_time | DATETIME | 创建时间 |
| updated_time | DATETIME | 更新时间 |

**索引**：
- PRIMARY KEY (id)
- UNIQUE KEY (template_id)
- UNIQUE KEY (topic_name)
- INDEX (status)

#### 1.2 模板字段定义表 (template_field)

用于存储模板的字段定义和校验规则。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| template_id | VARCHAR(64) | 模板ID |
| field_name | VARCHAR(128) | 字段名称 |
| field_alias | VARCHAR(128) | 字段别名 |
| field_type | VARCHAR(32) | 字段类型：STRING, INTEGER, LONG, DOUBLE, BOOLEAN, DATE, OBJECT, ARRAY |
| data_format | VARCHAR(128) | 数据格式，如日期格式 |
| max_length | INT | 最大长度 |
| is_required | TINYINT | 是否必填：0-否，1-是 |
| default_value | VARCHAR(500) | 默认值 |
| description | VARCHAR(500) | 字段描述 |
| sort_order | INT | 排序字段 |
| created_time | DATETIME | 创建时间 |
| updated_time | DATETIME | 更新时间 |

**索引**：
- PRIMARY KEY (id)
- INDEX (template_id)
- INDEX (field_name)
- UNIQUE KEY (template_id, field_name)

#### 1.3 消费者注册表 (consumer_registry)

用于存储消费者的注册信息和推送配置。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| template_id | VARCHAR(64) | 模板ID |
| consumer_name | VARCHAR(128) | 消费者名称 |
| consumer_url | VARCHAR(500) | 消费者URL |
| http_method | VARCHAR(10) | HTTP方法，默认POST |
| headers | JSON | 请求头 |
| timeout_ms | INT | 超时时间(ms)，默认30000 |
| retry_count | INT | 重试次数，默认3 |
| status | TINYINT | 状态：0-禁用，1-启用 |
| group_id | VARCHAR(255) | Kafka消费者组ID |
| created_by | VARCHAR(64) | 注册人 |
| created_time | DATETIME | 创建时间 |
| updated_time | DATETIME | 更新时间 |

**索引**：
- PRIMARY KEY (id)
- INDEX (template_id)
- INDEX (consumer_url)
- INDEX (status)
- UNIQUE KEY (template_id, consumer_name)

### 2. 消息日志表

#### 2.1 生产者消息日志表 (producer_message_log)

用于记录生产者推送消息的日志信息。

**注意**：此表记录基础信息，详细日志存储在Elasticsearch中。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| message_id | VARCHAR(64) | 消息唯一ID |
| template_id | VARCHAR(64) | 模板ID |
| topic_name | VARCHAR(255) | 主题名称 |
| producer_app | VARCHAR(128) | 生产者应用 |
| request_body | JSON | 原始请求体（简要信息） |
| response_result | JSON | 返回结果（简要信息） |
| status | TINYINT | 状态：0-失败，1-成功 |
| error_message | TEXT | 错误信息 |
| kafka_offset | BIGINT | Kafka偏移量 |
| kafka_partition | INT | Kafka分区 |
| created_time | DATETIME | 创建时间 |

**索引**：
- PRIMARY KEY (id)
- UNIQUE KEY (message_id)
- INDEX (template_id)
- INDEX (topic_name)
- INDEX (created_time)
- INDEX (producer_app)

**优化建议**：
- 按月进行分区，提高查询性能
- 定期归档历史数据

#### 2.2 消费者推送日志表 (consumer_push_log)

用于记录消息中心向消费者推送消息的日志。

**注意**：此表记录基础信息，详细日志存储在Elasticsearch中。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| message_id | VARCHAR(64) | 消息ID |
| template_id | VARCHAR(64) | 模板ID |
| consumer_id | BIGINT | 消费者ID |
| consumer_url | VARCHAR(500) | 消费者URL |
| request_body | JSON | 推送请求体（简要信息） |
| response_body | TEXT | 消费者响应体（简要信息） |
| http_status | INT | HTTP状态码 |
| status | TINYINT | 推送状态：0-失败，1-成功，2-重试中 |
| retry_count | INT | 重试次数 |
| error_message | TEXT | 错误信息 |
| push_time | DATETIME | 推送时间 |
| next_retry_time | DATETIME | 下次重试时间 |
| completed_time | DATETIME | 完成时间 |

**索引**：
- PRIMARY KEY (id)
- INDEX (message_id)
- INDEX (template_id)
- INDEX (consumer_id)
- INDEX (status)
- INDEX (next_retry_time)
- INDEX (push_time)

**优化建议**：
- 建立(status, push_time)联合索引
- 按月进行分区

### 3. 重试任务表

#### 3.1 重试配置表 (retry_config)

用于存储重试机制的配置参数。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| config_key | VARCHAR(128) | 配置键 |
| config_value | VARCHAR(500) | 配置值 |
| description | VARCHAR(500) | 配置描述 |
| created_time | DATETIME | 创建时间 |
| updated_time | DATETIME | 更新时间 |

**初始数据**：
- retry.intervals: [10, 30] - 重试间隔时间(分钟)
- retry.max_count: 2 - 最大重试次数
- retry.enabled: true - 是否启用重试机制

#### 3.2 失败消息重试表 (failed_message_retry)

用于记录需要重试的失败消息。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| push_log_id | BIGINT | 推送日志ID |
| message_id | VARCHAR(64) | 消息ID |
| template_id | VARCHAR(64) | 模板ID |
| consumer_id | BIGINT | 消费者ID |
| retry_count | INT | 当前重试次数 |
| max_retry_count | INT | 最大重试次数，默认2 |
| next_retry_time | DATETIME | 下次重试时间 |
| retry_intervals | JSON | 重试间隔配置 |
| status | TINYINT | 状态：0-待重试，1-重试成功，2-重试失败 |
| last_error_message | TEXT | 最后错误信息 |
| created_time | DATETIME | 创建时间 |
| updated_time | DATETIME | 更新时间 |

**索引**：
- PRIMARY KEY (id)
- INDEX (push_log_id)
- INDEX (message_id)
- INDEX (next_retry_time)
- INDEX (status)

### 4. 系统配置表

#### 4.1 系统配置表 (system_config)

用于存储系统级别的配置信息。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| config_key | VARCHAR(128) | 配置键 |
| config_value | TEXT | 配置值 |
| config_type | VARCHAR(32) | 配置类型：STRING, JSON, NUMBER, BOOLEAN |
| description | VARCHAR(500) | 配置描述 |
| is_apollo_sync | TINYINT | 是否与Apollo同步：0-否，1-是 |
| created_time | DATETIME | 创建时间 |
| updated_time | DATETIME | 更新时间 |

**索引**：
- PRIMARY KEY (id)
- UNIQUE KEY (config_key)

## Elasticsearch索引设计

由于步骤4和5记录的日志比较庞大，需要写入到ES中进行存储和查询。

### ES索引1: producer_message_log_index

**用途**：存储生产者推送消息的完整日志

**索引结构**：

```json
{
  "mappings": {
    "properties": {
      "message_id": {
        "type": "keyword"
      },
      "template_id": {
        "type": "keyword"
      },
      "topic_name": {
        "type": "keyword"
      },
      "producer_app": {
        "type": "keyword"
      },
      "request_body": {
        "type": "text",
        "fields": {
          "keyword": {
            "type": "keyword",
            "ignore_above": 256
          }
        }
      },
      "response_result": {
        "type": "text",
        "fields": {
          "keyword": {
            "type": "keyword",
            "ignore_above": 256
          }
        }
      },
      "status": {
        "type": "byte"
      },
      "error_message": {
        "type": "text"
      },
      "kafka_offset": {
        "type": "long"
      },
      "kafka_partition": {
        "type": "integer"
      },
      "created_time": {
        "type": "date",
        "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"
      }
    }
  },
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 1,
    "index.lifecycle.name": "message-log-policy",
    "index.lifecycle.rollover_alias": "producer_message_log"
  }
}
```

### ES索引2: consumer_push_log_index

**用途**：存储消费者推送消息的完整日志

**索引结构**：

```json
{
  "mappings": {
    "properties": {
      "message_id": {
        "type": "keyword"
      },
      "template_id": {
        "type": "keyword"
      },
      "consumer_id": {
        "type": "long"
      },
      "consumer_url": {
        "type": "keyword"
      },
      "request_body": {
        "type": "text",
        "fields": {
          "keyword": {
            "type": "keyword",
            "ignore_above": 256
          }
        }
      },
      "response_body": {
        "type": "text"
      },
      "http_status": {
        "type": "integer"
      },
      "status": {
        "type": "byte"
      },
      "retry_count": {
        "type": "integer"
      },
      "error_message": {
        "type": "text"
      },
      "push_time": {
        "type": "date",
        "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"
      },
      "next_retry_time": {
        "type": "date",
        "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"
      },
      "completed_time": {
        "type": "date",
        "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"
      }
    }
  },
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 1,
    "index.lifecycle.name": "message-log-policy",
    "index.lifecycle.rollover_alias": "consumer_push_log"
  }
}
```

### ES索引生命周期策略

建议配置索引生命周期管理(ILM)策略：

```json
{
  "policy": {
    "phases": {
      "hot": {
        "min_age": "0ms",
        "actions": {
          "rollover": {
            "max_size": "50gb",
            "max_age": "30d"
          },
          "set_priority": {
            "priority": 100
          }
        }
      },
      "warm": {
        "min_age": "30d",
        "actions": {
          "set_priority": {
            "priority": 50
          },
          "shrink": {
            "number_of_shards": 1
          }
        }
      },
      "cold": {
        "min_age": "90d",
        "actions": {
          "set_priority": {
            "priority": 0
          }
        }
      },
      "delete": {
        "min_age": "180d",
        "actions": {
          "delete": {}
        }
      }
    }
  }
}
```

## 使用说明

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS message_center 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_general_ci;
```

### 2. 执行表结构脚本

按顺序执行以下SQL脚本：

```bash
mysql -u username -p message_center < src/main/resources/db/schema/01_core_tables.sql
mysql -u username -p message_center < src/main/resources/db/schema/02_log_tables.sql
mysql -u username -p message_center < src/main/resources/db/schema/03_retry_tables.sql
mysql -u username -p message_center < src/main/resources/db/schema/04_system_tables.sql
```

### 3. 执行初始化数据脚本

```bash
mysql -u username -p message_center < src/main/resources/db/data/01_init_retry_config.sql
```

### 4. 创建Elasticsearch索引

使用Kibana或curl命令创建ES索引：

```bash
# 创建生产者日志索引
curl -X PUT "localhost:9200/producer_message_log_index" -H 'Content-Type: application/json' -d @producer_message_log_mapping.json

# 创建消费者推送日志索引
curl -X PUT "localhost:9200/consumer_push_log_index" -H 'Content-Type: application/json' -d @consumer_push_log_mapping.json
```

## 性能优化建议

### MySQL优化

1. **分区策略**：
   - producer_message_log: 按created_time月度分区
   - consumer_push_log: 按push_time月度分区

2. **索引优化**：
   - 定期分析表，更新统计信息
   - 监控慢查询日志，优化查询语句

3. **数据归档**：
   - 定期归档6个月以前的历史数据
   - 使用历史库存储归档数据

### Elasticsearch优化

1. **索引策略**：
   - 使用索引别名，便于滚动更新
   - 配置ILM策略，自动管理索引生命周期

2. **查询优化**：
   - 使用过滤器(filter)而非查询(query)提升性能
   - 合理设置分页大小，避免深度分页

3. **存储优化**：
   - 定期执行force merge
   - 冷数据使用冷存储节点

## 注意事项

1. **数据一致性**：MySQL和ES之间的数据需要保持一致性，建议使用消息队列或定时同步机制

2. **安全性**：
   - 敏感字段加密存储
   - 定期备份数据库
   - 配置访问权限控制

3. **监控告警**：
   - 监控数据库连接池
   - 监控ES集群状态
   - 配置异常告警

4. **扩展性**：
   - 预留字段扩展空间
   - 考虑分库分表策略
   - ES集群支持横向扩展
