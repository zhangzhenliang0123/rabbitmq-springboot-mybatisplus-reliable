package xyz.zhenliang.rabbitmq.demo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import xyz.zhenliang.rabbitmq.demo.dto.OrderDTO;
import xyz.zhenliang.rabbitmq.demo.dto.Result;
import xyz.zhenliang.rabbitmq.demo.service.DemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.zhenliang.rabbitmq.dto.QueueProcessResult;
import xyz.zhenliang.rabbitmq.dto.RabbitmqMsgDTO;
import xyz.zhenliang.rabbitmq.entity.RabbitmqMessage;
import xyz.zhenliang.rabbitmq.rescue.IRabbitmqRescue;

/**
 * 消息Demo控制器
 * 提供消息发送和重发接口
 */
@RestController
@Tag(name = "rabbitmq示例", description = "消息相关操作接口")
@RequestMapping("/demo")
public class DemoController {
    @Autowired
    private DemoService demoService;
    @Autowired
    private IRabbitmqRescue rabbitmqRescue;

    /**
     * 发送消息接口
     * @param orderDTO 订单数据传输对象
     * @return 发送结果
     */
    @Operation(summary = "发送消息", description = "发送消息接口")
    @PostMapping("/send")
    public Result<RabbitmqMsgDTO<OrderDTO>> send(@RequestBody OrderDTO orderDTO) {
        return Result.ok(demoService.send(orderDTO));
    }
    
    /**
     * 重发消息接口
     * @param messageId 消息ID
     * @return 重发结果
     */
    @Operation(summary = "重发消息", description = "根据消息id重发消息")
    @GetMapping("/resendMsg")
    public Result<String> resendMsg(@RequestParam String messageId) {
        demoService.resendMsg(messageId);
        return Result.ok();
    }

    @Operation(summary = "获取队列消息数量", description = "根据消息队列名称获取消息数量")
    @GetMapping("/getDeadLetterCount")
    public Result<Long> getDeadLetterCount(@RequestParam String queueName) {

        return Result.ok(rabbitmqRescue.getDeadLetterCount(queueName));
    }

    @Operation(summary = "死信队列消息转消息表", description = "根据消息队列名称清除队列消息")
    @GetMapping("/moveQueueMessagesToMsgTable")
    public Result<QueueProcessResult> moveQueueMessagesToMsgTable(@RequestParam String queueName) {
        QueueProcessResult qpr=rabbitmqRescue.moveQueueMessagesToMsgTable(queueName);
        return Result.ok(qpr);
    }

    @Operation(summary = "清除队列消息", description = "根据消息队列名称清除队列消息")
    @GetMapping("/purgeQueue")
    public Result<String> purgeQueue(@RequestParam String queueName) {
        rabbitmqRescue.purgeQueue(queueName);
        return Result.ok();
    }

    @Operation(summary = "删除消息", description = "根据消息id删除消息")
    @GetMapping("/deleteMessage")
    public Result<String> deleteMessage(@RequestParam String messageId) {
        rabbitmqRescue.deleteMessage(messageId);
        return Result.ok();
    }
    @Operation(summary = "根据ID获取消息", description = "根据消息id获取消息表中消息")
    @GetMapping("/getMessageById")
    public Result<RabbitmqMessage> getMessageById(@RequestParam String messageId) {
        return Result.ok(rabbitmqRescue.getMessageById(messageId));
    }

    @Operation(summary = "获取发送失败消息数量", description = "获取1分钟前1天内的发送失败的消息")
    @GetMapping("/getSendFailedCount")
    public Result<Long> getSendFailedCount() {

        return Result.ok(rabbitmqRescue.getSendFailedCount(24*60*60,60));
    }
    @Operation(summary = "获取1分钟前消费失败消息数量", description = "获取1分钟前1天内的消费失败的消息")
    @GetMapping("/getConsumeFailedCount")
    public Result<Long> getConsumeFailedCount() {

        return Result.ok(rabbitmqRescue.getConsumeFailedCount(24*60*60,60));
    }
    @Operation(summary = "分页1分钟前获取发送失败消息", description = "分页获取1分钟前1天内的发送失败的消息")
    @GetMapping("/getSendFailedPage")
    public Result<Page<RabbitmqMessage>> getSendFailedPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Page<RabbitmqMessage> pageObj = new Page<>(page, size);
        return Result.ok(rabbitmqRescue.getSendFailedPage(pageObj, 24*60*60, 60));
    }
    @Operation(summary = "分页获取消费失败消息", description = "分页获取1分钟前1天内的消费失败的消息")
    @GetMapping("/getConsumeFailedPage")
    public Result<Page<RabbitmqMessage>> getConsumeFailedPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<RabbitmqMessage> pageObj = new Page<>(page, size);
        return Result.ok(rabbitmqRescue.getConsumeFailedPage(pageObj, 24*60*60, 60));
    }



}