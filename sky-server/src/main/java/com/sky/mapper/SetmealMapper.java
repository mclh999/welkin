package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    Page<Setmeal> page(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 插入数据
     * @param setmeal
     */
    void insert(Setmeal setmeal);

    /**
     * 修改套餐
     * @param setmeal
     */
    void update(Setmeal setmeal);

    /**
     * 根据id查询
     * @param id
     * @return
     */
    Setmeal getById(Long id);

    /**
     * 批量删除
     * @param ids
     */
    void deleteByIds(List<Long> ids);
}
