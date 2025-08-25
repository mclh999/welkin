package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("userOrderController")
@RequestMapping("/user/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    public Result submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("用户下单,{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    public Result payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付:{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        return Result.success(orderPaymentVO);
    }


    /**
     * 用户根据订单id查询订单
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> userGetOrderById(@PathVariable Long id){
        log.info("用户查询订单：{}", id);
        OrderVO orderVO = orderService.getOrderById(id);
        return Result.success(orderVO);
    }

    /**
     * 查询历史订单
     * @param pageQueryDTO
     * @return
     */
    @GetMapping("/historyOrders")
    public Result<PageResult> getHistoryOrders(OrdersPageQueryDTO pageQueryDTO){
        log.info("查询历史订单");
        PageResult pageResult = orderService.getHistoryOrders(pageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 取消订单
     * @param id
     * @return
     * @throws Exception
     */
    @PutMapping("/cancel/{id}")
    public Result userCancel(@PathVariable Long id) throws Exception{
        log.info("用户取消订单：{}",id);
        orderService.userCancel(id);
        return Result.success();
    }


    /**
     * 再来一单
     * @param id
     * @return
     */
    @PostMapping("/repetition/{id}")
    public Result repetition(@PathVariable Long id) throws Exception{
        log.info("用户重复点餐：{}",id);
        orderService.repetition(id);
        return Result.success();
    }


    /**
     * 催单
     * @param id
     * @return
     */
    @GetMapping("/reminder/{id}")
    public Result reminder(@PathVariable Long id){
        log.info("用户催单：{}",id);
        orderService.reminder(id);
        return Result.success();
    }

}
