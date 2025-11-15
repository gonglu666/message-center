package cn.gt.msg.controller;

import cn.gt.msg.dto.Result;
import cn.gt.msg.dto.TemplateRegisterRequest;
import cn.gt.msg.entity.MessageTemplate;
import cn.gt.msg.entity.TemplateField;
import cn.gt.msg.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模板管理控制器
 *
 * @author message-center
 * @date 2025-11-15
 */
@Slf4j
@RestController
@RequestMapping("/api/template")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    /**
     * 注册消息模板
     */
    @PostMapping("/register")
    public Result<Void> registerTemplate(@Validated @RequestBody TemplateRegisterRequest request) {
        try {
            log.info("接收到模板注册请求: {}", request.getTemplateId());
            templateService.registerTemplate(request);
            return Result.success();
        } catch (Exception e) {
            log.error("模板注册失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询模板列表
     */
    @GetMapping("/list")
    public Result<List<MessageTemplate>> listTemplates(
            @RequestParam(required = false) String templateName,
            @RequestParam(required = false) Integer status) {
        try {
            List<MessageTemplate> templates = templateService.listTemplates(templateName, status);
            return Result.success(templates);
        } catch (Exception e) {
            log.error("查询模板列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询模板详情
     */
    @GetMapping("/detail/{templateId}")
    public Result<MessageTemplate> getTemplateDetail(@PathVariable String templateId) {
        try {
            MessageTemplate template = templateService.getTemplateByTemplateId(templateId);
            if (template == null) {
                return Result.error(404, "模板不存在");
            }
            return Result.success(template);
        } catch (Exception e) {
            log.error("查询模板详情失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询模板字段
     */
    @GetMapping("/fields/{templateId}")
    public Result<List<TemplateField>> getTemplateFields(@PathVariable String templateId) {
        try {
            List<TemplateField> fields = templateService.getTemplateFields(templateId);
            return Result.success(fields);
        } catch (Exception e) {
            log.error("查询模板字段失败", e);
            return Result.error(e.getMessage());
        }
    }
}
