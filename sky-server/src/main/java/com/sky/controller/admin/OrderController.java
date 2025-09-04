package com.sky.controller.admin;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Result<PageResult<OrderVO>> searchPageOrders(@ParameterObject OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("管理员端条件分页查询订单: {}", ordersPageQueryDTO);
        PageResult<OrderVO> pageResult = orderService.searchPageOrders(ordersPageQueryDTO);
        return Result.success(pageResult);
    }
}
