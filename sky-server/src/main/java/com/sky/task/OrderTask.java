package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务，处理订单状态
 */
@Slf4j
@Component
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrders(){
        log.info("处理超时订单");

        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

        //获取所有超时订单
        List<Orders> ordersList = orderMapper.processTimeoutOrders(Orders.PENDING_PAYMENT,time);

        //取消所有超时订单
        for (Orders orders : ordersList) {
            orders.setStatus(Orders.CANCELLED);
            orders.setCancelReason("支付超时");
            orders.setCancelTime(LocalDateTime.now());

            orderMapper.update(orders);
        }

    }

    /**
     * 处理处于待派送状态的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrders() {
        log.info("派送超时");

        //相当于处理上一个工作日的订单
        LocalDateTime time = LocalDateTime.now().plusHours(-1);

        //获取所有超时订单
        List<Orders> ordersList = orderMapper.processTimeoutOrders(Orders.DELIVERY_IN_PROGRESS,time);

        //取消所有超时订单
        //TODO 到时候需要考虑预到达时间，防止用户刚下完就取消订单
        for (Orders orders : ordersList) {
            orders.setStatus(Orders.COMPLETED);
            orderMapper.update(orders);
        }
    }

}
