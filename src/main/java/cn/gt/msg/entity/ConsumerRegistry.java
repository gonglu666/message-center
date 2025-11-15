package cn.gt.msg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 消费者注册实体类
 * 用于存储消费者的注册信息和推送配置
 *
 * @author message-center
 * @date 2025-11-15
 */
@Data
@TableName("consumer_registry")
public class ConsumerRegistry implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板ID
     */
    @TableField("template_id")
    private String templateId;

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
     * HTTP方法
     */
    @TableField("http_method")
    private String httpMethod;

    /**
     * 请求头(JSON格式)
     */
    @TableField("headers")
    private String headers;

    /**
     * 超时时间(ms)
     */
    @TableField("timeout_ms")
    private Integer timeoutMs;

    /**
     * 重试次数
     */
    @TableField("retry_count")
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    @TableField("max_retry_count")
    private Integer maxRetryCount;

    /**
     * 重试间隔配置
     */
    @TableField("retry_intervals")
    private String retryIntervals;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * Kafka消费者组ID
     */
    @TableField("group_id")
    private String groupId;

    /**
     * 注册人
     */
    @TableField("created_by")
    private String createdBy;

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
