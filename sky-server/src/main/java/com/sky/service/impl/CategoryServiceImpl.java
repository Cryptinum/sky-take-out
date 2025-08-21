package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author Zhao Chonghao
 * Create by 2025.08.21 15:31
 */

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;


    /**
     * 根据分类类型查询
     *
     * @param type
     * @param name
     * @return
     */
    @Override
    public List<Category> queryByType(Integer type, String name) {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(type != null, Category::getType, type)
                .like(name != null && !name.isEmpty(), Category::getName, name));
    }

    /**
     * 分页查询分类列表
     *
     * @param categoryPageQueryDTO
     * @return
     */
    @Override
    public PageResult<Category> queryCategoryPage(CategoryPageQueryDTO categoryPageQueryDTO) {
        String name = categoryPageQueryDTO.getName();
        Integer type = categoryPageQueryDTO.getType();
        int page = categoryPageQueryDTO.getPage();
        int pageSize = categoryPageQueryDTO.getPageSize();

        Page<Category> p = Page.of(page, pageSize);
        p.addOrder(OrderItem.desc("sort"));

        Page<Category> queryPage = lambdaQuery()
                .like(name != null && !name.isEmpty(), Category::getName, name)
                .like(type != null, Category::getType, type)
                .page(p);

        return PageResult.of(queryPage);
    }

    /**
     * 新增菜品分类
     *
     * @param categoryDTO
     * @return
     */
    @Override
    public Integer addCategory(CategoryDTO categoryDTO) {
        Category category = BeanUtil.copyProperties(categoryDTO, Category.class);
        category.setStatus(1);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());
        return categoryMapper.insert(category);
    }

    /**
     * 删除菜品分类
     *
     * @param id
     * @return
     */
    @Override
    public Integer deleteCategory(Long id) {
        return categoryMapper.deleteById(id);
    }

    /**
     * 更新菜品分类状态
     *
     * @param status
     * @param id
     * @return
     */
    @Override
    public Integer updateCategoryStatus(Integer status, Long id) {
        return categoryMapper.update(new LambdaUpdateWrapper<Category>()
                .eq(Category::getId, id)
                .set(Category::getStatus, status));
    }

    /**
     * 编辑菜品分类
     * @param categoryDTO
     * @return
     */
    @Override
    public Integer editCategory(CategoryDTO categoryDTO) {
        Category category = BeanUtil.copyProperties(categoryDTO, Category.class);
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        return categoryMapper.updateById(category);
    }
}
