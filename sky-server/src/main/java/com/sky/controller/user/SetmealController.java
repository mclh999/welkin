package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;


    /**
     * 点击分类查询套餐
     * 根据分类id批量查询套餐
      * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @CachePut(cacheNames = "SetmealCache", key = "#categoryId")//SetmealCache::categoryId
    public Result<List<Setmeal>> list(Long categoryId) {
        log.info("查询套餐：{}", categoryId);
        Setmeal setmeal = new Setmeal();
        setmeal.setCategoryId(categoryId);
        setmeal.setStatus(StatusConstant.ENABLE);

        List<Setmeal> setmealList = setmealService.list(setmeal);
        return Result.success(setmealList);
    }

    /**
     * 点击套餐，查询菜品
     * 获取套餐详情
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    public Result<List<DishItemVO>> getDishItems(@PathVariable Long id){
        log.info("查询套餐菜品：{}", id);
        List<DishItemVO> dishVOList = setmealService.getDishItems(id);
        return Result.success(dishVOList);
    }

}
