package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;

    private Orders orders;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //处理各种业务异常（地址簿为空，购物车为空）
        //根据ID查询地址簿
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBook addressBook = addressBookMapper.getById(addressBookId);
        if(addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //购物车为空
        ShoppingCart shoppingCart = new ShoppingCart();
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if(shoppingCartList == null || shoppingCartList.size() == 0){
            throw new AddressBookBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //添加一条数据到订单表
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        String address = addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail();
        orders.setAddress(address);
        this.orders = orders;//将orders保存到当前线程的变量中，以便获取orderId

        //TODO username
        orders.setUserName("呼呼大");

        orderMapper.insert(orders);

        //创建集合存储订单明细
        List<OrderDetail> orderDetailList = new ArrayList();

        //添加多条数据到订单明细表
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());

            orderDetailList.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetailList);

        //清空购物车数据
        shoppingCartMapper.deleteByUserId(userId);

        //封装VO并返回
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();

        return orderSubmitVO;

    }

    /**
     * 订单支付
     * @param ordersPaymentDTO
     */
    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        String userId = BaseContext.getCurrentId().toString();
        User user = userMapper.getByOpenId(userId);

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
        Integer orderStatus = Orders.TO_BE_CONFIRMED;//订单状态->待接单
        Integer orderPaidStatus = Orders.PAID;//订单状态->已支付
        LocalDateTime checkOutTime = LocalDateTime.now();
        orderMapper.updateStatus(orderStatus,orderPaidStatus,checkOutTime,this.orders.getId());

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 获取订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO getOrderById(Long id) {
        //获取订单数据
        Orders order = orderMapper.getById(id);

        //获取商品数据
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(order.getId());

        //封装成VO返回
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }

    /**
     * 订单列表
     * @param pageQueryDTO
     * @return
     */
    @Override
    public PageResult page(OrdersPageQueryDTO pageQueryDTO) {
        PageHelper.startPage(pageQueryDTO.getPage(),pageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.page(pageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 用户取消订单
     * @param id
     */
    @Override
    public void userCancel(Long id) throws Exception {
        //获取订单数据
        Orders order = orderMapper.getById(id);


        //判断订单是否存在
        if(order==null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        //接单后不可取消订单
        if(order.getStatus()>2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orderT = new Orders();
        orderT.setId(order.getId());

        //如果待接单时取消，需要退款,没有开通所以注释 ，可通过修改数据库来实现退款
//        if(order.getStatus()==Orders.TO_BE_CONFIRMED){
//            //调用微信支付退款接口
//            weChatPayUtil.refund(
//                    order.getNumber(), //商户订单号
//                    order.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额
//
//            //支付状态修改为 退款
//            orderT.setPayStatus(Orders.REFUND);
//        }

        //将订单状态改为取消,设置取消原因，订单取消时间
        orderT.setStatus(Orders.CANCELLED);
        orderT.setCancelReason("用户取消");
        orderT.setCancelTime(LocalDateTime.now());

        //插入表格
        orderMapper.update(order);

    }


    /**
     * 取消订单
     * @param ordersCancelDTO
     * @throws Exception
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception {
        //获取订单信息
        Orders order = orderMapper.getById(ordersCancelDTO.getId());

//        //判断是否需要退款
//        Integer payStatus = order.getPayStatus();
//        if (payStatus == 1) {
//            //用户已支付，需要退款
//            String refund = weChatPayUtil.refund(
//                    order.getNumber(),
//                    order.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
//        }


        //更新订单状态，取消时间，取消原因
        order.setCancelReason(ordersCancelDTO.getCancelReason());
        order.setStatus(Orders.CANCELLED);
        order.setCancelTime(LocalDateTime.now());

        orderMapper.update(order);

    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void complete(Long id) {
        //根据ID查询订单
        Orders orderT = orderMapper.getById(id);

        //判断订单是否存在且是否状态为4
        if(orderT==null || !orderT.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        /**
         * 这里创建一个新对象，可以避免对其他数据的修改，只更新想要的数据
         * 减少传入的字段，可以提高数据库的性能
         * 使用getId()来获取，可以及时抛出异常（如果为空），并表明这个对象就是用来更新上面那个对象
         */
        Orders order = new Orders();

        order.setId(orderT.getId());
        order.setStatus(Orders.COMPLETED);
        order.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(order);
    }

    /**
     * 订单拒绝
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        //获取订单信息
        Orders orderT = orderMapper.getById(ordersRejectionDTO.getId());

        //判断订单是否为空且是否为待接单
        if(orderT == null || !orderT.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //判断是否需要退款
//        Integer payStatus = orderT.getPayStatus();
//        if (payStatus == 1) {
//            //用户已支付，需要退款
//            weChatPayUtil.refund(
//                    orderT.getNumber(),
//                    orderT.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
//        }


        //更新订单状态
        Orders order = new Orders();
        order.setId(orderT.getId());
        order.setStatus(Orders.CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        order.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orderMapper.update(order);

    }

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        //获取订单数据
        Orders orderT = orderMapper.getById(ordersConfirmDTO.getId());

        //判断订单是否为空且是否为待接单
        if(orderT == null || !orderT.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //更新订单状态
        Orders order = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(order);

    }

    /**
     * 派送
     * @param id
     */
    @Override
    public void delivery(Long id) {
        Orders orderT = orderMapper.getById(id);

        if(orderT == null || !orderT.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders order = Orders.builder()
                .id(orderT.getId())
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();

        orderMapper.update(order);

    }

    /**
     * 订单统计
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed = orderMapper.selectOrders(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.selectOrders(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.selectOrders(Orders.DELIVERY_IN_PROGRESS);

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);



        return orderStatisticsVO;
    }
}
