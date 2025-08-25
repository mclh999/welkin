package com.sky.service;

import com.sky.dto.*;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

import java.util.List;
import java.util.Map;

public interface OrderService {

    //管理端

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    OrderVO getOrderById(Long id);

    /**
     * 分页查询订单
     * @param pageQueryDTO
     * @return
     */
    PageResult page(OrdersPageQueryDTO pageQueryDTO);

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception;

    /**
     * 完成订单
     * @param id
     */
    void complete(Long id);

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception;

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 派送
     * @param id
     */
    void delivery(Long id);


    //用户端

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

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
     * 用户取消订单
     * @param id
     */
    void userCancel(Long id) throws Exception;

    /**
     * 统计订单数据
     * @return
     */
    OrderStatisticsVO statistics();

    /**
     * 查询历史订单
     * @param pageQueryDTO
     * @return
     */
    PageResult getHistoryOrders(OrdersPageQueryDTO pageQueryDTO);

    /**
     * 再来一单
     * @param id
     */
    void repetition(Long id);

    /**
     * 催单
     * @param id
     */
    void reminder(Long id);
}
