package cn.gt.msg.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 消息推送请求DTO
 *
 * @author message-center
 * @date 2025-11-15
 */
@Data
public class MessagePushRequest {

    /**
     * 模板ID
     */
    @NotBlank(message = "模板ID不能为空")
    private String templateId;

    /**
     * 消息内容
     */
    @NotNull(message = "消息内容不能为空")
    private Map<String, Object> message;

    /**
     * 消息key（用于分区）
     */
    private String messageKey;
}
