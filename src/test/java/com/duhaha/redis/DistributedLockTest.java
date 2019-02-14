package com.duhaha.redis;

import com.duhaha.redis.distributed_lock.DistributionLock;
import com.duhaha.redis.distributed_lock.RedisDistributionLock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author yym
 * @Date 2019/1/30 16:36
 * @Description
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RedisStudyApplication.class)
public class DistributedLockTest {

    @Autowired
    private DistributionLock distributionLock;

}
