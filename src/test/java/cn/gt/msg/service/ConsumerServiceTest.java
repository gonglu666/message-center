package cn.gt.msg.service;

import cn.gt.msg.dto.ConsumerRegisterRequest;
import cn.gt.msg.entity.ConsumerRegistry;
import cn.gt.msg.entity.MessageTemplate;
import cn.gt.msg.mapper.ConsumerRegistryMapper;
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
 * 消费者服务单元测试
 *
 * @author message-center
 * @date 2025-11-15
 */
@ExtendWith(MockitoExtension.class)
class ConsumerServiceTest {

    @Mock
    private ConsumerRegistryMapper consumerRegistryMapper;

    @Mock
    private TemplateService templateService;

    @InjectMocks
    private ConsumerService consumerService;

    private ConsumerRegisterRequest request;
    private MessageTemplate template;

    @BeforeEach
    void setUp() {
        request = new ConsumerRegisterRequest();
        request.setTemplateId("test_template");
        request.setConsumerName("test_consumer");
        request.setConsumerUrl("http://localhost:8080/api/message/receive");
        request.setHttpMethod("POST");
        request.setTimeoutMs(30000);
        request.setMaxRetryCount(2);
        request.setRetryIntervals("10,30");

        template = new MessageTemplate();
        template.setTemplateId("test_template");
        template.setTemplateName("测试模板");
    }

    @Test
    void testRegisterConsumer_Success() {
        // Given
        when(templateService.getTemplateByTemplateId("test_template")).thenReturn(template);
        when(consumerRegistryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(consumerRegistryMapper.insert(any(ConsumerRegistry.class))).thenReturn(1);

        // When
        assertDoesNotThrow(() -> consumerService.registerConsumer(request));

        // Then
        verify(consumerRegistryMapper).insert(any(ConsumerRegistry.class));
    }

    @Test
    void testRegisterConsumer_TemplateNotFound() {
        // Given
        when(templateService.getTemplateByTemplateId("test_template")).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> consumerService.registerConsumer(request));
        assertTrue(exception.getMessage().contains("模板不存在"));
        verify(consumerRegistryMapper, never()).insert(any());
    }

    @Test
    void testRegisterConsumer_ConsumerAlreadyExists() {
        // Given
        ConsumerRegistry existConsumer = new ConsumerRegistry();
        existConsumer.setConsumerName("test_consumer");
        when(templateService.getTemplateByTemplateId("test_template")).thenReturn(template);
        when(consumerRegistryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existConsumer);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> consumerService.registerConsumer(request));
        assertTrue(exception.getMessage().contains("消费者已存在"));
        verify(consumerRegistryMapper, never()).insert(any());
    }

    @Test
    void testListConsumers() {
        // Given
        List<ConsumerRegistry> consumers = new ArrayList<>();
        ConsumerRegistry consumer = new ConsumerRegistry();
        consumer.setTemplateId("test_template");
        consumer.setConsumerName("test_consumer");
        consumers.add(consumer);

        when(consumerRegistryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(consumers);

        // When
        List<ConsumerRegistry> result = consumerService.listConsumers("test_template", 1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test_consumer", result.get(0).getConsumerName());
        verify(consumerRegistryMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetActiveConsumersByTemplateId() {
        // Given
        List<ConsumerRegistry> consumers = new ArrayList<>();
        ConsumerRegistry consumer = new ConsumerRegistry();
        consumer.setTemplateId("test_template");
        consumer.setConsumerName("test_consumer");
        consumer.setStatus(1);
        consumers.add(consumer);

        when(consumerRegistryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(consumers);

        // When
        List<ConsumerRegistry> result = consumerService.getActiveConsumersByTemplateId("test_template");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getStatus());
        verify(consumerRegistryMapper).selectList(any(LambdaQueryWrapper.class));
    }
}
