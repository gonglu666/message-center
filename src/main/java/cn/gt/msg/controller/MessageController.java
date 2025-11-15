package cn.gt.msg.controller;

import cn.gt.msg.dto.MessagePushRequest;
import cn.gt.msg.dto.Result;
import cn.gt.msg.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息推送控制器
 *
 * @author message-center
 * @date 2025-11-15
 */
@Slf4j
@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 推送消息
     */
    @PostMapping("/push")
    public Result<String> pushMessage(@Validated @RequestBody MessagePushRequest request) {
        try {
            log.info("接收到消息推送请求, templateId: {}", request.getTemplateId());
            String messageId = messageService.pushMessage(request);
            return Result.success(messageId);
        } catch (Exception e) {
            log.error("消息推送失败", e);
            return Result.error(e.getMessage());
        }
    }
}
