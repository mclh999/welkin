package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderDetailMapper {
    /**
     * 批量插入订单明细数据
     * @param orderDetailList
     */
    void insertBatch(List<OrderDetail> orderDetailList);

    /**
     * 根据订单id查询订单明细
     * @param id
     * @return
     */
    List<OrderDetail> getByOrderId(Long id);

    /**
     * 根据ID统计菜品数量
     * @param map
     * @return
     */
    List<Map<String, Object>> getDishTop(Map map);

    List<GoodsSalesDTO> getDishTop2(LocalDateTime beginTime, LocalDateTime endTime);
}
