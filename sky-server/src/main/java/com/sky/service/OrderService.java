package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.*;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

/**
 *
 * @author FragrantXue
 * Create by 2025.09.02 15:07
 */

public interface OrderService extends IService<Orders> {
    /**
     * 提交订单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 根据id查询订单详情
     * @param id
     * @return
     */
    OrderVO getOrderById(Long id);

    /**
     * 查询历史订单
     *
     * @return
     */
    PageResult<OrderVO> getHistoryOrders(int page, int pageSize, Integer status);

    /**
     * 用户端取消订单
     * @param id
     * @return
     */
    Integer cancelOrder(Long id);

    /**
     * 再来一单
     * @param id
     * @return
     */
    Integer repeatOrder(Long id);

    /**
     * 催单
     * @param id
     * @return
     */
    Integer reminderOrder(Long id);

    /**
     * 条件分页查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult<Orders> searchPageOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 获取订单统计数据
     * @return
     */
    OrderStatisticsVO getOrderStatistics();

    /**
     * 确认订单
     *
     * @param ordersConfirmDTO@return
     */
    Integer confirmOrder(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 拒绝订单
     * @param ordersRejectionDTO
     * @return
     */
    Integer rejectOrder(OrdersRejectionDTO ordersRejectionDTO) throws Exception;

    /**
     * 管理端取消订单
     * @param ordersCancelDTO
     * @return
     * @throws Exception
     */
    Integer cancelOrder(OrdersCancelDTO ordersCancelDTO) throws Exception;

    /**
     * 订单派送
     * @param id
     * @return
     */
    Integer deliverOrder(Long id);

    /**
     * 完成订单
     * @param id
     * @return
     */
    Integer completeOrder(Long id);
}
