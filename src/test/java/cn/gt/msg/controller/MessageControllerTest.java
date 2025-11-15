package cn.gt.msg.controller;

import cn.gt.msg.dto.MessagePushRequest;
import cn.gt.msg.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 消息控制器单元测试
 *
 * @author message-center
 * @date 2025-11-15
 */
@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageService messageService;

    private MessagePushRequest request;

    @BeforeEach
    void setUp() {
        request = new MessagePushRequest();
        request.setTemplateId("test_template");
        Map<String, Object> message = new HashMap<>();
        message.put("orderId", "202411150001");
        message.put("orderStatus", "PAID");
        request.setMessage(message);
    }

    @Test
    void testPushMessage_Success() throws Exception {
        // Given
        String messageId = "test_message_id_12345";
        when(messageService.pushMessage(any(MessagePushRequest.class))).thenReturn(messageId);

        // When & Then
        mockMvc.perform(post("/api/message/push")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data").value(messageId));

        verify(messageService).pushMessage(any(MessagePushRequest.class));
    }

    @Test
    void testPushMessage_ValidationFailed_MissingTemplateId() throws Exception {
        // Given
        request.setTemplateId(null);

        // When & Then
        mockMvc.perform(post("/api/message/push")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(messageService, never()).pushMessage(any());
    }

    @Test
    void testPushMessage_ValidationFailed_MissingMessage() throws Exception {
        // Given
        request.setMessage(null);

        // When & Then
        mockMvc.perform(post("/api/message/push")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(messageService, never()).pushMessage(any());
    }

    @Test
    void testPushMessage_ServiceError() throws Exception {
        // Given
        when(messageService.pushMessage(any(MessagePushRequest.class)))
                .thenThrow(new RuntimeException("模板不存在"));

        // When & Then
        mockMvc.perform(post("/api/message/push")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("模板不存在"));

        verify(messageService).pushMessage(any(MessagePushRequest.class));
    }
}
