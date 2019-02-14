package com.duhaha.redis.pipeline;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author yym
 * @Date 2019/2/13 19:48
 * @Description redis管道
 *
 * pipeline并不是redis server的特性，而是客户端通过改变读写的顺序导致的服务器性能的巨大提升。
 * 具体的原理是，网络传输引发的开销，参考书籍理解。
 */
public class RedisPipeline {

    @Autowired
    private Jedis jedis;

    @Autowired
    private RedisTemplate redisTemplate;

    @SuppressWarnings("unchecked")
    public void redisTemplatePipeline1() {
        redisTemplate.execute((RedisCallback) connection -> {
            connection.openPipeline();
            for (int i = 0; i < 10; i++) {
                redisTemplate.opsForValue().set("pipelineOne" + i, String.valueOf(i));
            }
            List<Object> objects = connection.closePipeline();
            System.err.println(objects);
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    public void redisTemplatePipeline2() {
        redisTemplate.executePipelined((RedisCallback) connection -> {
            for (int i = 0; i < 10; i++) {
                redisTemplate.opsForValue().set("pipelineTwo" + i, String.valueOf(i));
            }
            return null;
        });
    }

    /**
     * jedis中对redis管道的实现
     * @return
     */
    public Map<String, String> jedisPipeline() {
        Pipeline pipelined = jedis.pipelined();
        Map<String, Response<String>> map = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            map.put("pipelineOne" + i, pipelined.get("pipelineOne" + i));
        }
        //读取所有的结果并关闭管道
        pipelined.sync();

        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, Response<String>> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue().get());
        }

        return result;
    }
}
