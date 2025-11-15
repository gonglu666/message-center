package cn.gt.msg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 生产者消息日志实体类
 * 用于记录生产者推送消息的日志信息
 * 注意：此表记录基础信息，详细日志存储在Elasticsearch中
 *
 * @author message-center
 * @date 2025-11-15
 */
@Data
@TableName("producer_message_log")
public class ProducerMessageLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 消息唯一ID
     */
    @TableField("message_id")
    private String messageId;

    /**
     * 模板ID
     */
    @TableField("template_id")
    private String templateId;

    /**
     * 主题名称
     */
    @TableField("topic_name")
    private String topicName;

    /**
     * 消息Key
     */
    @TableField("message_key")
    private String messageKey;

    /**
     * 生产者应用
     */
    @TableField("producer_app")
    private String producerApp;

    /**
     * 原始请求体(JSON格式，简要信息)
     */
    @TableField("request_body")
    private String requestBody;

    /**
     * 返回结果(JSON格式，简要信息)
     */
    @TableField("response_result")
    private String responseResult;

    /**
     * 状态：0-失败，1-成功
     */
    @TableField("status")
    private Integer status;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * Kafka偏移量
     */
    @TableField("kafka_offset")
    private Long kafkaOffset;

    /**
     * Kafka分区
     */
    @TableField("kafka_partition")
    private Integer kafkaPartition;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private Date createdTime;
}
