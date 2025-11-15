package cn.gt.msg.service;

import cn.gt.msg.dto.TemplateRegisterRequest;
import cn.gt.msg.entity.MessageTemplate;
import cn.gt.msg.entity.TemplateField;
import cn.gt.msg.mapper.MessageTemplateMapper;
import cn.gt.msg.mapper.TemplateFieldMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 模板服务类
 *
 * @author message-center
 * @date 2025-11-15
 */
@Slf4j
@Service
public class TemplateService {

    @Autowired
    private MessageTemplateMapper messageTemplateMapper;

    @Autowired
    private TemplateFieldMapper templateFieldMapper;

    @Autowired
    private KafkaAdminService kafkaAdminService;

    /**
     * 注册消息模板
     */
    @Transactional(rollbackFor = Exception.class)
    public void registerTemplate(TemplateRegisterRequest request) {
        log.info("开始注册模板: {}", request.getTemplateId());

        // 检查模板是否已存在
        MessageTemplate existTemplate = messageTemplateMapper.selectOne(
                new LambdaQueryWrapper<MessageTemplate>()
                        .eq(MessageTemplate::getTemplateId, request.getTemplateId())
        );
        if (existTemplate != null) {
            throw new RuntimeException("模板ID已存在: " + request.getTemplateId());
        }

        // 创建Kafka主题
        String topicName = "msg_" + request.getTemplateId();
        kafkaAdminService.createTopic(topicName, request.getPartitionCount(), request.getReplicationFactor());

        // 保存模板信息
        MessageTemplate template = new MessageTemplate();
        template.setTemplateId(request.getTemplateId());
        template.setTemplateName(request.getTemplateName());
        template.setTopicName(topicName);
        template.setDescription(request.getDescription());
        template.setStatus(1);
        template.setPartitionCount(request.getPartitionCount());
        template.setReplicationFactor(request.getReplicationFactor());
        template.setRetentionMs(request.getRetentionMs());
        template.setCreatedBy("system");
        template.setCreatedTime(new Date());
        template.setUpdatedTime(new Date());
        messageTemplateMapper.insert(template);

        // 保存字段信息
        List<TemplateField> fields = request.getFields().stream().map(fieldDTO -> {
            TemplateField field = new TemplateField();
            field.setTemplateId(request.getTemplateId());
            field.setFieldName(fieldDTO.getFieldName());
            field.setFieldType(fieldDTO.getFieldType());
            field.setMaxLength(fieldDTO.getMaxLength());
            field.setIsRequired(fieldDTO.getIsRequired());
            field.setDataFormat(fieldDTO.getDataFormat());
            field.setDescription(fieldDTO.getDescription());
            field.setCreatedTime(new Date());
            return field;
        }).collect(Collectors.toList());

        fields.forEach(templateFieldMapper::insert);

        log.info("模板注册成功: {}", request.getTemplateId());
    }

    /**
     * 查询模板列表
     */
    public List<MessageTemplate> listTemplates(String templateName, Integer status) {
        LambdaQueryWrapper<MessageTemplate> wrapper = new LambdaQueryWrapper<>();
        if (templateName != null && !templateName.isEmpty()) {
            wrapper.like(MessageTemplate::getTemplateName, templateName);
        }
        if (status != null) {
            wrapper.eq(MessageTemplate::getStatus, status);
        }
        wrapper.orderByDesc(MessageTemplate::getCreatedTime);
        return messageTemplateMapper.selectList(wrapper);
    }

    /**
     * 查询模板字段
     */
    public List<TemplateField> getTemplateFields(String templateId) {
        return templateFieldMapper.selectList(
                new LambdaQueryWrapper<TemplateField>()
                        .eq(TemplateField::getTemplateId, templateId)
                        .orderByAsc(TemplateField::getId)
        );
    }

    /**
     * 根据模板ID查询模板
     */
    public MessageTemplate getTemplateByTemplateId(String templateId) {
        return messageTemplateMapper.selectOne(
                new LambdaQueryWrapper<MessageTemplate>()
                        .eq(MessageTemplate::getTemplateId, templateId)
        );
    }
}
