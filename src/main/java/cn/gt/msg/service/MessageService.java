package cn.gt.msg.service;

import cn.gt.msg.dto.MessagePushRequest;
import cn.gt.msg.entity.MessageTemplate;
import cn.gt.msg.entity.ProducerMessageLog;
import cn.gt.msg.entity.TemplateField;
import cn.gt.msg.mapper.ProducerMessageLogMapper;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 消息服务类
 *
 * @author message-center
 * @date 2025-11-15
 */
@Slf4j
@Service
public class MessageService {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private ProducerMessageLogMapper producerMessageLogMapper;

    @Autowired
    private ElasticsearchService elasticsearchService;

    /**
     * 推送消息
     */
    @Transactional(rollbackFor = Exception.class)
    public String pushMessage(MessagePushRequest request) {
        log.info("开始推送消息, templateId: {}", request.getTemplateId());

        // 查询模板
        MessageTemplate template = templateService.getTemplateByTemplateId(request.getTemplateId());
        if (template == null) {
            throw new RuntimeException("模板不存在: " + request.getTemplateId());
        }
        if (template.getStatus() != 1) {
            throw new RuntimeException("模板已禁用: " + request.getTemplateId());
        }

        // 查询字段定义并校验消息
        List<TemplateField> fields = templateService.getTemplateFields(request.getTemplateId());
        validateMessage(request.getMessage(), fields);

        // 生成消息ID
        String messageId = UUID.randomUUID().toString().replace("-", "");
        String messageKey = request.getMessageKey() != null ? request.getMessageKey() : messageId;

        // 保存生产者日志到MySQL（基础信息）
        ProducerMessageLog logEntity = new ProducerMessageLog();
        logEntity.setMessageId(messageId);
        logEntity.setTemplateId(request.getTemplateId());
        logEntity.setTopicName(template.getTopicName());
        logEntity.setMessageKey(messageKey);
        logEntity.setStatus(1); // 1-成功
        logEntity.setCreatedTime(new Date());
        producerMessageLogMapper.insert(logEntity);

        // 异步保存完整日志到ES
        elasticsearchService.saveProducerLog(messageId, request.getTemplateId(), 
                template.getTopicName(), messageKey, request.getMessage());

        // 发送消息到Kafka
        kafkaProducerService.sendMessage(template.getTopicName(), messageKey, request.getMessage());

        log.info("消息推送成功, messageId: {}", messageId);
        return messageId;
    }

    /**
     * 校验消息内容
     */
    private void validateMessage(Map<String, Object> message, List<TemplateField> fields) {
        for (TemplateField field : fields) {
            String fieldName = field.getFieldName();
            Object value = message.get(fieldName);

            // 校验必填字段
            if (field.getIsRequired() == 1 && (value == null || value.toString().isEmpty())) {
                throw new RuntimeException("字段 " + fieldName + " 不能为空");
            }

            // 校验字段类型和长度
            if (value != null) {
                String valueStr = value.toString();
                if (field.getMaxLength() != null && valueStr.length() > field.getMaxLength()) {
                    throw new RuntimeException("字段 " + fieldName + " 长度超过限制: " + field.getMaxLength());
                }

                // 校验数据格式
                if (field.getDataFormat() != null && !field.getDataFormat().isEmpty()) {
                    if (!valueStr.matches(field.getDataFormat())) {
                        throw new RuntimeException("字段 " + fieldName + " 格式不正确");
                    }
                }
            }
        }
    }
}
