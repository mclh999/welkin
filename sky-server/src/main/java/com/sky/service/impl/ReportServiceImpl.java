package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        //封装每天的时间
        List<LocalDate> dateTimeList = new ArrayList<>();
        dateTimeList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateTimeList.add(begin);
        }

        //将日期转成string
        String dateList = StringUtils.join(dateTimeList, ",");

        //查询每天营业额
        List<Double> list = new ArrayList<>();
        for (LocalDate dateTime : dateTimeList) {
            //获取一天的间隔时间
            LocalDateTime beginTime = LocalDateTime.of(dateTime, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(dateTime, LocalTime.MAX);

            //将参数封装成map，以便复用
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);

            Double turnover = orderMapper.getTurnover(map);
            //如果为空就赋0
            turnover = turnover == null ? 0.0 : turnover;

            list.add(turnover);
        }

        String turnoverList = StringUtils.join(list, ",");

        TurnoverReportVO turnoverReportVO = TurnoverReportVO.builder()
                .dateList(dateList)
                .turnoverList(turnoverList)
                .build();

        return turnoverReportVO;
    }

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateTimeList = new ArrayList<>();
        dateTimeList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateTimeList.add(begin);
        }

        String dateList = StringUtils.join(dateTimeList, ",");

        //新增用户
        List<Integer> newList = new ArrayList<>();
        //总用户
        List<Integer> totalList = new ArrayList<>();

        for (LocalDate dateTime : dateTimeList) {
            LocalDateTime beginTime = LocalDateTime.of(dateTime, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(dateTime, LocalTime.MAX);

            Map map = new HashMap();
            map.put("end",endTime);
            //查询总用户数量
            Integer totalUser = userMapper.getUserStatistics(map);
            totalUser = totalUser == null ? 0 : totalUser;
            totalList.add(totalUser);

            //查询新用户数量
            map.put("begin", beginTime);
            Integer newUser = userMapper.getUserStatistics(map);
            newUser = newUser == null ? 0 : newUser;
            newList.add(newUser);
        }
        String totalUserList = StringUtils.join(totalList, ",");
        String newUserList = StringUtils.join(newList, ",");

        UserReportVO userReportVO = UserReportVO.builder()
                .dateList(dateList)
                .totalUserList(totalUserList)
                .newUserList(newUserList)
                .build();

        return userReportVO;

    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateTimeList = new ArrayList<>();
        dateTimeList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateTimeList.add(begin);
        }

        String dateList = StringUtils.join(dateTimeList, ",");

        //统计每日订单数
        List<Integer> orderCountListT = new ArrayList<>();
        //统计每日订单完成数
        Integer validOrderCount = 0;
        //统计每日完成订单数
        List<Integer> completedOrderCountListT = new ArrayList<>();

        for (LocalDate dateTime : dateTimeList) {
            LocalDateTime beginTime = LocalDateTime.of(dateTime, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(dateTime, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            //获取每日订单总数
            Integer orderCountT = orderMapper.getOrderCount(map);
            orderCountT = orderCountT == null? 0 : orderCountT;

            orderCountListT.add(orderCountT);

            //获取每日订单完成数
            map.put("status", Orders.COMPLETED);
            Integer validOrderCountT = orderMapper.getOrderCount(map);
            validOrderCountT = validOrderCountT == null ? 0 : validOrderCountT;
            validOrderCount += validOrderCountT;

            completedOrderCountListT.add(validOrderCountT);

        }

        //统计订单总数,方法引用，stream流
        Integer totalOrderCount = orderCountListT.stream().reduce(Integer::sum).get();

        Double orderCompletionRate = 0.0;
        //判断订单总数是否为空
        if(totalOrderCount != 0){
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        String orderCountList = StringUtils.join(orderCountListT, ",");
        String validOrderCountList = StringUtils.join(completedOrderCountListT, ",");

        return   OrderReportVO.builder()
                .dateList(dateList)
                .orderCountList(orderCountList)
                .validOrderCountList(validOrderCountList)
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();

    }

    /**
     * 销量排名10
     * @param begin
     * @param end
     */
    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        //解法一：
//        Map map = new HashMap();
//        map.put("begin", beginTime);
//        map.put("end", endTime);
//
//        //统计订单中的菜品名称和数量
//        List<Map<String,Object>> dishList = orderDetailMapper.getDishTop(map);
//        //统计菜品名称
//        List<String> nameList = dishList.stream().map(x -> x.get("name").toString()).limit(10).collect(Collectors.toList());
//        //统计菜品数量
//        List<Integer> numberList = dishList.stream().map(x -> Integer.valueOf(x.get("number").toString())).limit(10).collect(Collectors.toList());

        //解法二
        List<GoodsSalesDTO> dishList = orderDetailMapper.getDishTop2(beginTime, endTime);
        List<String> nameList = dishList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = dishList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();

    }
}
