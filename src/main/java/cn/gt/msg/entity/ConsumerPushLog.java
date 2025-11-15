package cn.gt.msg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 消费者推送日志实体类
 * 用于记录消息中心向消费者推送消息的日志
 * 注意：此表记录基础信息，详细日志存储在Elasticsearch中
 *
 * @author message-center
 * @date 2025-11-15
 */
@Data
@TableName("consumer_push_log")
public class ConsumerPushLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 消息ID
     */
    @TableField("message_id")
    private String messageId;

    /**
     * 模板ID
     */
    @TableField("template_id")
    private String templateId;

    /**
     * 消费者ID
     */
    @TableField("consumer_id")
    private Long consumerId;

    /**
     * 消费者名称
     */
    @TableField("consumer_name")
    private String consumerName;

    /**
     * 消费者URL
     */
    @TableField("consumer_url")
    private String consumerUrl;

    /**
     * 推送请求体(JSON格式，简要信息)
     */
    @TableField("request_body")
    private String requestBody;

    /**
     * 消费者响应体(简要信息)
     */
    @TableField("response_body")
    private String responseBody;

    /**
     * HTTP状态码
     */
    @TableField("http_status")
    private Integer httpStatus;

    /**
     * 推送状态：0-失败，1-成功，2-重试中
     */
    @TableField("status")
    private Integer status;

    /**
     * 重试次数
     */
    @TableField("retry_count")
    private Integer retryCount;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 推送时间
     */
    @TableField("push_time")
    private Date pushTime;

    /**
     * 下次重试时间
     */
    @TableField("next_retry_time")
    private Date nextRetryTime;

    /**
     * 完成时间
     */
    @TableField("completed_time")
    private Date completedTime;
}
