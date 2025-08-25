package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface UserMapper {
    /**
     * 根据openid查询用户
     * @param openId
     * @return
     */
    User getByOpenId(String openId);

    /**
     * 插入用户数据
     * @param user
     */
    void insert(User user);

    /**
     * 根据条件统计用户数量
     * @param map
     * @return
     */
    Integer getUserStatistics(Map map);
}
