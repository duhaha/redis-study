package com.duhaha.redis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author yym
 * @Date ${Date} ${Time}
 * @Description
 */
@Service
public class RedisService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void setValue() {
        redisTemplate.opsForValue().set("test", "duhaha");
    }

    public Object getValue() {
        return redisTemplate.opsForValue().get("test");
    }

    /**
     * 使用redis scan代替keys
     * 注意Cursor一定不能关闭，在之前的版本中，这里Cursor需要手动关闭，但是从1.8.0开始，不能手动关闭！否则会报异常。
     */
    @SuppressWarnings("unchecked")
    public List<Object> scanCursor() {
        return (List<Object>) redisTemplate.execute(new RedisCallback<List<Object>>() {
            @Override
            public List<Object> doInRedis(RedisConnection connection) throws DataAccessException {
                List<Object> list = new ArrayList<>();
                Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match("dba_*").count(5).build());
                while (cursor.hasNext()) {
                    list.add(new String(cursor.next()));
                }
                return list;
            }
        });
    }

    /**
     * 使用redis的pipelined(流水线)实现批量的读写，可以节省很多连接redis的时间
     * 注意：用pipeline方式打包命令发 送，redis必须在处理完所有命令前先缓存起所有命令的处理结果。打包的命令越多，缓存消耗内存也越多。
     * <p>
     * Redis本身是基于Request/Response协议的，客户端发送一个命令，等待Redis应答，Redis在接收到命令，处理后应答。
     * 其中发送命令加上返回结果的时间称为(Round Time Trip)RRT-往返时间。如果客户端发送大量的命令给Redis，
     * 那就是等待上一条命令应答后再执行再执行下一条命令，这中间不仅仅多了RTT，而且还频繁的调用系统IO，发送网络请求。
     */
    public List<Object> redisPipelined() {
        return stringRedisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                for (int i = 20; i < 50; i++) {
                    stringRedisTemplate.opsForValue().set("dba_" + i, i + "");
                }
                return null;
            }
        });
    }
}
