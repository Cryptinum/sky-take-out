package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author FragrantXue
 * Create by 2025.09.04 22:44
 */

@RestController
@RequestMapping("/admin/order")
@Slf4j
@Tag(name = "管理员订单相关接口", description = "管理员订单相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/conditionSearch")
    @Operation(summary = "条件分页查询订单", description = "管理员端条件分页查询订单")
    public Result<PageResult<Orders>> searchPageOrders(@ParameterObject OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("管理员端条件分页查询订单: {}", ordersPageQueryDTO);
        PageResult<Orders> pageResult = orderService.searchPageOrders(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/details/{id}")
    @Operation(summary = "根据id查询订单详情", description = "管理员端根据id查询订单详情")
    public Result<OrderVO> getOrderById(@PathVariable Long id) {
        log.info("管理员端根据id查询订单详情: {}", id);
        OrderVO orderVO = orderService.getOrderById(id);
        return Result.success(orderVO);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取订单统计数据", description = "管理员端获取订单统计数据")
    public Result<OrderStatisticsVO> getOrderStatistics() {
        log.info("管理员端获取订单统计数据");
        OrderStatisticsVO orderStatisticsVO = orderService.getOrderStatistics();
        return Result.success(orderStatisticsVO);
    }

    @PutMapping("/confirm")
    @Operation(summary = "确认订单", description = "管理员端确认订单")
    public Result<Integer> confirmOrder(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("管理员端确认订单: {}", ordersConfirmDTO);
        Integer success = orderService.confirmOrder(ordersConfirmDTO);
        return Result.success(success);
    }

    @PutMapping("/rejection")
    @Operation(summary = "拒绝订单", description = "管理员端拒绝订单")
    public Result<Integer> rejectOrder(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        log.info("管理员端拒绝订单: {}", ordersRejectionDTO);
        Integer success = orderService.rejectOrder(ordersRejectionDTO);
        return Result.success(success);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消订单", description = "管理员端取消订单")
    public Result<Integer> cancelOrder(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception {
        log.info("管理员端取消订单: {}", ordersCancelDTO);
        Integer success = orderService.cancelOrder(ordersCancelDTO);
        return Result.success(success);
    }

    @PutMapping("/delivery/{id}")
    @Operation(summary = "订单派送", description = "管理员端订单派送")
    public Result<Integer> deliverOrder(@PathVariable Long id) {
        log.info("管理员端订单派送: {}", id);
        Integer success = orderService.deliverOrder(id);
        return Result.success(success);
    }

    @PutMapping("/complete/{id}")
    @Operation(summary = "完成订单", description = "管理员端完成订单")
    public Result<Integer> completeOrder(@PathVariable Long id) {
        log.info("管理员端完成订单: {}", id);
        Integer success = orderService.completeOrder(id);
        return Result.success(success);
    }
}
