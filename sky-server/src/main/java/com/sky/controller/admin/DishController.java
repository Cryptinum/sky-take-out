package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author Zhao Chonghao
 * Create by 2025.08.21 23:54
 */

@Controller
@RequestMapping("/admin/dish")
@Tag(name = "用户接口", description = "提供用户管理的一系列功能")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 根据菜品ID查询菜品信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "菜品ID查询菜品信息", description = "根据菜品ID查询菜品的详细信息")
    public Result<DishVO> getDishById(@PathVariable Long id) {
        log.info("查询菜品信息，id: {}", id);
        DishVO dishVO = dishService.getDishById(id);
        return Result.success(dishVO);
    }
}
