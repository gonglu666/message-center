package cn.gt.msg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 失败消息重试实体类
 * 用于记录需要重试的失败消息
 *
 * @author message-center
 * @date 2025-11-15
 */
@Data
@TableName("failed_message_retry")
public class FailedMessageRetry implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 推送日志ID
     */
    @TableField("push_log_id")
    private Long pushLogId;

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
     * 当前重试次数
     */
    @TableField("retry_count")
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    @TableField("max_retry_count")
    private Integer maxRetryCount;

    /**
     * 下次重试时间
     */
    @TableField("next_retry_time")
    private Date nextRetryTime;

    /**
     * 重试间隔配置(JSON格式)
     */
    @TableField("retry_intervals")
    private String retryIntervals;

    /**
     * 状态：0-待重试，1-重试成功，2-重试失败
     */
    @TableField("status")
    private Integer status;

    /**
     * 最后错误信息
     */
    @TableField("last_error_message")
    private String lastErrorMessage;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private Date createdTime;

    /**
     * 更新时间
     */
    @TableField("updated_time")
    private Date updatedTime;
}
