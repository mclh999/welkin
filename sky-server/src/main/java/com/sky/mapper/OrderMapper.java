package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param outTradeNo
     * @return
     */
    Orders getByNumber(String outTradeNo);

    /**
     * 更新订单数据
     * @param orders
     */
    void update(Orders orders);

    /**
     * 更新订单状态
     * 记得改成表中对应的字段
     * @param status
     * @param payStatus
     * @param checkoutTime
     * @param id
     */
    void updateStatus(Integer status, Integer payStatus, LocalDateTime checkoutTime, Long id);

    /**
     * 根据id查询订单详情
     * @param id
     * @return
     */
    Orders getById(Long id);

    /**
     * 分页查询订单
     * @param pageQueryDTO
     * @return
     */
    Page<Orders> page(OrdersPageQueryDTO pageQueryDTO);


    /**
     * 统计订单数据
     * @return
     */
    Integer countOrders(Integer status);


    /**
     * 获取超时订单
     * @return
     */
    List<Orders> processTimeoutOrders(Integer status, LocalDateTime orderTime);

    /**
     * 统计营业额数据
     * @return
     */
    Double getTurnover(Map map);

    /**
     * 统计订单数量
     * @return
     */
    Integer getOrderCount(Map map);

    /**
     * 获取指定时间区间的订单ID
     * @param map
     * @return
     */
    List<Integer> getOrderIdList(Map map);
}
