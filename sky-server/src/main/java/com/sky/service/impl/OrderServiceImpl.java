package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
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
import com.sky.websocket.WebSocketServer;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    private WebSocketServer webSocketServer;

    private Orders orders;
    public static final String USER_CANCEL = "用户取消";
    public static final Integer ORDER_REMIND = 1;//订单提醒
    public static final Integer REMINDER = 2;//催单


//    ----------------管理端----------------

    /**
     * 订单列表
     * @param pageQueryDTO
     * @return
     */
    @Override
    public PageResult page(OrdersPageQueryDTO pageQueryDTO) {
        PageHelper.startPage(pageQueryDTO.getPage(),pageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.page(pageQueryDTO);
        // 部分订单状态，需要额外返回订单菜品信息，将Orders转化为OrderVO
        // 这是为了在工作台展示简略订单信息
        List<OrderVO> orderVOList = getOrderVOList(page);

        return new PageResult(page.getTotal(), orderVOList);
    }

    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        // 需要返回订单菜品信息，自定义OrderVO响应结果
        List<OrderVO> orderVOList = new ArrayList<>();
        List<Orders> ordersList = page.getResult();

        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                // 将共同字段复制到OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderDishes = getOrderDishesStr(orders);

                // 将订单菜品信息封装到orderVO中，并添加到orderVOList
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    /**
     * 根据订单id获取菜品信息字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
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
        Integer toBeConfirmed = orderMapper.countOrders(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countOrders(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countOrders(Orders.DELIVERY_IN_PROGRESS);

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);



        return orderStatisticsVO;
    }

//    ---------------用户端-------------------

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

        //跳过支付后，用于验证消息推送是否成功
        Map map = new HashMap();
        map.put("type", ORDER_REMIND);
        map.put("orderId", this.orders.getId());
        map.put("content","订单号:"+this.orders.getNumber());

        //转成json格式
        String jsonString = JSONObject.toJSONString(map);

        webSocketServer.sendToAllClient(jsonString);

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

        //给商家推送订单消息
        Map map = new HashMap();
        map.put("type", ORDER_REMIND);
        map.put("orderId", ordersDB.getId());
        map.put("content","订单号:"+outTradeNo);

        //转成json格式
        String jsonString = JSONObject.toJSONString(map);

        webSocketServer.sendToAllClient(jsonString);

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
     * 用户取消订单
     * @param id
     */
    @Override
    public void userCancel(Long id) throws Exception {
        //获取订单数据
        Orders orderT = orderMapper.getById(id);


        //判断订单是否存在
        if(orderT==null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        //接单后不可取消订单
        if(orderT.getStatus()>2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders order = new Orders();
        order.setId(orderT.getId());

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
//            order.setPayStatus(Orders.REFUND);
//        }

        //将订单状态改为取消,设置取消原因，订单取消时间
        order.setStatus(Orders.CANCELLED);
        order.setCancelReason(USER_CANCEL);
        order.setCancelTime(LocalDateTime.now());

        //插入表格
        orderMapper.update(order);

    }


    /**
     * 查询历史订单
     * @param pageQueryDTO
     * @return
     */
    @Override
    public PageResult getHistoryOrders(OrdersPageQueryDTO pageQueryDTO) {
        PageHelper.startPage(pageQueryDTO.getPage(),pageQueryDTO.getPageSize());
        pageQueryDTO.setUserId(BaseContext.getCurrentId());
        Page<Orders> page = orderMapper.page(pageQueryDTO);

        //创建VO集合存储数据
        List<OrderVO> orderVOList = new ArrayList();
        
        //遍历page集合
        if(page != null && page.getTotal()>0){
            for (Orders order : page) {
                //获取订单ID
                Long orderId = order.getId();
                //获得订单详情
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);
                //封装到VO对象，并添加到集合中
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);
                orderVO.setOrderDetailList(orderDetailList);

                orderVOList.add(orderVO);
            }
        }

        return new PageResult(page.getTotal(),orderVOList);

    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    public void repetition(Long id) {
        //根据ID查询订单详情
        Orders order = orderMapper.getById(id);

        //获取订单ID和当前用户ID
        Long orderId = order.getId();
        Long userId = BaseContext.getCurrentId();

        //获取商品详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);

        //将商品详情对象转成购物车对象
//        orderDetail->ShoppingCart
        List<ShoppingCart> shoppingCarts = orderDetailList.stream().map(od -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            BeanUtils.copyProperties(od, shoppingCart);

            return shoppingCart;
        }).collect(Collectors.toList());

        shoppingCartMapper.insertBatch(shoppingCarts);

    }

    /**
     * 订单催单
     * @param id
     */
    @Override
    public void reminder(Long id) {
        Orders order = orderMapper.getById(id);

        //判断订单是否存在
        if(order==null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        Map map = new HashMap();
        map.put("type", REMINDER);
        map.put("orderId", order.getId());
        map.put("content","订单号:"+order.getNumber());

        //转成json格式
        String jsonString = JSONObject.toJSONString(map);

        webSocketServer.sendToAllClient(jsonString);
    }

}
