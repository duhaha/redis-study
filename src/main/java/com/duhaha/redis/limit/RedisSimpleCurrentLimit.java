package com.duhaha.redis.limit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.io.IOException;

/**
 * @Author yym
 * @Date 2019/1/31 15:52
 * @Description redis简单限流
 * 思路：zset + score  实现
 * 每一个行为到来时，都维护一次时间窗口。将时间窗口外的记录全部清理掉，只保留窗口内的记录。
 * 缺点：一定时间内的允许请求次数非常大时，不适合使用zset来实现，因为会消耗大量的存储内存
 */
@Component
public class RedisSimpleCurrentLimit {

    @Autowired
    private Jedis jedis;

    //public RedisSimpleCurrentLimit() {
    //    jedis = new Jedis("127.0.0.1", 6379, 0);
    //}

    /**
     * 简单限流
     *
     * @param userId    用户
     * @param actionKey 行为key
     * @param period    时间窗口大小
     * @param maxCount  最大请求数
     * @return
     * @throws IOException
     */
    public boolean isAllowed(String userId, String actionKey, Integer period, Integer maxCount) throws IOException {
        String key = String.format("hist:%s:%s", userId, actionKey);
        Pipeline pipe = jedis.pipelined();
        long nowTs = System.currentTimeMillis();

        pipe.multi();
        pipe.zadd(key, nowTs, "" + nowTs);
        pipe.zremrangeByScore(key, 0, nowTs - period * 1000);
        Response<Long> count = pipe.zcard(key);
        pipe.expire(key, period+1);
        pipe.exec();
        pipe.close();

        Long aLong = count.get();
        System.err.println(aLong);
        return aLong <= maxCount;
    }
}
