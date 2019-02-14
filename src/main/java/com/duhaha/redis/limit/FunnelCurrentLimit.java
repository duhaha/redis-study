package com.duhaha.redis.limit;

import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author yym
 * @Date 2019/2/11 10:24
 * @Description 漏斗限流算法
 * 漏斗算法的核心makeSpace() 每次灌水前都会被调用以触发漏水，给漏斗腾出空间
 * 能腾出多少空间取决于过去了多久以及流水的速率。 Funnel 对象占据的空间大小不再和行为的频率成正比，它的空间占用是一个常量。
 *
 * redis实现：将funnel的重要参数保存在redis中，取出在内存中运算，最后写入redis
 * 问题：上面所说的三步操作不能保证原子性
 * 方法：redis4.0提供了限流redis-cell模块，该模块使用了漏斗算法，并提供了原子的限流指令。 该模块只有一个指令：cl.throttle
 * 注意：java中jedis集合redis-cell模块的代码暂时没找到。尝试使用Lua脚本实现
 */
public class FunnelCurrentLimit {

    static class Funnel {
        int capacity;  //漏斗容量
        float leakingRate; //漏嘴流水速度
        int leftQuota;  //漏斗剩余空间
        long leakingTs; //上一次漏水时间

        public Funnel(int capacity, float leakingRate) {
            this.capacity = capacity;
            this.leakingRate = leakingRate;
            this.leftQuota = capacity;
            this.leakingTs = System.currentTimeMillis();
        }

        void makeSpace() {
            long nowTs = System.currentTimeMillis();
            long deltaTs = nowTs - leakingTs;
            int deltaQuota = (int) (deltaTs * leakingRate); //又可以腾出多少空间

            if (deltaQuota < 0) { //间隔时间太长，整数数字溢出
                this.leftQuota = capacity;
                this.leakingTs = nowTs;
                return;
            }

            if (deltaQuota < 1) { //腾出空间太小，最小单位是1，本次不做处理，等待下次处理
                return;
            }

            this.leftQuota += deltaQuota;
            this.leakingTs = nowTs;
            if (this.leftQuota > this.capacity) {
                this.leftQuota = this.capacity;
            }
        }

        boolean watering(int quota) {
            makeSpace();
            if (this.leftQuota >= quota) { //判断剩余空间是否足够
                this.leftQuota -= quota;
                return true;
            }
            return false;
        }
    }

    private Map<String, Funnel> funnels = new HashMap<>();

    public boolean isActionAllowed(String userId, String actionKey, int capacity, float leakingRate) {
        String key = String.format("%s:%s", userId, actionKey);
        Funnel funnel = funnels.get(key);
        if(funnel == null){
            funnel = new Funnel(capacity, leakingRate);
            funnels.put(key, funnel);
        }

        return funnel.watering(1);
    }
}
