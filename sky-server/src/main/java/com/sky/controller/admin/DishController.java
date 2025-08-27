package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * @author FragrantXue
 * Create by 2025.08.21 23:54
 */

@RestController("adminDishController")
@RequestMapping("/admin/dish")
@Slf4j
@Tag(name = "菜品接口", description = "提供菜品接口的一系列功能")
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

    /**
     * 根据分类ID查询菜品信息
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @Operation(summary = "根据分类ID查询菜品信息", description = "提供根据菜品分类ID查询菜品列表的功能")
    public Result<List<Dish>> getDishByCategory(Long categoryId) {
        log.info("根据分类ID查询菜品信息，categoryId: {}", categoryId);
        List<Dish> dishList = dishService.getDishByCategory(categoryId);
        return Result.success(dishList);
    }

    /**
     * 分页查询菜品信息
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询菜品信息", description = "提供菜品的分页查询功能")
    public Result<PageResult<DishVO>> getDishPage(@ParameterObject DishPageQueryDTO dishPageQueryDTO) {
        log.info("分页查询菜品信息：{}", dishPageQueryDTO);
        PageResult<DishVO> pageResult = dishService.getDishPage(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 保存菜品信息
     * @param dishVO
     * @return
     */
    @PostMapping
    @Operation(summary = "保存菜品信息", description = "提供菜品的新增功能")
    public Result<Integer> saveDishWithFlavor(@RequestBody DishDTO dishDTO) {
        log.info("保存菜品信息：{}", dishDTO);
        Integer success = dishService.saveDishWithFlavor(dishDTO);
        return Result.success(success);
    }

    /**
     * 修改菜品状态
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @Operation(summary = "修改菜品状态", description = "根据ID修改菜品启售、停售状态")
    public Result<Integer> updateDishStatus(@PathVariable Integer status, @RequestParam Long id) {
        log.info("修改菜品状态: id: {}, status: {}", id, status);
        Integer success = dishService.updateDishStatus(status, id);
        return Result.success(success);
    }

    @PutMapping
    @Operation(summary = "编辑菜品信息", description = "提供菜品的编辑功能")
    public Result<Integer> editDish(@RequestBody DishDTO dishDTO) {
        log.info("编辑菜品信息：{}", dishDTO);
        Integer success = dishService.editDish(dishDTO);
        return Result.success(success);
    }

    /**
     * 批量删除菜品
     * @param idString
     * @return
     */
    @DeleteMapping
    @Operation(summary = "批量删除菜品", description = "根据菜品ID字符串批量删除菜品信息")
    public Result<Integer> deleteDishes(@RequestParam List<Long> ids) {
        log.info("删除菜品信息，ids: {}", ids);
        Integer success = dishService.deleteDishes(ids);
        return Result.success(success);
    }


}
