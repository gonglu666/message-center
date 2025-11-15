package cn.gt.msg.service;

import cn.gt.msg.entity.ConsumerPushLog;
import cn.gt.msg.entity.ConsumerRegistry;
import cn.gt.msg.entity.FailedMessageRetry;
import cn.gt.msg.mapper.ConsumerPushLogMapper;
import cn.gt.msg.mapper.FailedMessageRetryMapper;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;

/**
 * Kafka消费者服务
 *
 * @author message-center
 * @date 2025-11-15
 */
@Slf4j
@Service
public class KafkaConsumerService {

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private ConsumerPushLogMapper consumerPushLogMapper;

    @Autowired
    private FailedMessageRetryMapper failedMessageRetryMapper;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 监听所有消息主题
     */
    @KafkaListener(topicPattern = "msg_.*", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeMessage(String message, 
                                org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record) {
        String topicName = record.topic();
        String templateId = topicName.replace("msg_", "");
        String messageKey = record.key();
        
        log.info("接收到Kafka消息, topic: {}, templateId: {}, key: {}", topicName, templateId, messageKey);

        try {
            // 查询该模板的所有启用的消费者
            List<ConsumerRegistry> consumers = consumerService.getActiveConsumersByTemplateId(templateId);
            
            if (consumers.isEmpty()) {
                log.warn("模板没有注册的消费者, templateId: {}", templateId);
                return;
            }

            // 推送消息给每个消费者
            for (ConsumerRegistry consumer : consumers) {
                pushToConsumer(consumer, message, messageKey);
            }
        } catch (Exception e) {
            log.error("处理Kafka消息异常, topic: {}, key: {}", topicName, messageKey, e);
        }
    }

    /**
     * 推送消息给消费者
     */
    private void pushToConsumer(ConsumerRegistry consumer, String message, String messageKey) {
        log.info("开始推送消息给消费者: {}, url: {}", consumer.getConsumerName(), consumer.getConsumerUrl());

        String responseBody = null;
        Integer httpStatus = null;
        Integer status = 0; // 0-失败
        String errorMsg = null;

        try {
            // 构造HTTP请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(message, headers);
            
            // 发送HTTP请求
            ResponseEntity<String> response = restTemplate.exchange(
                    consumer.getConsumerUrl(),
                    HttpMethod.valueOf(consumer.getHttpMethod()),
                    entity,
                    String.class
            );

            httpStatus = response.getStatusCodeValue();
            responseBody = response.getBody();

            if (httpStatus >= 200 && httpStatus < 300) {
                status = 1; // 1-成功
                log.info("推送消息成功, consumer: {}, httpStatus: {}", consumer.getConsumerName(), httpStatus);
            } else {
                errorMsg = "HTTP状态码异常: " + httpStatus;
                log.error("推送消息失败, consumer: {}, httpStatus: {}", consumer.getConsumerName(), httpStatus);
            }
        } catch (Exception e) {
            errorMsg = e.getMessage();
            log.error("推送消息异常, consumer: {}", consumer.getConsumerName(), e);
        }

        // 保存推送日志到MySQL（基础信息）
        ConsumerPushLog pushLog = new ConsumerPushLog();
        pushLog.setMessageId(messageKey);
        pushLog.setConsumerId(consumer.getId());
        pushLog.setConsumerName(consumer.getConsumerName());
        pushLog.setConsumerUrl(consumer.getConsumerUrl());
        pushLog.setStatus(status);
        pushLog.setHttpStatus(httpStatus);
        pushLog.setErrorMessage(errorMsg);
        pushLog.setPushTime(new Date());
        consumerPushLogMapper.insert(pushLog);

        // 异步保存完整日志到ES
        elasticsearchService.saveConsumerPushLog(messageKey, consumer.getId(), 
                consumer.getConsumerName(), message, responseBody, status, httpStatus, errorMsg);

        // 如果推送失败，记录重试任务
        if (status == 0) {
            saveRetryTask(pushLog.getId(), consumer, messageKey, message);
        }
    }

    /**
     * 保存重试任务
     */
    private void saveRetryTask(Long pushLogId, ConsumerRegistry consumer, String messageKey, String message) {
        try {
            FailedMessageRetry retry = new FailedMessageRetry();
            retry.setPushLogId(pushLogId);
            retry.setTemplateId(consumer.getTemplateId());
            retry.setConsumerId(consumer.getId());
            retry.setMessageId(messageKey);
            retry.setMessageContent(message);
            retry.setRetryCount(0);
            retry.setMaxRetryCount(consumer.getMaxRetryCount());
            retry.setRetryIntervals(consumer.getRetryIntervals());
            retry.setNextRetryTime(calculateNextRetryTime(0, consumer.getRetryIntervals()));
            retry.setStatus(0); // 0-待重试
            retry.setCreatedTime(new Date());
            retry.setUpdatedTime(new Date());
            failedMessageRetryMapper.insert(retry);

            log.info("重试任务保存成功, messageId: {}, consumerId: {}", messageKey, consumer.getId());
        } catch (Exception e) {
            log.error("保存重试任务失败, messageId: {}, consumerId: {}", messageKey, consumer.getId(), e);
        }
    }

    /**
     * 计算下次重试时间
     */
    private Date calculateNextRetryTime(int retryCount, String retryIntervals) {
        String[] intervals = retryIntervals.split(",");
        int intervalMinutes = 10; // 默认10分钟
        
        if (retryCount < intervals.length) {
            intervalMinutes = Integer.parseInt(intervals[retryCount].trim());
        } else if (intervals.length > 0) {
            intervalMinutes = Integer.parseInt(intervals[intervals.length - 1].trim());
        }

        return new Date(System.currentTimeMillis() + intervalMinutes * 60 * 1000);
    }
}
