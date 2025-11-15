package cn.gt.msg.service;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * Kafka生产者服务
 *
 * @author message-center
 * @date 2025-11-15
 */
@Slf4j
@Service
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 发送消息到Kafka
     */
    public void sendMessage(String topic, String key, Object message) {
        try {
            String messageJson = JSON.toJSONString(message);
            log.info("发送消息到Kafka, topic: {}, key: {}, message: {}", topic, key, messageJson);

            ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, messageJson);
            
            future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                @Override
                public void onSuccess(SendResult<String, String> result) {
                    log.info("消息发送成功, topic: {}, partition: {}, offset: {}",
                            topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }

                @Override
                public void onFailure(Throwable ex) {
                    log.error("消息发送失败, topic: {}, key: {}", topic, key, ex);
                }
            });
        } catch (Exception e) {
            log.error("发送消息到Kafka异常, topic: {}, key: {}", topic, key, e);
            throw new RuntimeException("发送消息失败", e);
        }
    }
}
