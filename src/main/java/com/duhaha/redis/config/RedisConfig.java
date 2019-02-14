package com.duhaha.redis.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.Jedis;

/**
 * @Author yym
 * @Date 2019/1/29 13:45
 * @Description
 */

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private Integer port;
    @Value("${spring.redis.timeout}")
    private Integer timeout;

    @Bean
    @Autowired
    @SuppressWarnings("unchecked")
    public RedisTemplate redisTemplate(RedisTemplate redisTemplate){
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public Jedis jedis(){
        return new Jedis(host, port, timeout);
    }

    @Bean
    @Autowired
    public JedisConnection jedisConnection(Jedis jedis){
        return new JedisConnection(jedis);
    }

}
