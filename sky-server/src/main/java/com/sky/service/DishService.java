package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品及口味
     * @param dishDTO
     */
    void save(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 菜品起售停售
     * @param status
     * @param id
     */
    void updateStatus(Integer status, long id);

    /**
     * 根据id查询菜品和对应的口味
     * @param id
     * @return
     */
    DishVO getById(long id);

    /**
     * 修改菜品
     * @param dishDTO
     */
    void update(DishDTO dishDTO);

    /**
     * 批量删除菜品
     * @param ids
     */
    void delete(List<Long> ids);

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    List<Dish> list(Long categoryId);

    /**
     * 根据分类id查询批量菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
