# 消息中心 (Message Center)

基于 Spring Boot 2.7.10 和 Kafka 的消息中心系统，用于降低开发人员使用 Kafka 的学习成本。

## 项目概述

消息中心是一个企业级消息管理平台，提供统一的消息模板管理、消息推送、消费者注册和失败重试等功能，让业务系统无需直接操作 Kafka 即可实现可靠的消息传递。

## 核心功能

1. **模板注册 API** - 创建消息模板，自动创建 Kafka 主题，支持自定义字段及校验规则
2. **消息推送 API** - 向指定模板推送消息，自动校验消息内容
3. **消费者注册 API** - 注册消费者 URL，自动推送新消息到业务系统
4. **生产者日志** - 记录生产者推送的消息和返回结果
5. **消费者日志** - 记录向消费者推送的消息和响应结果
6. **失败重试机制** - 自动重试失败的推送，支持自定义重试间隔
7. **Apollo 配置管理** - 所有配置项可通过 Apollo 统一管理
8. **查询修改 API** - 提供条件查询和修改已注册的模板、字段和消费者
9. **ES 日志存储** - 庞大的日志数据存储到 Elasticsearch，支持高效查询

## 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.10 | 基础框架 |
| JDK | 1.8 | Java 版本 |
| MyBatis-Plus | 3.5.3.1 | ORM 框架 |
| MySQL | 8.0+ | 关系型数据库 |
| Kafka | 兼容 2.7.10 | 消息队列 |
| Redis | 兼容 2.7.10 | 缓存 |
| Elasticsearch | 7.17.9 | 日志存储和检索 |
| Apollo | 2.0.1 | 配置中心 |
| Druid | 1.2.16 | 数据库连接池 |
| Lombok | - | 简化实体类编写 |

## 项目结构

```
message-center/
├── src/
│   └── main/
│       ├── java/
│       │   └── cn/gt/msg/
│       │       ├── entity/              # 实体类
│       │       │   ├── MessageTemplate.java
│       │       │   ├── TemplateField.java
│       │       │   ├── ConsumerRegistry.java
│       │       │   ├── ProducerMessageLog.java
│       │       │   ├── ConsumerPushLog.java
│       │       │   ├── RetryConfig.java
│       │       │   ├── FailedMessageRetry.java
│       │       │   └── SystemConfig.java
│       │       └── MessageApplication.java
│       └── resources/
│           ├── db/
│           │   ├── schema/              # 数据库表结构
│           │   │   ├── 01_core_tables.sql
│           │   │   ├── 02_log_tables.sql
│           │   │   ├── 03_retry_tables.sql
│           │   │   └── 04_system_tables.sql
│           │   ├── data/                # 初始化数据
│           │   │   └── 01_init_retry_config.sql
│           │   └── README.md            # 数据库设计文档
│           ├── elasticsearch/           # ES 索引映射
│           │   ├── producer_message_log_mapping.json
│           │   ├── consumer_push_log_mapping.json
│           │   └── ilm_policy.json
│           └── application.yml          # 应用配置
├── pom.xml
└── README.md
```

## 数据库设计

### MySQL 表结构

#### 1. 核心配置表

- **message_template** - 消息模板表，存储模板基本信息和 Kafka 主题配置
- **template_field** - 模板字段定义表，存储字段定义和校验规则
- **consumer_registry** - 消费者注册表，存储消费者信息和推送配置

#### 2. 消息日志表

- **producer_message_log** - 生产者消息日志表，记录推送消息的基础信息
- **consumer_push_log** - 消费者推送日志表，记录向消费者推送的基础信息

#### 3. 重试任务表

- **retry_config** - 重试配置表，存储重试机制的配置参数
- **failed_message_retry** - 失败消息重试表，记录需要重试的失败消息

#### 4. 系统配置表

- **system_config** - 系统配置表，存储系统级别的配置信息

详细的数据库设计文档请参考：[数据库设计文档](src/main/resources/db/README.md)

### Elasticsearch 索引

- **producer_message_log_index** - 存储生产者推送消息的完整日志
- **consumer_push_log_index** - 存储消费者推送消息的完整日志

## 快速开始

### 1. 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Kafka 2.8+
- Redis 6.0+
- Elasticsearch 7.17+
- Apollo Config Server (可选)

### 2. 创建数据库

```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE IF NOT EXISTS message_center 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_general_ci;
```

### 3. 执行数据库脚本

```bash
# 创建表结构
mysql -u root -p message_center < src/main/resources/db/schema/01_core_tables.sql
mysql -u root -p message_center < src/main/resources/db/schema/02_log_tables.sql
mysql -u root -p message_center < src/main/resources/db/schema/03_retry_tables.sql
mysql -u root -p message_center < src/main/resources/db/schema/04_system_tables.sql

# 初始化数据
mysql -u root -p message_center < src/main/resources/db/data/01_init_retry_config.sql
```

### 4. 创建 Elasticsearch 索引

```bash
# 创建生产者日志索引
curl -X PUT "localhost:9200/producer_message_log_index" \
  -H 'Content-Type: application/json' \
  -d @src/main/resources/elasticsearch/producer_message_log_mapping.json

# 创建消费者推送日志索引
curl -X PUT "localhost:9200/consumer_push_log_index" \
  -H 'Content-Type: application/json' \
  -d @src/main/resources/elasticsearch/consumer_push_log_mapping.json

# 创建索引生命周期策略
curl -X PUT "localhost:9200/_ilm/policy/message-log-policy" \
  -H 'Content-Type: application/json' \
  -d @src/main/resources/elasticsearch/ilm_policy.json
```

### 5. 配置应用

修改 `src/main/resources/application.yml` 中的配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/message_center
    username: your_username
    password: your_password
  
  kafka:
    bootstrap-servers: localhost:9092
  
  redis:
    host: localhost
    port: 6379
  
  elasticsearch:
    uris: http://localhost:9200

apollo:
  meta: http://localhost:8080
```

### 6. 编译运行

```bash
# 编译项目
mvn clean package -DskipTests

# 运行项目
java -jar target/message-center-0.0.1.jar
```

或者使用 Spring Boot Maven 插件：

```bash
mvn spring-boot:run
```

## API 接口说明

### 1. 模板注册 API

**接口**: `POST /api/template/register`

**请求体**:
```json
{
  "templateId": "order_notification",
  "templateName": "订单通知模板",
  "description": "用于订单状态变更通知",
  "partitionCount": 3,
  "replicationFactor": 1,
  "fields": [
    {
      "fieldName": "orderId",
      "fieldType": "STRING",
      "maxLength": 64,
      "isRequired": 1,
      "description": "订单ID"
    },
    {
      "fieldName": "orderStatus",
      "fieldType": "STRING",
      "maxLength": 32,
      "isRequired": 1,
      "description": "订单状态"
    }
  ]
}
```

### 2. 消息推送 API

**接口**: `POST /api/message/push`

**请求体**:
```json
{
  "templateId": "order_notification",
  "message": {
    "orderId": "2024111500001",
    "orderStatus": "PAID"
  }
}
```

### 3. 消费者注册 API

**接口**: `POST /api/consumer/register`

**请求体**:
```json
{
  "templateId": "order_notification",
  "consumerName": "order_service",
  "consumerUrl": "http://order-service.example.com/api/message/receive",
  "httpMethod": "POST",
  "timeoutMs": 30000
}
```

### 4. 模板查询 API

**接口**: `GET /api/template/list`

**参数**:
- `status`: 状态筛选 (0-禁用, 1-启用)
- `templateName`: 模板名称 (模糊匹配)

### 5. 消费者查询 API

**接口**: `GET /api/consumer/list`

**参数**:
- `templateId`: 模板ID
- `status`: 状态筛选

## 配置说明

### 重试配置

系统默认配置：
- 第一次重试间隔: 10 分钟
- 第二次重试间隔: 30 分钟
- 最大重试次数: 2 次

可通过 Apollo 或 `system_config` 表修改配置：
```sql
UPDATE retry_config SET config_value = '[5, 15, 30]' WHERE config_key = 'retry.intervals';
UPDATE retry_config SET config_value = '3' WHERE config_key = 'retry.max_count';
```

### Apollo 配置

在 Apollo 中配置的参数会自动同步到系统：
- `message-center.retry.enabled` - 是否启用重试
- `message-center.retry.intervals` - 重试间隔
- `message-center.retry.max-count` - 最大重试次数

## 监控和运维

### 日志查询

#### MySQL 日志查询

```sql
-- 查询最近的生产者日志
SELECT * FROM producer_message_log 
WHERE created_time > DATE_SUB(NOW(), INTERVAL 1 DAY)
ORDER BY created_time DESC;

-- 查询失败的推送日志
SELECT * FROM consumer_push_log 
WHERE status = 0 
ORDER BY push_time DESC;
```

#### Elasticsearch 日志查询

```bash
# 查询生产者日志
curl -X GET "localhost:9200/producer_message_log_index/_search" \
  -H 'Content-Type: application/json' \
  -d '{
    "query": {
      "match": {
        "template_id": "order_notification"
      }
    }
  }'

# 查询失败的消费者推送日志
curl -X GET "localhost:9200/consumer_push_log_index/_search" \
  -H 'Content-Type: application/json' \
  -d '{
    "query": {
      "term": {
        "status": 0
      }
    }
  }'
```

### 性能优化

1. **数据库优化**
   - 定期分析表，更新统计信息
   - 按月分区历史日志表
   - 归档6个月以上的数据

2. **Elasticsearch 优化**
   - 配置 ILM 策略自动管理索引
   - 定期执行 force merge
   - 冷数据使用冷存储节点

3. **应用优化**
   - 调整线程池大小
   - 配置合适的批处理大小
   - 使用 Redis 缓存热点数据

## 故障排查

### 常见问题

1. **消息推送失败**
   - 检查 Kafka 连接状态
   - 验证模板配置是否正确
   - 查看生产者日志表

2. **消费者推送失败**
   - 检查消费者 URL 是否可达
   - 查看 consumer_push_log 表的错误信息
   - 确认重试任务是否正常执行

3. **数据库连接异常**
   - 检查连接池配置
   - 验证数据库账号权限
   - 查看慢查询日志

## 开发指南

### 添加新的 API 接口

1. 在 `controller` 包下创建控制器类
2. 在 `service` 包下创建服务类
3. 在 `mapper` 包下创建 Mapper 接口
4. 在 `resources/mapper` 下创建 XML 映射文件

### 添加新的配置项

1. 在 `system_config` 表中添加配置记录
2. 在 Apollo 中添加对应的配置项
3. 在代码中使用 `@Value` 或 `@ConfigurationProperties` 注入

## 贡献指南

欢迎提交 Issue 和 Pull Request！

## 许可证

本项目采用 MIT 许可证。

## 联系方式

如有问题或建议，请通过以下方式联系：
- Issue: https://github.com/gonglu666/message-center/issues
- Email: support@example.com
