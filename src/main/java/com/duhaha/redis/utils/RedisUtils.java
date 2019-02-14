package com.duhaha.redis.utils;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * redis工具类
 */
@Component
public class RedisUtils {

    @Autowired
    public RedisTemplate redisTemplate;
}
