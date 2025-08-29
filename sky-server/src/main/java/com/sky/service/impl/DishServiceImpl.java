package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.constant.RedisConstant;
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
import com.sky.util.RedisCacheUtil;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author FragrantXue
 * Create by 2025.08.21 23:52
 */

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisCacheUtil redisCacheUtil;


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
     * 用户端根据分类ID查询菜品信息
     *
     * @param categoryId
     * @return
     */
    @Override
    public List<DishVO> getDishByCategoryUser(Long categoryId) {
        // 1. 构造redis中的key，规则为 dish_category_{categoryId}
        String key = RedisConstant.DISH_CATEGORY_PATH + "dish_category_" + categoryId;

        // 2. 先从redis当中查询分类数据
        List<DishVO> dishVOList = (List<DishVO>) redisTemplate.opsForValue().get(key);

        // 3. 如果存在，则直接返回
        if (dishVOList != null && !dishVOList.isEmpty()) {
            log.info("从Redis缓存中查询到数据: {}", key);
            return dishVOList;
        }

        // 4. 如果不存在，那么查询数据库，并将数据存入redis

        /* ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓以下为原始实现↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ */

        log.info("Redis缓存未命中，开始查询数据库: {}", key);
        List<Dish> dishes = dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getCategoryId, categoryId)
                .eq(Dish::getStatus, 1) // 只查询起售状态的菜品
        );
        Category category = categoryMapper.selectById(categoryId);
        List<DishVO> dishVOS = BeanUtil.copyToList(dishes, DishVO.class);

        // 获取菜品所属的口味信息和类别名称
        for (DishVO dishVO : dishVOS) {
            List<DishFlavor> dishFlavors = dishFlavorMapper.selectList(new LambdaQueryWrapper<DishFlavor>()
                    .eq(DishFlavor::getDishId, dishVO.getId()));
            dishVO.setCategoryName(category.getName());
            dishVO.setFlavors(dishFlavors);
        }

        /* ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑以上为原始实现↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ */

        // 5. 将数据存入redis，设置过期时间为1小时
        redisTemplate.opsForValue().set(key, dishVOS, 1, TimeUnit.HOURS);
        return dishVOS;
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

        // 清除对应菜品类别的redis缓存
        String key = RedisConstant.DISH_CATEGORY_PATH + "dish_category_" + dish.getCategoryId();
        redisTemplate.delete(key);
        log.info("清除以下键的Redis缓存: {}", key);
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
        Integer update = dishMapper.update(new LambdaUpdateWrapper<Dish>()
                .eq(Dish::getId, id)
                .set(Dish::getStatus, status));

        // 清空redis缓存
        cleanDishCategoryCache();
        return update;
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

        // 清空redis缓存
        cleanDishCategoryCache();
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

    private void cleanDishCategoryCache() {
        String key = RedisConstant.DISH_CATEGORY_PATH + "*";
        redisCacheUtil.cleanCacheSafe(key);
        log.info("清除以下目录的Redis缓存: {}", key);
    }
}
