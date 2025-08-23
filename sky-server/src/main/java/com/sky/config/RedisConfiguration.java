package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
public class RedisConfiguration {

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
        log.info("创建redis模板类...");
        //创建redis模板类
        RedisTemplate redisTemplate = new RedisTemplate();;
        //设置redis的key序列化器,让key值的显示不是乱码
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //设置redis连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }
}
