package cn.gt.msg.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 消费者注册请求DTO
 *
 * @author message-center
 * @date 2025-11-15
 */
@Data
public class ConsumerRegisterRequest {

    /**
     * 模板ID
     */
    @NotBlank(message = "模板ID不能为空")
    private String templateId;

    /**
     * 消费者名称
     */
    @NotBlank(message = "消费者名称不能为空")
    private String consumerName;

    /**
     * 消费者URL
     */
    @NotBlank(message = "消费者URL不能为空")
    private String consumerUrl;

    /**
     * HTTP请求方法
     */
    private String httpMethod = "POST";

    /**
     * 超时时间(ms)
     */
    private Integer timeoutMs = 30000;

    /**
     * 请求头
     */
    private String headers;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount = 2;

    /**
     * 重试间隔配置
     */
    private String retryIntervals = "10,30";
}
