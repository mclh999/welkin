package com.sky.controller.user;

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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController("userDishController")
@RequestMapping("/user/dish")
@Api(tags = "菜品管理")
public class DishController {
    @Autowired
    private DishService dishService;


    /**
     * 根据分类id查询菜品和对应的口味
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    public Result<List<DishVO>> getById(Long categoryId){
        log.info("获取菜品信息：{}", categoryId);
        List<DishVO> dishVOList = dishService.listWithFlavor(categoryId);
        return Result.success(dishVOList);
    }

}
