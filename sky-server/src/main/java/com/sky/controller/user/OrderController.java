package com.sky.controller.user;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author FragrantXue
 * Create by 2025.09.02 15:04
 */

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Tag(name = "用户订单相关接口", description = "用户订单相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/orderDetail/{id}")
    @Operation(summary = "查询订单详情", description = "用户端查询订单详情")
    public Result<OrderVO> getOrderById(@PathVariable Long id) {
        log.info("用户端查询订单详情: {}", id);
        OrderVO orderVO = orderService.getOrderById(id);
        return Result.success(orderVO);
    }

    @GetMapping("/historyOrders")
    @Operation(summary = "查询历史订单", description = "用户端查询历史订单")
    public Result<PageResult<OrderVO>> getHistoryOrders(int page, int pageSize, Integer status) {
        log.info("用户端查询历史订单");
        PageResult<OrderVO> historyOrders = orderService.getHistoryOrders(page, pageSize, status);
        return Result.success(historyOrders);
    }

    @GetMapping("/reminder/{id}")
    @Operation(summary = "催单", description = "用户端催单")
    public Result<Integer> reminderOrder(@PathVariable Long id) {
        log.info("用户端催单: {}", id);
        Integer success = orderService.reminderOrder(id);
        return Result.success(success);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交订单", description = "用户端提交订单")
    public Result<OrderSubmitVO> submitOrder(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户端提交订单: {}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    @PutMapping("/repetition/{id}")
    @Operation(summary = "再来一单", description = "用户端再来一单")
    public Result<Integer> repeatOrder(@PathVariable Long id) {
        log.info("用户端再来一单: {}", id);
        Integer success = orderService.repeatOrder(id);
        return Result.success(success);
    }

    @PutMapping("/payment")
    @Operation(summary = "订单支付", description = "用户端订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    @PutMapping("/cancel/{id}")
    public Result<Integer> cancelOrder(@PathVariable Long id) {
        log.info("用户端取消订单: {}", id);
        Integer success = orderService.cancelOrder(id);
        return Result.success(success);
    }
}
