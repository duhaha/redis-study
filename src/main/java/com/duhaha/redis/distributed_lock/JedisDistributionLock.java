package com.duhaha.redis.distributed_lock;

import redis.clients.jedis.Jedis;

import java.util.Collections;

/**
 * @Author yym
 * @Date 2019/1/30 17:40
 * @Description
 * 分布式锁需要满足的四个条件：
 *  1、互斥性：在任意时刻，只有一个客户端能持有锁。
 *  2、不会发生死锁：即使有一个客户端在持有锁的期间崩溃而没有主动解锁，也能保证后续其他客户端能加锁。
 *  3、具有容错性：只要大部分的Redis节点正常运行，客户端就可以加锁和解锁。
 *  4、解铃还须系铃人：加锁和解锁必须是同一个客户端，客户端自己不能把别人加的锁给解了。
 *
 *  加锁失败的解决办法：
 *  1、 直接抛出异常，通知用户稍后重试；
 *  2、 sleep 一会再重试；
 *  3、 将请求转移至延时队列，过一会再试；  类似com.duhaha.redis.delay_queue.RedisDelayingQueue
 */
public class JedisDistributionLock{

    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";   //毫秒   EX-秒
    private static final Long RELEASE_SUCCESS = 1L;

    /**
     * 加锁
     * @param jedis
     * @param lockKey key
     * @param requestId 用于区分客户端的value
     * @param expireTime 过期时间
     * @return
     */
    public boolean lock(Jedis jedis, String lockKey, String requestId, int expireTime) {
        String code = jedis.set(lockKey, requestId, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
        return LOCK_SUCCESS.equals(code);
    }

    /**
     * 释放锁
     * @param jedis
     * @param lockKey
     * @param requestId
     * @return
     * 注意：释放锁的时候使用Lua脚本实现，为了保证操作的原子性，在加锁的时候设置了一个数值。
     * 只有在匹配成功的时候才能释放锁，但是匹配了释放不能保证原子性，因此使用Lua脚本保证联系多个指令的原子性操作。
     */
    public boolean releaseLock(Jedis jedis, String lockKey, String requestId) {
        //lua脚本语句
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));

        if (RELEASE_SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }
}
