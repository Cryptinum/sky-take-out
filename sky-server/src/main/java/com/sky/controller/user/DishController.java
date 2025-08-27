package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 * @author FragrantXue
 * Create by 2025.08.28 02:21
 */


@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Tag(name = "菜品接口", description = "提供菜品接口的一系列功能")
public class DishController {

    @Autowired
    private DishService dishService;

    @GetMapping("/list")
    public Result<List<DishVO>> getDishByCategoryUser(Long categoryId) {
        log.info("根据分类ID查询菜品信息, categoryId: {}", categoryId);
        List<DishVO> dishVOList = dishService.getDishByCategoryUser(categoryId);
        return Result.success(dishVOList);
    }
}
