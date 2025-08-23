package com.sky.service;

import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.CategorypageDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService {
    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    PageResult page(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 分类状态变更
     * @param status
     * @param id
     */
    void updateStatus(Integer status, long id);

    /**
     * 新增分类
     * @param categorypageDTO
     */
    void save(CategorypageDTO categorypageDTO);

    /**
     * 分类型查询
     * @param type
     * @return
     */
    List<Category> list(Integer type);

    /**
     * 修改分类
     * @param categorypageDTO
     */
    void update(CategorypageDTO categorypageDTO);

    /**
     * 删除分类
     * @param id
     */
    void delete(Long id);
}
