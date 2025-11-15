package cn.gt.msg.service;

import cn.gt.msg.dto.MessagePushRequest;
import cn.gt.msg.entity.MessageTemplate;
import cn.gt.msg.entity.TemplateField;
import cn.gt.msg.mapper.ProducerMessageLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 消息服务单元测试
 *
 * @author message-center
 * @date 2025-11-15
 */
@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private TemplateService templateService;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @Mock
    private ProducerMessageLogMapper producerMessageLogMapper;

    @Mock
    private ElasticsearchService elasticsearchService;

    @InjectMocks
    private MessageService messageService;

    private MessagePushRequest request;
    private MessageTemplate template;
    private List<TemplateField> fields;

    @BeforeEach
    void setUp() {
        request = new MessagePushRequest();
        request.setTemplateId("test_template");
        Map<String, Object> message = new HashMap<>();
        message.put("orderId", "202411150001");
        message.put("orderStatus", "PAID");
        request.setMessage(message);

        template = new MessageTemplate();
        template.setTemplateId("test_template");
        template.setTemplateName("测试模板");
        template.setTopicName("msg_test_template");
        template.setStatus(1);

        fields = new ArrayList<>();
        TemplateField field1 = new TemplateField();
        field1.setFieldName("orderId");
        field1.setFieldType("STRING");
        field1.setMaxLength(64);
        field1.setIsRequired(1);
        fields.add(field1);

        TemplateField field2 = new TemplateField();
        field2.setFieldName("orderStatus");
        field2.setFieldType("STRING");
        field2.setMaxLength(32);
        field2.setIsRequired(1);
        fields.add(field2);
    }

    @Test
    void testPushMessage_Success() {
        // Given
        when(templateService.getTemplateByTemplateId("test_template")).thenReturn(template);
        when(templateService.getTemplateFields("test_template")).thenReturn(fields);
        when(producerMessageLogMapper.insert(any())).thenReturn(1);
        doNothing().when(elasticsearchService).saveProducerLog(anyString(), anyString(), anyString(), anyString(), any());
        doNothing().when(kafkaProducerService).sendMessage(anyString(), anyString(), any());

        // When
        String messageId = messageService.pushMessage(request);

        // Then
        assertNotNull(messageId);
        assertFalse(messageId.isEmpty());
        verify(kafkaProducerService).sendMessage(eq("msg_test_template"), anyString(), any());
        verify(producerMessageLogMapper).insert(any());
    }

    @Test
    void testPushMessage_TemplateNotFound() {
        // Given
        when(templateService.getTemplateByTemplateId("test_template")).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> messageService.pushMessage(request));
        assertTrue(exception.getMessage().contains("模板不存在"));
        verify(kafkaProducerService, never()).sendMessage(anyString(), anyString(), any());
    }

    @Test
    void testPushMessage_TemplateDisabled() {
        // Given
        template.setStatus(0); // 禁用
        when(templateService.getTemplateByTemplateId("test_template")).thenReturn(template);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> messageService.pushMessage(request));
        assertTrue(exception.getMessage().contains("模板已禁用"));
        verify(kafkaProducerService, never()).sendMessage(anyString(), anyString(), any());
    }

    @Test
    void testPushMessage_ValidationFailed_RequiredFieldMissing() {
        // Given
        request.getMessage().remove("orderId"); // 移除必填字段
        when(templateService.getTemplateByTemplateId("test_template")).thenReturn(template);
        when(templateService.getTemplateFields("test_template")).thenReturn(fields);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> messageService.pushMessage(request));
        assertTrue(exception.getMessage().contains("不能为空"));
        verify(kafkaProducerService, never()).sendMessage(anyString(), anyString(), any());
    }

    @Test
    void testPushMessage_ValidationFailed_MaxLengthExceeded() {
        // Given
        request.getMessage().put("orderId", "a".repeat(100)); // 超过最大长度64
        when(templateService.getTemplateByTemplateId("test_template")).thenReturn(template);
        when(templateService.getTemplateFields("test_template")).thenReturn(fields);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> messageService.pushMessage(request));
        assertTrue(exception.getMessage().contains("长度超过限制"));
        verify(kafkaProducerService, never()).sendMessage(anyString(), anyString(), any());
    }
}
