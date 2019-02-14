package com.duhaha.redis.distributed_lock;

/**
 * @Author yym
 * @Date 2019/1/30 15:43
 * @Description redis分布式锁
 */
public interface DistributionLock {

    public boolean lock(String lockKey, Long expire);

    public boolean releaseLock(String key, String requestId);
}
