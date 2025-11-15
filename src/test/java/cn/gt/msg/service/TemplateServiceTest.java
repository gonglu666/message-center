package cn.gt.msg.service;

import cn.gt.msg.dto.TemplateRegisterRequest;
import cn.gt.msg.entity.MessageTemplate;
import cn.gt.msg.entity.TemplateField;
import cn.gt.msg.mapper.MessageTemplateMapper;
import cn.gt.msg.mapper.TemplateFieldMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 模板服务单元测试
 *
 * @author message-center
 * @date 2025-11-15
 */
@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

    @Mock
    private MessageTemplateMapper messageTemplateMapper;

    @Mock
    private TemplateFieldMapper templateFieldMapper;

    @Mock
    private KafkaAdminService kafkaAdminService;

    @InjectMocks
    private TemplateService templateService;

    private TemplateRegisterRequest request;

    @BeforeEach
    void setUp() {
        request = new TemplateRegisterRequest();
        request.setTemplateId("test_template");
        request.setTemplateName("测试模板");
        request.setDescription("测试描述");
        request.setPartitionCount(3);
        request.setReplicationFactor(1);
        request.setRetentionMs(604800000L);

        List<TemplateRegisterRequest.TemplateFieldDTO> fields = new ArrayList<>();
        TemplateRegisterRequest.TemplateFieldDTO field = new TemplateRegisterRequest.TemplateFieldDTO();
        field.setFieldName("orderId");
        field.setFieldType("STRING");
        field.setMaxLength(64);
        field.setIsRequired(1);
        field.setDescription("订单ID");
        fields.add(field);

        request.setFields(fields);
    }

    @Test
    void testRegisterTemplate_Success() {
        // Given
        when(messageTemplateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        doNothing().when(kafkaAdminService).createTopic(anyString(), anyInt(), anyInt());
        when(messageTemplateMapper.insert(any(MessageTemplate.class))).thenReturn(1);
        when(templateFieldMapper.insert(any(TemplateField.class))).thenReturn(1);

        // When
        assertDoesNotThrow(() -> templateService.registerTemplate(request));

        // Then
        verify(kafkaAdminService).createTopic("msg_test_template", 3, 1);
        verify(messageTemplateMapper).insert(any(MessageTemplate.class));
        verify(templateFieldMapper).insert(any(TemplateField.class));
    }

    @Test
    void testRegisterTemplate_TemplateAlreadyExists() {
        // Given
        MessageTemplate existTemplate = new MessageTemplate();
        existTemplate.setTemplateId("test_template");
        when(messageTemplateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existTemplate);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> templateService.registerTemplate(request));
        assertTrue(exception.getMessage().contains("模板ID已存在"));
        verify(kafkaAdminService, never()).createTopic(anyString(), anyInt(), anyInt());
    }

    @Test
    void testListTemplates() {
        // Given
        List<MessageTemplate> templates = new ArrayList<>();
        MessageTemplate template = new MessageTemplate();
        template.setTemplateId("test_template");
        template.setTemplateName("测试模板");
        templates.add(template);

        when(messageTemplateMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(templates);

        // When
        List<MessageTemplate> result = templateService.listTemplates("测试", 1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test_template", result.get(0).getTemplateId());
        verify(messageTemplateMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetTemplateFields() {
        // Given
        List<TemplateField> fields = new ArrayList<>();
        TemplateField field = new TemplateField();
        field.setFieldName("orderId");
        field.setFieldType("STRING");
        fields.add(field);

        when(templateFieldMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(fields);

        // When
        List<TemplateField> result = templateService.getTemplateFields("test_template");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("orderId", result.get(0).getFieldName());
        verify(templateFieldMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetTemplateByTemplateId() {
        // Given
        MessageTemplate template = new MessageTemplate();
        template.setTemplateId("test_template");
        template.setTemplateName("测试模板");

        when(messageTemplateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(template);

        // When
        MessageTemplate result = templateService.getTemplateByTemplateId("test_template");

        // Then
        assertNotNull(result);
        assertEquals("test_template", result.getTemplateId());
        verify(messageTemplateMapper).selectOne(any(LambdaQueryWrapper.class));
    }
}
