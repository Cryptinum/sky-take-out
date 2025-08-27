package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
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
 * Create by 2025.08.21 15:28
 */

@RestController("adminCategoryController")
@RequestMapping("/admin/category")
@Slf4j
@Tag(name = "分类管理接口", description = "提供分类管理的一系列功能")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    @Operation(summary = "根据类型查询分类", description = "根据分类类型和名称查询分类列表")
    public Result<List<Category>> getCategoryByType(Integer type) {
        log.info("根据类型查询分类: type: {}", type);
        List<Category> list = categoryService.getCategoryByType(type);
        return Result.success(list);
    }

    @GetMapping("/page")
    @Operation(summary = "分类分页查询", description = "分页查询分类列表")
    public Result<PageResult<Category>> getCategoryPage(@ParameterObject CategoryPageQueryDTO categoryPageQueryDTO) {
        log.info("分页查询分类列表: {}", categoryPageQueryDTO);
        PageResult<Category> pageResult = categoryService.getCategoryPage(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping
    @Operation(summary = "新增菜品分类", description = "新增菜品分类")
    public Result<Integer> saveCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("新增菜品分类: {}", categoryDTO);
        Integer success = categoryService.saveCategory(categoryDTO);
        return Result.success(success);
    }

    @PostMapping("/status/{status}")
    @Operation(summary = "修改分类状态", description = "根据ID修改分类状态")
    public Result<Integer> updateCategoryStatus(@PathVariable Integer status, @RequestParam Long id){
        log.info("修改分类状态: id: {}, status: {}", id, status);
        Integer success = categoryService.updateCategoryStatus(status, id);
        return Result.success(success);
    }

    @PutMapping
    @Operation(summary = "编辑菜品分类", description = "编辑菜品分类信息")
    public Result<Integer> editCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("编辑菜品分类: {}", categoryDTO);
        Integer success = categoryService.editCategory(categoryDTO);
        return Result.success(success);
    }

    @DeleteMapping
    @Operation(summary = "删除菜品分类", description = "根据ID删除菜品分类")
    public Result<Integer> deleteCategory(@RequestParam Long id) {
        log.info("删除菜品分类: id: {}", id);
        Integer success = categoryService.deleteCategory(id);
        return Result.success(success);
    }
}
