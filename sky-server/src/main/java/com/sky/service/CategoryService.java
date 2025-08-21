package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService extends IService<Category> {

    /**
     * 根据分类类型查询
     *
     * @param type
     * @return
     */
    List<Category> getCategoryByType(Integer type);


    /**
     * 分页查询分类列表
     * @param categoryPageQueryDTO
     * @return
     */
    PageResult<Category> getCategoryPage(CategoryPageQueryDTO categoryPageQueryDTO);


    /**
     * 新增菜品分类
     * @param categoryDTO
     * @return
     */
    Integer saveCategory(CategoryDTO categoryDTO);

    /**
     * 删除菜品分类
     * @param id
     * @return
     */
    Integer deleteCategory(Long id);

    /**
     * 更新菜品分类状态
     * @param status
     * @param id
     * @return
     */
    Integer updateCategoryStatus(Integer status, Long id);

    /**
     * 编辑菜品分类
     * @param categoryDTO
     * @return
     */
    Integer editCategory(CategoryDTO categoryDTO);
}
