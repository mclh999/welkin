package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorsMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorsMapper dishFlavorsMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;


    /**
     * 新增菜品及口味
     * @param dishDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)//添加事务
    public void save(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //向菜品表插入数据
        dishMapper.insert(dish);
        //获取口味表
        List<DishFlavor> dishFlavors = dishDTO.getFlavors();
        //向口味表插入数据
        if(!dishFlavors.isEmpty()){
            dishFlavors.forEach(flavor->flavor.setDishId(dish.getId()));
            dishFlavorsMapper.insertBatch(dishFlavors);
        }

    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 菜品起售停售
     * @param status
     * @param id
     */
    @Override
    public void updateStatus(Integer status, long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();

        dishMapper.update(dish);
    }

    /**
     * 根据id查询菜品和对应的口味数据
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DishVO getById(long id) {
        Dish dish = dishMapper.getById(id);//与删除功能复用
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);

        List<DishFlavor> dishFlavor = dishFlavorsMapper.getDishFlavorByDishId(id);
        dishVO.setFlavors(dishFlavor);
        return dishVO;
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
        //先删除口味数据
        dishFlavorsMapper.deleteById(dish.getId());
        //再添加新的口味数据
        List<DishFlavor> dishFlavors = dishDTO.getFlavors();
        if(!dishFlavors.isEmpty()){
            dishFlavors.forEach(flavor->flavor.setDishId(dish.getId()));
            dishFlavorsMapper.insertBatch(dishFlavors);
        }
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        //起售菜品不能删除
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                //菜品处于起售中
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //套餐菜品不能删除
        List<Long>setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if(!setmealIds.isEmpty()){//存在与套餐关联的菜品
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

//        //删除菜品数据
//        for (Long id : ids) {
//            dishMapper.deleteById(id);//直接遍历删除
//            //删除口味数据
//            dishFlavorsMapper.deleteById(id);
//        }

//        优化：删除菜品数据和口味数据
        dishMapper.deleteByIds(ids);
        dishFlavorsMapper.deleteByDishIds(ids);
    }

    /**
     * 根据分类id查询批量菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> list(Long categoryId) {return dishMapper.getByCategoryId(categoryId);}

    /**
     * 根据分类id批量查询菜品和口味
     * @param dish
     * @return
     */
    @Override
    public List<DishVO> listWithFlavor(Dish dish) {
        //查询菜品数据
        List<Dish> dishList = list(dish.getCategoryId());

        //创建DishVO集合对象，用于封装DishVO数据
        List<DishVO> dishVOList = new ArrayList();

        //遍历菜品集合，获取每个菜品的口味
        for (Dish d : dishList) {
            //创建DisshVO对象
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //给DishVO设置对应的口味数据
            List<DishFlavor> dishFlavors = dishFlavorsMapper.getDishFlavorByDishId(d.getId());
            dishVO.setFlavors(dishFlavors);

            //封装集合
            dishVOList.add(dishVO);
        }
        //返回DishVO集合
        return dishVOList;
    }
}
