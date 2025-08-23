package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.CategorypageDTO;
import com.sky.entity.Category;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        Page<Category> page = categoryMapper.page(categoryPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 修改分类状态
     * @param status
     * @param id
     */
    @Override
    public void updateStatus(Integer status, long id) {
        Category category = Category.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        categoryMapper.update(category);
    }

    /**
     * 新增分类
     * @param categorypageDTO
     */
    @Override
    public void save(CategorypageDTO categorypageDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categorypageDTO, category);
//        // 设置默认值
//        category.setCreateTime(LocalDateTime.now());
//        category.setUpdateTime(LocalDateTime.now());
//
//        category.setCreateUser(BaseContext.getCurrentId());
//        category.setUpdateUser(BaseContext.getCurrentId());

        category.setStatus(StatusConstant.ENABLE);

        categoryMapper.insert(category);
    }

    /**
     * 分类型查询
     * @param type
     * @return
     */
    @Override
    public List<Category> list(Integer type) {return categoryMapper.list(type);}

    /**
     * 修改分类
     * @param categorypageDTO
     */
    @Override
    public void update(CategorypageDTO categorypageDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categorypageDTO, category);

        category.builder()
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        categoryMapper.update(category);
    }

    /**
     * 删除分类
     * @param id
     */
    @Override
    public void delete(Long id) {
        //查询当前分类是否关联了菜品，如果关联了就抛出业务异常
        Integer count = dishMapper.countByCategoryId(id);
        if(count > 0){
            //当前分类下有菜品，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }

        //查询当前分类是否关联了套餐，如果关联了就抛出业务异常
        count = setmealMapper.countByCategoryId(id);
        if(count > 0){
            //当前分类下有菜品，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        //删除分类数据
        categoryMapper.deleteById(id);
    }
}
