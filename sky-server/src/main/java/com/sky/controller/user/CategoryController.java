package com.sky.controller.user;

import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
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
 * Create by 2025.08.28 01:35
 */

@RestController("userCategoryController")
@RequestMapping("/user/category")
@Slf4j
@Tag(name = "分类接口", description = "提供分类接口的一系列功能")
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
}
