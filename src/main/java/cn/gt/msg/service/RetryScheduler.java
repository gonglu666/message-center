package cn.gt.msg.service;

import cn.gt.msg.entity.ConsumerPushLog;
import cn.gt.msg.entity.ConsumerRegistry;
import cn.gt.msg.entity.FailedMessageRetry;
import cn.gt.msg.mapper.ConsumerPushLogMapper;
import cn.gt.msg.mapper.ConsumerRegistryMapper;
import cn.gt.msg.mapper.FailedMessageRetryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;

/**
 * 重试调度服务
 *
 * @author message-center
 * @date 2025-11-15
 */
@Slf4j
@Service
public class RetryScheduler {

    @Autowired
    private FailedMessageRetryMapper failedMessageRetryMapper;

    @Autowired
    private ConsumerRegistryMapper consumerRegistryMapper;

    @Autowired
    private ConsumerPushLogMapper consumerPushLogMapper;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 定时执行重试任务（每分钟执行一次）
     */
    @Scheduled(cron = "0 * * * * ?")
    public void executeRetryTasks() {
        log.info("开始执行重试任务");

        try {
            // 查询需要重试的任务
            List<FailedMessageRetry> retryTasks = failedMessageRetryMapper.selectList(
                    new LambdaQueryWrapper<FailedMessageRetry>()
                            .eq(FailedMessageRetry::getStatus, 0) // 0-待重试
                            .le(FailedMessageRetry::getNextRetryTime, new Date())
                            .orderByAsc(FailedMessageRetry::getNextRetryTime)
            );

            log.info("查询到待重试任务数量: {}", retryTasks.size());

            for (FailedMessageRetry retryTask : retryTasks) {
                processRetryTask(retryTask);
            }
        } catch (Exception e) {
            log.error("执行重试任务异常", e);
        }
    }

    /**
     * 处理单个重试任务
     */
    @Transactional(rollbackFor = Exception.class)
    public void processRetryTask(FailedMessageRetry retryTask) {
        log.info("处理重试任务, messageId: {}, retryCount: {}", 
                retryTask.getMessageId(), retryTask.getRetryCount());

        try {
            // 查询消费者信息
            ConsumerRegistry consumer = consumerRegistryMapper.selectById(retryTask.getConsumerId());
            if (consumer == null) {
                log.error("消费者不存在, consumerId: {}", retryTask.getConsumerId());
                updateRetryTaskStatus(retryTask, 2, "消费者不存在"); // 2-重试失败
                return;
            }

            // 推送消息
            String responseBody = null;
            Integer httpStatus = null;
            Integer status = 0; // 0-失败
            String errorMsg = null;

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<>(retryTask.getMessageContent(), headers);

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
                    log.info("重试推送成功, messageId: {}, retryCount: {}", 
                            retryTask.getMessageId(), retryTask.getRetryCount() + 1);
                } else {
                    errorMsg = "HTTP状态码异常: " + httpStatus;
                }
            } catch (Exception e) {
                errorMsg = e.getMessage();
                log.error("重试推送异常, messageId: {}", retryTask.getMessageId(), e);
            }

            // 保存推送日志
            ConsumerPushLog pushLog = new ConsumerPushLog();
            pushLog.setMessageId(retryTask.getMessageId());
            pushLog.setConsumerId(consumer.getId());
            pushLog.setConsumerName(consumer.getConsumerName());
            pushLog.setConsumerUrl(consumer.getConsumerUrl());
            pushLog.setStatus(status);
            pushLog.setHttpStatus(httpStatus);
            pushLog.setErrorMessage(errorMsg);
            pushLog.setPushTime(new Date());
            consumerPushLogMapper.insert(pushLog);

            // 异步保存到ES
            elasticsearchService.saveConsumerPushLog(retryTask.getMessageId(), consumer.getId(),
                    consumer.getConsumerName(), retryTask.getMessageContent(), 
                    responseBody, status, httpStatus, errorMsg);

            // 更新重试任务状态
            int newRetryCount = retryTask.getRetryCount() + 1;
            if (status == 1) {
                // 重试成功
                updateRetryTaskStatus(retryTask, 1, null); // 1-重试成功
            } else if (newRetryCount >= retryTask.getMaxRetryCount()) {
                // 达到最大重试次数
                updateRetryTaskStatus(retryTask, 2, errorMsg); // 2-重试失败
            } else {
                // 继续重试
                retryTask.setRetryCount(newRetryCount);
                retryTask.setNextRetryTime(calculateNextRetryTime(newRetryCount, retryTask.getRetryIntervals()));
                retryTask.setUpdatedTime(new Date());
                failedMessageRetryMapper.updateById(retryTask);
            }
        } catch (Exception e) {
            log.error("处理重试任务异常, messageId: {}", retryTask.getMessageId(), e);
        }
    }

    /**
     * 更新重试任务状态
     */
    private void updateRetryTaskStatus(FailedMessageRetry retryTask, Integer status, String errorMsg) {
        retryTask.setStatus(status);
        retryTask.setErrorMessage(errorMsg);
        retryTask.setUpdatedTime(new Date());
        failedMessageRetryMapper.updateById(retryTask);
    }

    /**
     * 计算下次重试时间
     */
    private Date calculateNextRetryTime(int retryCount, String retryIntervals) {
        String[] intervals = retryIntervals.split(",");
        int intervalMinutes = 10;

        if (retryCount < intervals.length) {
            intervalMinutes = Integer.parseInt(intervals[retryCount].trim());
        } else if (intervals.length > 0) {
            intervalMinutes = Integer.parseInt(intervals[intervals.length - 1].trim());
        }

        return new Date(System.currentTimeMillis() + intervalMinutes * 60 * 1000);
    }
}
