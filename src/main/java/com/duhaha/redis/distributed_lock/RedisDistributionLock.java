package com.duhaha.redis.distributed_lock;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @Author yym
 * @Date 2019/1/30 15:44
 * @Description  分布式锁的实现方案
 * 也可以参考这篇文章，使用jedis实现：https://www.cnblogs.com/linjiqin/p/8003838.html
 */
@Component
public class RedisDistributionLock implements DistributionLock {
    private static final Logger LOGGER = Logger.getLogger(RedisDistributionLock.class);
    @Autowired
    private RedisTemplate redisTemplate;

    //lua语法
    public static final String UNLOCK_LUA;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("if redis.call(\"get\",KEYS[1]) == ARGV[1] ");
        sb.append("then ");
        sb.append("    return redis.call(\"del\",KEYS[1]) ");
        sb.append("else ");
        sb.append("    return 0 ");
        sb.append("end ");
        UNLOCK_LUA = sb.toString();
    }

    @Override
    public boolean lock(String lockKey, Long expire) {
        try {
            String code = (String) redisTemplate.execute((RedisCallback) connection -> {
                JedisCommands commands = (JedisCommands) connection.getNativeConnection();
                String uuid = UUID.randomUUID().toString();
                return commands.set(lockKey, uuid, "NX", "PX", expire);
            });

            return !StringUtils.isEmpty(code);
        } catch (Exception ex) {
            LOGGER.info("set redis occur an exception", ex);
        }
        return false;
    }

    public String get(String key) {
        try {
            RedisCallback<String> callback = (connection) -> {
                JedisCommands commands = (JedisCommands) connection.getNativeConnection();
                return commands.get(key);
            };
            String result = (String) redisTemplate.execute(callback);
            return result;
        } catch (Exception e) {
            LOGGER.error("get redis occured an exception", e);
        }
        return "";
    }


    public boolean releaseLock(String key, String requestId) {
        // 释放锁的时候，有可能因为持锁之后方法执行时间大于锁的有效期，此时有可能已经被另外一个线程持有锁，所以不能直接删除
        try {
            List<String> keys = new ArrayList<>();
            keys.add(key);
            List<String> args = new ArrayList<>();
            args.add(requestId);

            // 使用lua脚本删除redis中匹配value的key，可以避免由于方法执行时间过长而redis锁自动过期失效的时候误删其他线程的锁
            // spring自带的执行脚本方法中，集群模式直接抛出不支持执行脚本的异常，所以只能拿到原redis的connection来执行脚本
            RedisCallback<Long> callback = (connection) -> {
                Object nativeConnection = connection.getNativeConnection();
                // 集群模式和单机模式虽然执行脚本的方法一样，但是没有共同的接口，所以只能分开执行
                // 集群模式
                if (nativeConnection instanceof JedisCluster) {
                    return (Long) ((JedisCluster) nativeConnection).eval(UNLOCK_LUA, keys, args);
                }

                // 单机模式
                else if (nativeConnection instanceof Jedis) {
                    return (Long) ((Jedis) nativeConnection).eval(UNLOCK_LUA, keys, args);
                }
                return 0L;
            };
            Long result = (Long) redisTemplate.execute(callback);

            return result != null && result > 0;
        } catch (Exception e) {
            LOGGER.error("release lock occured an exception", e);
        } finally {
            // 清除掉ThreadLocal中的数据，避免内存溢出
            //lockFlag.remove();
        }
        return false;
    }
}
