package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorsMapper {

    /**
     * 批量插入口味数据
     * @param dishFlavors
     */

    void insertBatch(List<DishFlavor> dishFlavors);

    /**
     * 根据菜品id查询菜品口味数据
     * @param id
     * @return
     */
    List<DishFlavor> getDishFlavorByDishId(Long id);

    /**
     * 根据id删除菜品口味数据
     * @param dishId
     */
    void deleteById(Long dishId);

    /**
     * 根据菜品id批量删除菜品口味数据
     * @param ids
     */
    void deleteByDishIds(List<Long> ids);
}
