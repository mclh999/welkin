package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.page(setmealPageQueryDTO);
        return new PageResult((page.getTotal()), page.getResult());
    }

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SetmealDTO setmealDTO) {
        //创建套餐对象
        com.sky.entity.Setmeal setmeal = new com.sky.entity.Setmeal();

        //拷贝DTO数据
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmeal.setStatus(StatusConstant.DISABLE);//默认停售

        //插入套餐表
        setmealMapper.insert(setmeal);

        //获取菜品数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        //获取套餐ID,记得主键返回
        Long setmealId = setmeal.getId();

        //插入菜品表
        if(!setmealDishes.isEmpty()){
            setmealDishes.forEach(setmealDish->setmealDish.setSetmealId(setmealId));
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 修改套餐状态
     * @param status
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Integer status, Long id) {
        //通过传参判断当前套餐是否未启售
        if(status==StatusConstant.DISABLE){
            //启售改成停售,直接更改
            com.sky.entity.Setmeal setmeal = com.sky.entity.Setmeal.builder()
                    .id(id)
                    .status(status)
                    .build();
            setmealMapper.update(setmeal);
        }else if(status==StatusConstant.ENABLE){//停售改成启售
            //获取菜品信息
            List<SetmealDish> dishList = setmealDishMapper.getDishBySetmealId(id);

            dishList.forEach((SetmealDish setmealDish)-> {
                //获取菜品ID
                Long dishId = setmealDish.getDishId();
                //获取菜品售卖状态
                Dish dish = dishMapper.getById(dishId);
                    //判断套餐内是否有未启售菜品
                    if(dish.getStatus()==StatusConstant.DISABLE){
                        throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
            });

            //更新状态
            com.sky.entity.Setmeal setmeal = com.sky.entity.Setmeal.builder()
                    .id(id)
                    .status(status)
                    .build();
            setmealMapper.update(setmeal);

        }

    }

    /**
     * 批量删除
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        //启售不能删除
        for (Long id : ids) {
            com.sky.entity.Setmeal setmeal = setmealMapper.getById(id);
            if(setmeal.getStatus()==StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        //删除套餐
        setmealMapper.deleteByIds(ids);

        //删除关联的菜品数据
        setmealDishMapper.deleteByIds(ids);
    }

    /**
     * 根据ID查询套餐
     * @param id
     * @return
     */
    @Override
    public SetmealVO getById(Long id) {
        com.sky.entity.Setmeal setmeal = setmealMapper.getById(id);
        //获取菜品信息
        List<SetmealDish> setmealDishes = setmealDishMapper.getDishBySetmealId(id);

        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SetmealDTO setmealDTO) {
        com.sky.entity.Setmeal setmeal = new com.sky.entity.Setmeal();

        BeanUtils.copyProperties(setmealDTO, setmeal);

        setmealMapper.update(setmeal);
        //先删除菜品数据
        setmealDishMapper.deleteById(setmeal.getId());

        //获取菜品信息
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        //获取套餐ID
        Long setmealId = setmeal.getId();

        //判断是否为空
        if(!setmealDishes.isEmpty()){
            setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealId));
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 根据分类ID查询套餐信息
     * @param setmeal
     * @return
     */
    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        return setmealMapper.list(setmeal);
    }


    /**
     * 获取套餐详情
     * @param id
     * @return
     */
    @Override
    public List<DishItemVO> getDishItems(Long id) {
        List<DishItemVO> dishVOList = dishMapper.getDishItems(id);
        return dishVOList;
    }
}
