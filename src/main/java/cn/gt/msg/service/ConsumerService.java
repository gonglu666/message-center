package cn.gt.msg.service;

import cn.gt.msg.dto.ConsumerRegisterRequest;
import cn.gt.msg.entity.ConsumerRegistry;
import cn.gt.msg.entity.MessageTemplate;
import cn.gt.msg.mapper.ConsumerRegistryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 消费者服务类
 *
 * @author message-center
 * @date 2025-11-15
 */
@Slf4j
@Service
public class ConsumerService {

    @Autowired
    private ConsumerRegistryMapper consumerRegistryMapper;

    @Autowired
    private TemplateService templateService;

    /**
     * 注册消费者
     */
    @Transactional(rollbackFor = Exception.class)
    public void registerConsumer(ConsumerRegisterRequest request) {
        log.info("开始注册消费者: {}", request.getConsumerName());

        // 检查模板是否存在
        MessageTemplate template = templateService.getTemplateByTemplateId(request.getTemplateId());
        if (template == null) {
            throw new RuntimeException("模板不存在: " + request.getTemplateId());
        }

        // 检查消费者是否已存在
        ConsumerRegistry existConsumer = consumerRegistryMapper.selectOne(
                new LambdaQueryWrapper<ConsumerRegistry>()
                        .eq(ConsumerRegistry::getTemplateId, request.getTemplateId())
                        .eq(ConsumerRegistry::getConsumerName, request.getConsumerName())
        );
        if (existConsumer != null) {
            throw new RuntimeException("消费者已存在: " + request.getConsumerName());
        }

        // 保存消费者信息
        ConsumerRegistry consumer = new ConsumerRegistry();
        consumer.setTemplateId(request.getTemplateId());
        consumer.setConsumerName(request.getConsumerName());
        consumer.setConsumerUrl(request.getConsumerUrl());
        consumer.setHttpMethod(request.getHttpMethod());
        consumer.setTimeoutMs(request.getTimeoutMs());
        consumer.setHeaders(request.getHeaders());
        consumer.setMaxRetryCount(request.getMaxRetryCount());
        consumer.setRetryIntervals(request.getRetryIntervals());
        consumer.setStatus(1); // 1-启用
        consumer.setCreatedTime(new Date());
        consumer.setUpdatedTime(new Date());
        consumerRegistryMapper.insert(consumer);

        log.info("消费者注册成功: {}", request.getConsumerName());
    }

    /**
     * 查询消费者列表
     */
    public List<ConsumerRegistry> listConsumers(String templateId, Integer status) {
        LambdaQueryWrapper<ConsumerRegistry> wrapper = new LambdaQueryWrapper<>();
        if (templateId != null && !templateId.isEmpty()) {
            wrapper.eq(ConsumerRegistry::getTemplateId, templateId);
        }
        if (status != null) {
            wrapper.eq(ConsumerRegistry::getStatus, status);
        }
        wrapper.orderByDesc(ConsumerRegistry::getCreatedTime);
        return consumerRegistryMapper.selectList(wrapper);
    }

    /**
     * 根据模板ID查询启用的消费者
     */
    public List<ConsumerRegistry> getActiveConsumersByTemplateId(String templateId) {
        return consumerRegistryMapper.selectList(
                new LambdaQueryWrapper<ConsumerRegistry>()
                        .eq(ConsumerRegistry::getTemplateId, templateId)
                        .eq(ConsumerRegistry::getStatus, 1)
        );
    }
}
