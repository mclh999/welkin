package com.sky.controller.admin;

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
@RequestMapping("/admin/category")
@RestController
@Api(tags = "分类管理")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 分类查询
      * @param categoryPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("分页查询");
        PageResult pageResult = categoryService.page(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 修改分类状态
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("修改分类状态")
    public Result updateStatus(@PathVariable Integer status, long id){
        log.info("员工账号状态：{},员工id：{}", status, id);
        categoryService.updateStatus(status, id);
        return Result.success();
    }

    /**
     * 新增分类
     * @param categorypageDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增分类")
    public Result save(@RequestBody CategorypageDTO categorypageDTO){
        log.info("新增分类：{}", categorypageDTO);
        categoryService.save(categorypageDTO);
        return Result.success();
    }

    /**
     * 根据类型查询
     * @param type
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据类型查询")//用于新增和修改菜品时的分类选择
    public Result<List<Category>> list(Integer type){
        log.info("根据类型查询：{}", type);
        //直接返回一个集合
        List<Category> categoryList = categoryService.list(type);
        return Result.success(categoryList);
    }

    /**
     * 修改分类
     * @param categorypageDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改分类")
    public Result update(@RequestBody CategorypageDTO categorypageDTO){
        log.info("修改分类：{}", categorypageDTO);
        categoryService.update(categorypageDTO);
        return Result.success();
    }

    /**
     * 删除分类
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("删除分类")
    public Result delete(Long id){
        log.info("删除分类：{}", id);
        categoryService.delete(id);
        return Result.success();
    }
}
