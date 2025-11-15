package cn.gt.msg.service;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Elasticsearch服务
 *
 * @author message-center
 * @date 2025-11-15
 */
@Slf4j
@Service
public class ElasticsearchService {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;

    @Value("${message-center.elasticsearch.producer-log-index:producer_message_log_index}")
    private String producerLogIndex;

    @Value("${message-center.elasticsearch.consumer-log-index:consumer_push_log_index}")
    private String consumerLogIndex;

    /**
     * 保存生产者日志到ES
     */
    @Async
    public void saveProducerLog(String messageId, String templateId, String topicName, 
                                 String messageKey, Object message) {
        try {
            Map<String, Object> document = new HashMap<>();
            document.put("message_id", messageId);
            document.put("template_id", templateId);
            document.put("topic_name", topicName);
            document.put("message_key", messageKey);
            document.put("request_body", JSON.toJSONString(message));
            document.put("status", 1);
            document.put("created_time", new Date());

            elasticsearchTemplate.save(document, IndexCoordinates.of(producerLogIndex));
            log.debug("生产者日志保存到ES成功, messageId: {}", messageId);
        } catch (Exception e) {
            log.error("保存生产者日志到ES失败, messageId: {}", messageId, e);
        }
    }

    /**
     * 保存消费者推送日志到ES
     */
    @Async
    public void saveConsumerPushLog(String messageId, Long consumerId, String consumerName,
                                     String requestBody, String responseBody, Integer status, 
                                     Integer httpStatus, String errorMsg) {
        try {
            Map<String, Object> document = new HashMap<>();
            document.put("message_id", messageId);
            document.put("consumer_id", consumerId);
            document.put("consumer_name", consumerName);
            document.put("request_body", requestBody);
            document.put("response_body", responseBody);
            document.put("status", status);
            document.put("http_status", httpStatus);
            document.put("error_message", errorMsg);
            document.put("push_time", new Date());

            elasticsearchTemplate.save(document, IndexCoordinates.of(consumerLogIndex));
            log.debug("消费者推送日志保存到ES成功, messageId: {}, consumerId: {}", messageId, consumerId);
        } catch (Exception e) {
            log.error("保存消费者推送日志到ES失败, messageId: {}, consumerId: {}", messageId, consumerId, e);
        }
    }
}
