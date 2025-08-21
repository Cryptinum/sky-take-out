package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.entity.Dish;
import com.sky.vo.DishVO;

public interface DishService extends IService<Dish> {

    /**
     * 根据菜品id查询菜品信息
     * @param id
     * @return
     */
    DishVO getDishById(Long id);
}
