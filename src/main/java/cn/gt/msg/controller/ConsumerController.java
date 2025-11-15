package cn.gt.msg.controller;

import cn.gt.msg.dto.ConsumerRegisterRequest;
import cn.gt.msg.dto.Result;
import cn.gt.msg.entity.ConsumerRegistry;
import cn.gt.msg.service.ConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消费者管理控制器
 *
 * @author message-center
 * @date 2025-11-15
 */
@Slf4j
@RestController
@RequestMapping("/api/consumer")
public class ConsumerController {

    @Autowired
    private ConsumerService consumerService;

    /**
     * 注册消费者
     */
    @PostMapping("/register")
    public Result<Void> registerConsumer(@Validated @RequestBody ConsumerRegisterRequest request) {
        try {
            log.info("接收到消费者注册请求: {}", request.getConsumerName());
            consumerService.registerConsumer(request);
            return Result.success();
        } catch (Exception e) {
            log.error("消费者注册失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询消费者列表
     */
    @GetMapping("/list")
    public Result<List<ConsumerRegistry>> listConsumers(
            @RequestParam(required = false) String templateId,
            @RequestParam(required = false) Integer status) {
        try {
            List<ConsumerRegistry> consumers = consumerService.listConsumers(templateId, status);
            return Result.success(consumers);
        } catch (Exception e) {
            log.error("查询消费者列表失败", e);
            return Result.error(e.getMessage());
        }
    }
}
