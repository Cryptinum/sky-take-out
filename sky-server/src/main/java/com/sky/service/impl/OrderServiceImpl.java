package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.result.PageResult;
import com.sky.service.AddressBookService;
import com.sky.service.OrderService;
import com.sky.service.ShoppingCartService;
import com.sky.service.UserService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
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

    @Autowired
    private UserService userService;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    /**
     * 根据id查询订单详情
     *
     * @param id
     * @return
     */
    @Override
    public OrderVO getOrderById(Long id) {
        Orders orders = ordersMapper.selectById(id);
        OrderVO orderVO = BeanUtil.copyProperties(orders, OrderVO.class);
        setOrderVODetailAndName(orderVO);
        return orderVO;
    }

    /**
     * 查询历史订单
     *
     * @return
     */
    @Override
    public PageResult<OrderVO> getHistoryOrders(int page, int pageSize, Integer status) {
        Page<Orders> p = Page.of(page, pageSize);
        Long userId = BaseContext.getCurrentId();
        p.addOrder(OrderItem.desc("order_time"));
        List<Orders> records = lambdaQuery()
                .eq(userId != null, Orders::getUserId, userId)
                .eq(status != null, Orders::getStatus, status)
                .page(p).getRecords();
        if (records == null || records.isEmpty()) {
            return PageResult.empty(p);
        }
        List<OrderVO> orderVOS = BeanUtil.copyToList(records, OrderVO.class);
        for (OrderVO orderVO : orderVOS) {
            setOrderVODetailAndName(orderVO);
        }
        return new PageResult<>(p.getTotal(), p.getPages(), orderVOS);
    }

    private void setOrderVODetailAndName(OrderVO orderVO) {
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>()
                .eq(OrderDetail::getOrderId, orderVO.getId()));
        List<String> nameList = orderDetails.stream().map(OrderDetail::getName).toList();
        String nameString = String.join(",", nameList);

        orderVO.setOrderDetailList(orderDetails);
        orderVO.setOrderDishes(nameString);
    }

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

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userService.getById(userId);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        paySuccess(ordersPaymentDTO.getOrderNumber());

        return vo;


//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal("0.01"), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
//
//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));
//
//        return vo;

//        // 直接调用支付成功的方法，模拟微信支付回调
//        paySuccess(ordersPaymentDTO.getOrderNumber());
//
//        // 由于我们跳过了微信支付，无法生成真实的支付参数，
//        // 但为了保证接口契约，仍然返回一个空的VO对象。
//        // 前端可能需要做相应调整来处理这个模拟流程。
//        return OrderPaymentVO
//                .builder()
//                .timeStamp(String.valueOf(System.currentTimeMillis()))
//                .build();
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getNumber, outTradeNo));

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        ordersMapper.updateById(orders);
    }
}
