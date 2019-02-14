package com.duhaha.redis.service;

import com.duhaha.redis.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Author yym
 * @Date 2019/1/30 13:07
 * @Description 书籍作业
 */
@Service
public class PracticeCase {
    @Autowired
    private RedisTemplate redisTemplate;

    public void saveUser(User user) {
        try {
            Map<String, Object> map = ObjectUtils.convert2Map(user);
            map.put("age", map.get("age").toString());
            redisTemplate.opsForHash().putAll("user", map);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    public Map<String, Object> getUser() {
        Map<String, Object> map = redisTemplate.opsForHash().entries("user");
        return map;
    }


    public String getValue(String name) {
        return (String) redisTemplate.opsForValue().get(name);
    }

}
