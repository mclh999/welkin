package com.sky.controller.user;

import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.CategorypageDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 分类管理
 */
@Slf4j
@RequestMapping("/user/category")
@RestController("userCategoryController")
@Api(tags = "分类管理")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 根据类型查询
     * @param type
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据类型查询")
    public Result<List<Category>> list(Integer type){
        log.info("根据类型查询：{}", type);
        //直接返回一个集合
        List<Category> categoryList = categoryService.list(type);
        return Result.success(categoryList);
    }


}
