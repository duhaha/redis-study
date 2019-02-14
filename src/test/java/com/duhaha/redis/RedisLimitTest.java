package com.duhaha.redis;

import com.duhaha.redis.limit.RedisSimpleCurrentLimit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

import java.io.IOException;

/**
 * @Author yym
 * @Date 2019/1/31 16:37
 * @Description
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RedisStudyApplication.class)
public class RedisLimitTest {

    @Autowired
    private RedisSimpleCurrentLimit redisSimpleCurrentLimit;

    @Test
    public void test() throws IOException {
        redisSimpleCurrentLimit.isAllowed("name", "hello", 60, 5);
    }
}
