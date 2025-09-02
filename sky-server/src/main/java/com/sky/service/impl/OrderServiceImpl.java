package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.service.AddressBookService;
import com.sky.service.OrderService;
import com.sky.service.ShoppingCartService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author FragrantXue
 * Create by 2025.09.02 15:08
 */

@Service
public class OrderServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrderService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 提交订单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        // 处理业务异常，如地址簿为空，购物车为空等
        AddressBook addressById = addressBookService.getAddressById(ordersSubmitDTO.getAddressBookId());
        if (addressById == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        List<ShoppingCart> shoppingCartItems = shoppingCartService.getShoppingCartItems();
        if (shoppingCartItems == null || shoppingCartItems.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 向订单表中插入一条订单数据
        Orders orders = BeanUtil.copyProperties(ordersSubmitDTO, Orders.class);
        BeanUtil.copyProperties(addressById, orders, "id");
        orders.setAddressBookId(addressById.getId());
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(System.currentTimeMillis() + "" + BaseContext.getCurrentId().hashCode() + "" + UUID.randomUUID().hashCode());
        orders.setAddress(addressById.getProvinceName() + addressById.getCityName() + addressById.getDistrictName() + addressById.getDetail());
        ordersMapper.insert(orders);

        // 向订单明细表中插入多条订单明细数据
        List<OrderDetail> orderDetails = shoppingCartItems.stream()
                .map(shoppingCartItem -> {
                    OrderDetail orderDetail = BeanUtil.copyProperties(shoppingCartItem, OrderDetail.class);
                    orderDetail.setOrderId(orders.getId());
                    return orderDetail;
                })
                .toList();
        orderDetailMapper.insert(orderDetails);

        // 清空购物车
        shoppingCartService.clearShoppingCart();

        // 封装VO返回结果
        return OrderSubmitVO.builder()
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .build();
    }
}
