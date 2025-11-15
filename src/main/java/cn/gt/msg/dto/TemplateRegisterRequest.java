package cn.gt.msg.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 模板注册请求DTO
 *
 * @author message-center
 * @date 2025-11-15
 */
@Data
public class TemplateRegisterRequest {

    /**
     * 模板唯一标识
     */
    @NotBlank(message = "模板ID不能为空")
    private String templateId;

    /**
     * 模板名称
     */
    @NotBlank(message = "模板名称不能为空")
    private String templateName;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 分区数量
     */
    private Integer partitionCount = 3;

    /**
     * 副本因子
     */
    private Integer replicationFactor = 1;

    /**
     * 消息保留时间(ms)
     */
    private Long retentionMs = 604800000L; // 默认7天

    /**
     * 模板字段列表
     */
    @NotEmpty(message = "模板字段不能为空")
    private List<TemplateFieldDTO> fields;

    @Data
    public static class TemplateFieldDTO {
        /**
         * 字段名称
         */
        @NotBlank(message = "字段名称不能为空")
        private String fieldName;

        /**
         * 字段类型
         */
        @NotBlank(message = "字段类型不能为空")
        private String fieldType;

        /**
         * 最大长度
         */
        private Integer maxLength;

        /**
         * 是否必填：0-否，1-是
         */
        private Integer isRequired = 0;

        /**
         * 数据格式（正则表达式）
         */
        private String dataFormat;

        /**
         * 字段描述
         */
        private String description;
    }
}
