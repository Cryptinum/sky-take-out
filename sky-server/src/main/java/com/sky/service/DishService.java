package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService extends IService<Dish> {

    /**
     * 根据菜品id查询菜品信息
     * @param id
     * @return
     */
    DishVO getDishById(Long id);

    /**
     * 保存菜品信息
     * @param dishDTO
     * @return
     */
    Integer saveDishWithFlavor(DishDTO dishDTO);

    /**
     * 根据分类ID查询菜品信息
     * @param categoryId
     * @return
     */
    List<Dish> getDishByCategory(Long categoryId);

    /**
     * 用户端根据分类ID查询菜品信息
     * @param categoryId
     * @return
     */
    List<DishVO> getDishByCategoryUser(Long categoryId);

    /**
     * 分页查询菜品信息
     * @param dishPageQueryDTO
     * @return
     */
    PageResult<DishVO> getDishPage(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 编辑菜品信息
     * @param dishDTO
     * @return
     */
    Integer editDish(DishDTO dishDTO);

    /**
     * 批量删除菜品
     *
     * @param ids@return
     */
    Integer deleteDishes(List<Long> ids);

    /**
     * 更新菜品状态
     * @param status
     * @param id
     * @return
     */
    Integer updateDishStatus(Integer status, Long id);
}
