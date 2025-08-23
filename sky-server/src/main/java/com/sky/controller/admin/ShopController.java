package com.sky.controller.admin;

import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")//指定名称，防止bean与用户端重名报错
@RequestMapping("/admin/shop")
@Slf4j
public class ShopController {
    public static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 修改店铺营业状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    public Result updateStatus(@PathVariable Integer status){
        log.info("更新店铺营业状态：{}", status);
        redisTemplate.opsForValue().set(KEY,status);
        return Result.success();
    }

    /**
     * 获取店铺营业状态
     * @return
     */
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        log.info("查询店铺营业状态");
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        return Result.success(status);
    }
}
