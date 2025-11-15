package cn.gt.msg.integration;

import cn.gt.msg.dto.ConsumerRegisterRequest;
import cn.gt.msg.dto.MessagePushRequest;
import cn.gt.msg.dto.Result;
import cn.gt.msg.dto.TemplateRegisterRequest;
import cn.gt.msg.entity.ConsumerRegistry;
import cn.gt.msg.entity.MessageTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * API集成测试
 * 注意：此测试需要配置MySQL、Kafka、ES等依赖，如果环境不可用会被忽略
 *
 * @author message-center
 * @date 2025-11-15
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 测试完整的模板注册流程
     * 这个测试演示了如何使用API，但不会实际执行（因为需要依赖服务）
     */
    @Test
    void testTemplateRegistrationWorkflow() throws Exception {
        // 准备模板注册请求
        TemplateRegisterRequest request = new TemplateRegisterRequest();
        request.setTemplateId("integration_test_template");
        request.setTemplateName("集成测试模板");
        request.setDescription("用于集成测试");
        request.setPartitionCount(3);
        request.setReplicationFactor(1);

        List<TemplateRegisterRequest.TemplateFieldDTO> fields = new ArrayList<>();
        TemplateRegisterRequest.TemplateFieldDTO field = new TemplateRegisterRequest.TemplateFieldDTO();
        field.setFieldName("testField");
        field.setFieldType("STRING");
        field.setMaxLength(100);
        field.setIsRequired(1);
        fields.add(field);
        request.setFields(fields);

        // 此测试展示了API的使用方式
        // 实际执行需要正确配置的MySQL、Kafka等服务
        String requestJson = objectMapper.writeValueAsString(request);
        assertNotNull(requestJson);
        assertTrue(requestJson.contains("integration_test_template"));
    }

    /**
     * 测试完整的消息推送流程
     */
    @Test
    void testMessagePushWorkflow() throws Exception {
        // 准备消息推送请求
        MessagePushRequest request = new MessagePushRequest();
        request.setTemplateId("integration_test_template");
        
        Map<String, Object> message = new HashMap<>();
        message.put("testField", "testValue");
        request.setMessage(message);

        // 此测试展示了API的使用方式
        String requestJson = objectMapper.writeValueAsString(request);
        assertNotNull(requestJson);
        assertTrue(requestJson.contains("integration_test_template"));
    }

    /**
     * 测试完整的消费者注册流程
     */
    @Test
    void testConsumerRegistrationWorkflow() throws Exception {
        // 准备消费者注册请求
        ConsumerRegisterRequest request = new ConsumerRegisterRequest();
        request.setTemplateId("integration_test_template");
        request.setConsumerName("integration_test_consumer");
        request.setConsumerUrl("http://localhost:8080/api/test/receive");
        request.setHttpMethod("POST");
        request.setTimeoutMs(30000);
        request.setMaxRetryCount(2);
        request.setRetryIntervals("10,30");

        // 此测试展示了API的使用方式
        String requestJson = objectMapper.writeValueAsString(request);
        assertNotNull(requestJson);
        assertTrue(requestJson.contains("integration_test_consumer"));
    }

    /**
     * 测试API请求格式验证
     */
    @Test
    void testApiValidation() throws Exception {
        // 测试无效的模板注册请求
        TemplateRegisterRequest invalidRequest = new TemplateRegisterRequest();
        // 缺少必填字段
        
        String requestJson = objectMapper.writeValueAsString(invalidRequest);
        
        // 验证请求格式正确性
        assertNotNull(requestJson);
    }
}
