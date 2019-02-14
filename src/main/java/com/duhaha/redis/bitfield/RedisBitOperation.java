package com.duhaha.redis.bitfield;

import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

/**
 * @Author yym
 * @Date 2019/1/31 11:20
 * @Description redis的位图操作
 * 首先要明白位图不是什么神奇的东西，其本质上就是一个字符串，只不过是操作字符串对应的ASCII码的二进制形式罢了
 */
public class RedisBitOperation {

    @Autowired
    private Jedis jedis;

    public void bit(){
        jedis.setbit("a", 0, "hello");
    }
}
