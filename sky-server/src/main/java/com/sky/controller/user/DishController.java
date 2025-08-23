package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController("userDishController")
@RequestMapping("/user/dish")
@Api(tags = "菜品管理")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 根据分类id查询菜品和对应的口味
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    public Result<List<DishVO>> getById(Long categoryId){
        log.info("获取菜品信息：{}", categoryId);

        //构造redis的key值---dish_分类ID
        String key = "dish_" + categoryId;

        //判断缓存中是否存在相应的菜品
        List<DishVO> dishVOList = (List<DishVO>) redisTemplate.opsForValue().get(key);

        //有就直接返回
        if(dishVOList != null && dishVOList.size() > 0){
            return Result.success(dishVOList);
        }

        //没有就查询数据库并添加到缓存中
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);

        dishVOList = dishService.listWithFlavor(dish);
        redisTemplate.opsForValue().set(key, dishVOList);

        return Result.success(dishVOList);
    }

}
