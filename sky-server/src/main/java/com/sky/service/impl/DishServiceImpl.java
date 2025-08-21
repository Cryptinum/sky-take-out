package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 * @author Zhao Chonghao
 * Create by 2025.08.21 23:52
 */

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;


    /**
     * 根据菜品id查询菜品信息
     *
     * @param id
     * @return
     */
    @Override
    @Transactional
    public DishVO getDishById(Long id) {
        Dish dish = dishMapper.selectById(id);
        DishVO dishVO = BeanUtil.copyProperties(dish, DishVO.class);
        // 获取菜品所属的分类名称和口味信息
        Category category = categoryMapper.selectById(dishVO.getCategoryId());
        List<DishFlavor> dishFlavors = dishFlavorMapper.selectList(new LambdaQueryWrapper<DishFlavor>()
                .eq(DishFlavor::getDishId, dish.getId()));
        // 设置其他信息
        dishVO.setCategoryName(category.getName());
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    /**
     * 根据分类ID查询菜品信息
     *
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> getDishByCategory(Long categoryId) {
        return dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getCategoryId, categoryId));
    }

    /**
     * 分页查询菜品信息
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    @Transactional
    public PageResult<DishVO> getDishPage(DishPageQueryDTO dishPageQueryDTO) {
        int page = dishPageQueryDTO.getPage();
        int pageSize = dishPageQueryDTO.getPageSize();
        String name = dishPageQueryDTO.getName();
        Integer categoryId = dishPageQueryDTO.getCategoryId();
        Integer status = dishPageQueryDTO.getStatus();
        Page<Dish> p = Page.of(page, pageSize);
        p.addOrder(OrderItem.desc("create_time")).addOrder(OrderItem.desc("update_time"));
        List<Dish> records = lambdaQuery()
                .eq(categoryId != null, Dish::getCategoryId, categoryId)
                .eq(status != null, Dish::getStatus, status)
                .like(name != null && !name.isEmpty(), Dish::getName, name)
                .page(p).getRecords();
        if (records == null || records.isEmpty()) {
            return PageResult.empty(p);
        }
        List<DishVO> dishVOList = BeanUtil.copyToList(records, DishVO.class);
        for (DishVO dishVO : dishVOList) {
            dishVO.setCategoryName(categoryMapper.selectById(dishVO.getCategoryId()).getName());
            dishVO.setFlavors(dishFlavorMapper.selectList(new LambdaQueryWrapper<DishFlavor>()
                    .eq(DishFlavor::getDishId, dishVO.getId())));
        }
        return new PageResult<>(p.getTotal(), p.getPages(), dishVOList);
    }

    /**
     * 保存菜品信息
     *
     * @param dishDTO
     * @return
     */
    @Override
    @Transactional  // 使用注解保证事务的一致性
    public Integer saveDishWithFlavor(DishDTO dishDTO) {
        Dish dish = BeanUtil.copyProperties(dishDTO, Dish.class);
        int success = dishMapper.insert(dish);
        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(flavor -> flavor.setDishId(dishId));
            dishFlavorMapper.insert(flavors);
        }
        return success;
    }

    /**
     * 更新菜品状态
     *
     * @param status
     * @param id
     * @return
     */
    @Override
    public Integer updateDishStatus(Integer status, Long id) {
        return dishMapper.update(new LambdaUpdateWrapper<Dish>()
                .eq(Dish::getId, id)
                .set(Dish::getStatus, status));
    }

    /**
     * 编辑菜品信息
     *
     * @param dishDTO
     * @return
     */
    @Override
    @Transactional
    public Integer editDish(DishDTO dishDTO) {
        Dish dish = BeanUtil.copyProperties(dishDTO, Dish.class);
        Long dishId = dish.getId();
        int success = dishMapper.updateById(dish);
        dishFlavorMapper.delete(new LambdaUpdateWrapper<DishFlavor>()
                .in(DishFlavor::getDishId, dishId));
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(flavor -> flavor.setDishId(dishId));
            dishFlavorMapper.insert(flavors);
        }
        return success;
    }

    /**
     * 批量删除菜品
     *
     * @param ids
     * @return
     */
    @Override
    @Transactional
    public Integer deleteDishes(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new DeletionNotAllowedException(MessageConstant.ID_LIST_IS_NULL);
        }
        List<Dish> dishes = dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .in(Dish::getId, ids)
                .eq(Dish::getStatus, 0));
        if (dishes.size() != ids.size()) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }
        List<SetmealDish> setmealDishes = setmealDishMapper.selectList(new LambdaQueryWrapper<SetmealDish>()
                .in(SetmealDish::getDishId, ids));
        if (!setmealDishes.isEmpty()) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        // 删除菜品口味信息
        int i = dishMapper.deleteByIds(ids);
        int j = dishFlavorMapper.delete(new LambdaUpdateWrapper<DishFlavor>()
                .in(DishFlavor::getDishId, ids));
        return i + j;
    }
}
