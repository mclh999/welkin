package com.sky.mapper;

import com.sky.entity.Dish;
import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {


    /**
     * 根据菜品id查询套餐id
     * @param dishIds
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /**
     * 批量插入套餐和菜品的关联关系
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id查询关联的菜品信息
     * @param id
     * @return
     */
    List<SetmealDish> getDishBySetmealId(Long id);

    /**
     * 批量删除
     * @param dishIds
     */
    void deleteByIds(List<Long> dishIds);

    /**
     * 根据id删除
     * @param id
     */
    void deleteById(Long id);
}
