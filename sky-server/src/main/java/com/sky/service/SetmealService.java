package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

/**
 *
 * @author FragrantXue
 * @email Create by 2025.08.22 14:12
 */

public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    Integer saveSetmeal(SetmealDTO setmealDTO);

    /**
     * 根据套餐ID查询套餐信息
     * @param id
     * @return
     */
    SetmealVO getSetmealById(Long id);

    /**
     * 分页查询套餐信息
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult<SetmealVO> getSetmealPage(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 修改套餐状态
     * @param status
     * @param id
     * @return
     */
    Integer updateSetmealStatus(Integer status, Long id);
}
