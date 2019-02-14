package com.duhaha.redis.transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

/**
 * @Author yym
 * @Date 2019/2/14 14:27
 * @Description redis事务
 * redis事务模型并不严格，这样就不能像关系型数据库一样使用事务
 * redis事务根本不能算是[原子性]，而仅仅是满足了[隔离性]
 */
@Component
public class RedisTransaction {
    @Autowired
    private Jedis jedis;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 简单的实现redis 的事务（仅仅展示redis的事务命令）
     * 所有的指令在 exec 之前不执行，而是缓存在服务器的一个事务队列中，服务器一旦收到 exec 指令，才开执行整个事务队列，执行完毕
     * 后一次性返回所有指令的运行结果。因为 Redis 的单线程特性，它不用担心自己在执行队列的时候被其它指令打搅
     * <p>
     * 【注意】：redis每个原子性操作之间的事务是不可靠的，也就是说多个原子性操作之间的一致性不能保证
     */
    public void simpleTransaction() {
        Transaction transaction = jedis.multi();

        try {
            //事务之间的代码coding

            //执行
            transaction.exec();
        } catch (Exception ex) {
            //回滚
            transaction.discard();
        }
    }

    /**
     * 由于redis的事务会存在并发的问题，可以使用分布式锁（悲观锁）来解决redis的并发问题
     * 还有一种乐观锁也可以解决，redis的watch机制
     * <p>
     * 简介：watch会在事务开始之前关注一个或多个变量，并在exec执行缓存队列中的指令的时候，检查变量在watch之后是否发生变化
     * 如果没有变化执行成功，如果发生变化exec会返回null，通知客户端执行失败
     * <p>
     * 案例：
     * > watch books
     * OK
     * > incr books # 被修改了
     * (integer) 1
     * > multi
     * OK
     * > incr books
     * QUEUED  #这个返回代表将指令放入到事务队列中等待exec执行
     * > exec # 事务执行失败
     * (nil)
     * <p>
     * 【注意】：Redis 禁止在 multi 和 exec 之间执行 watch 指令，而必须在 multi 之前做好盯住关键变量，否则会出错。
     */
    public void redisWatch(String key) {
        jedis.setnx(key, "2"); //初始化

        jedis.watch(key);
        String value = jedis.get(key);
        value = Integer.valueOf(value) * 2 + "";

        //更改key的值，使事务失败
        jedis.set(key, "1");

        //注意 在multi中不能使用jedis，添加命令到redis的队列需要使用transaction
        Transaction transaction = jedis.multi();
        transaction.set(key, value);
        List<Object> exec = transaction.exec();
        System.err.println(exec);
    }
}
