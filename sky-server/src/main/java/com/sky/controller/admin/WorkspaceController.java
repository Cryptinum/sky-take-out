package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author FragrantXue
 * Create by 2025.09.11 00:32
 */

@RestController
@RequestMapping("/admin/workspace")
@Slf4j
@Tag(name = "工作台页面相关接口", description = "工作台页面相关接口")
public class WorkspaceController {

    @Autowired
    private WorkspaceService workspaceService;

    @GetMapping("/businessData")
    @Operation(summary = "获取平台今日运营数据", description = "获取平台今日运营数据")
    public Result<BusinessDataVO> getBusinessData(){
        log.info("获取平台今日运营数据");
        BusinessDataVO businessDataVO = workspaceService.getBusinessData();
        return Result.success(businessDataVO);
    }

    @GetMapping("/overviewOrders")
    @Operation(summary = "查询订单总览", description = "查询订单总览")
    public Result<OrderOverViewVO> orderOverView(){
        log.info("查询订单总览");
        OrderOverViewVO orderOverViewVO = workspaceService.getOrderOverView();
        return Result.success(orderOverViewVO);
    }

    @GetMapping("/overviewDishes")
    @Operation(summary = "查询菜品总览", description = "查询菜品总览")
    public Result<DishOverViewVO> dishOverView(){
        log.info("查询菜品总览");
        DishOverViewVO dishOverViewVO = workspaceService.getDishOverView();
        return Result.success(dishOverViewVO);
    }

    @GetMapping("/overviewSetmeals")
    @Operation(summary = "查询套餐总览", description = "查询套餐总览")
    public Result<SetmealOverViewVO> setmealOverView(){
        log.info("查询套餐总览");
        SetmealOverViewVO setmealOverViewVO = workspaceService.getSetmealOverView();
        return Result.success(setmealOverViewVO);
    }
}
