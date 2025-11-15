package cn.gt.msg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 消息模板实体类
 * 用于存储消息模板的基本信息和Kafka主题配置
 *
 * @author message-center
 * @date 2025-11-15
 */
@Data
@TableName("message_template")
public class MessageTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板唯一标识
     */
    @TableField("template_id")
    private String templateId;

    /**
     * 模板名称
     */
    @TableField("template_name")
    private String templateName;

    /**
     * Kafka主题名称
     */
    @TableField("topic_name")
    private String topicName;

    /**
     * 模板描述
     */
    @TableField("description")
    private String description;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 分区数量
     */
    @TableField("partition_count")
    private Integer partitionCount;

    /**
     * 副本因子
     */
    @TableField("replication_factor")
    private Integer replicationFactor;

    /**
     * 消息保留时间(ms)
     */
    @TableField("retention_ms")
    private Long retentionMs;

    /**
     * 创建人
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
