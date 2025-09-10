package com.sky.service;

import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;

/**
 *
 * @author FragrantXue
 * Create by 2025.09.11 00:34
 */

public interface WorkspaceService {

    /**
     * 获取平台今日运营数据
     * @return
     */
    BusinessDataVO getBusinessData();

    /**
     * 查询订单管理数据
     * @return
     */
    OrderOverViewVO getOrderOverView();

    /**
     * 查询菜品总览
     * @return
     */
    DishOverViewVO getDishOverView();

    /**
     * 查询套餐总览
     * @return
     */
    SetmealOverViewVO getSetmealOverView();
}
