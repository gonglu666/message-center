package cn.gt.msg.controller;

import cn.gt.msg.dto.Result;
import cn.gt.msg.dto.TemplateRegisterRequest;
import cn.gt.msg.entity.MessageTemplate;
import cn.gt.msg.entity.TemplateField;
import cn.gt.msg.service.TemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 模板控制器单元测试
 *
 * @author message-center
 * @date 2025-11-15
 */
@WebMvcTest(TemplateController.class)
class TemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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

        List<TemplateRegisterRequest.TemplateFieldDTO> fields = new ArrayList<>();
        TemplateRegisterRequest.TemplateFieldDTO field = new TemplateRegisterRequest.TemplateFieldDTO();
        field.setFieldName("orderId");
        field.setFieldType("STRING");
        field.setMaxLength(64);
        field.setIsRequired(1);
        fields.add(field);
        request.setFields(fields);
    }

    @Test
    void testRegisterTemplate_Success() throws Exception {
        // Given
        doNothing().when(templateService).registerTemplate(any(TemplateRegisterRequest.class));

        // When & Then
        mockMvc.perform(post("/api/template/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"));

        verify(templateService).registerTemplate(any(TemplateRegisterRequest.class));
    }

    @Test
    void testRegisterTemplate_ValidationFailed() throws Exception {
        // Given - 缺少必填字段
        request.setTemplateId(null);

        // When & Then
        mockMvc.perform(post("/api/template/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(templateService, never()).registerTemplate(any());
    }

    @Test
    void testListTemplates() throws Exception {
        // Given
        List<MessageTemplate> templates = new ArrayList<>();
        MessageTemplate template = new MessageTemplate();
        template.setTemplateId("test_template");
        template.setTemplateName("测试模板");
        templates.add(template);

        when(templateService.listTemplates(anyString(), any())).thenReturn(templates);

        // When & Then
        mockMvc.perform(get("/api/template/list")
                        .param("templateName", "测试")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].templateId").value("test_template"));

        verify(templateService).listTemplates("测试", 1);
    }

    @Test
    void testGetTemplateDetail() throws Exception {
        // Given
        MessageTemplate template = new MessageTemplate();
        template.setTemplateId("test_template");
        template.setTemplateName("测试模板");

        when(templateService.getTemplateByTemplateId("test_template")).thenReturn(template);

        // When & Then
        mockMvc.perform(get("/api/template/detail/{templateId}", "test_template"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.templateId").value("test_template"))
                .andExpect(jsonPath("$.data.templateName").value("测试模板"));

        verify(templateService).getTemplateByTemplateId("test_template");
    }

    @Test
    void testGetTemplateDetail_NotFound() throws Exception {
        // Given
        when(templateService.getTemplateByTemplateId("test_template")).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/template/detail/{templateId}", "test_template"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("模板不存在"));

        verify(templateService).getTemplateByTemplateId("test_template");
    }

    @Test
    void testGetTemplateFields() throws Exception {
        // Given
        List<TemplateField> fields = new ArrayList<>();
        TemplateField field = new TemplateField();
        field.setFieldName("orderId");
        field.setFieldType("STRING");
        fields.add(field);

        when(templateService.getTemplateFields("test_template")).thenReturn(fields);

        // When & Then
        mockMvc.perform(get("/api/template/fields/{templateId}", "test_template"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].fieldName").value("orderId"));

        verify(templateService).getTemplateFields("test_template");
    }
}
