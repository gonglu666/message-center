package cn.gt.msg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 模板字段定义实体类
 * 用于存储模板的字段定义和校验规则
 *
 * @author message-center
 * @date 2025-11-15
 */
@Data
@TableName("template_field")
public class TemplateField implements Serializable {

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
     * 字段名称
     */
    @TableField("field_name")
    private String fieldName;

    /**
     * 字段别名
     */
    @TableField("field_alias")
    private String fieldAlias;

    /**
     * 字段类型：STRING, INTEGER, LONG, DOUBLE, BOOLEAN, DATE, OBJECT, ARRAY
     */
    @TableField("field_type")
    private String fieldType;

    /**
     * 数据格式，如日期格式
     */
    @TableField("data_format")
    private String dataFormat;

    /**
     * 最大长度
     */
    @TableField("max_length")
    private Integer maxLength;

    /**
     * 是否必填：0-否，1-是
     */
    @TableField("is_required")
    private Integer isRequired;

    /**
     * 默认值
     */
    @TableField("default_value")
    private String defaultValue;

    /**
     * 字段描述
     */
    @TableField("description")
    private String description;

    /**
     * 排序字段
     */
    @TableField("sort_order")
    private Integer sortOrder;

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
