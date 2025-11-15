package cn.gt.msg.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

/**
 * Kafka管理服务
 *
 * @author message-center
 * @date 2025-11-15
 */
@Slf4j
@Service
public class KafkaAdminService {

    @Autowired
    private AdminClient adminClient;

    /**
     * 创建Kafka主题
     */
    public void createTopic(String topicName, int partitions, int replicationFactor) {
        try {
            log.info("开始创建Kafka主题: {}, 分区数: {}, 副本数: {}", topicName, partitions, replicationFactor);

            NewTopic newTopic = new NewTopic(topicName, partitions, (short) replicationFactor);
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();

            log.info("Kafka主题创建成功: {}", topicName);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof org.apache.kafka.common.errors.TopicExistsException) {
                log.warn("Kafka主题已存在: {}", topicName);
            } else {
                log.error("创建Kafka主题失败: {}", topicName, e);
                throw new RuntimeException("创建Kafka主题失败", e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("创建Kafka主题被中断: {}", topicName, e);
            throw new RuntimeException("创建Kafka主题被中断", e);
        }
    }
}
