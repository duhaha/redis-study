package com.duhaha.redis;

import com.duhaha.redis.scan.RedisScan;
import com.duhaha.redis.service.PracticeCase;
import com.duhaha.redis.service.RedisService;
import com.duhaha.redis.service.User;
import com.duhaha.redis.transaction.RedisTransaction;
import com.duhaha.redis.utils.ObjectUtils;
import com.duhaha.redis.utils.RedisUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

/**
 * @Author yym
 * @Date 2019/1/29 11:39
 * @Description
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RedisStudyApplication.class)
public class RedisTest {

    @Autowired
    private RedisService redisService;
    @Autowired
    private PracticeCase practiceCase;

    @Test
    public void testSetValue() {
        redisService.setValue();
    }

    @Test
    public void testGetValue() {
        Object value = redisService.getValue();
        System.err.println(value);
    }

    @Test
    public void testRedisScan() {
        List<Object> values = redisService.scanCursor();
        for (Object value : values) {
            System.err.println(value);
        }
    }

    @Test
    public void testPipelined() {
        List<Object> objects = redisService.redisPipelined();
    }


    @Test
    public void testHash() throws InterruptedException {
        //User user = new User("duhaha", 23);
        //practiceCase.saveUser(user);

        Thread.sleep(1000);
        Map<String, Object> map = practiceCase.getUser();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            System.err.println(entry.getKey() + "=" + entry.getValue());
        }
    }

    @Autowired
    private RedisScan redisScan;

    @Test
    public void testJedisScan() {
        redisScan.scan("user*", 10);
    }

    @Test
    public void testJedisConnectionScan(){
        redisScan.jedisConnectionScan("user*", 10);
    }

    @Autowired
    private RedisTransaction redisTransaction;
    @Test
    public void testRedisWatch(){
        redisTransaction.redisWatch("books");
    }
}
