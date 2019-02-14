package com.duhaha.redis.delay_queue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.UUID;

/**
 * @Author yym
 * @Date 2019/1/30 19:43
 * @Description  延时队列的实现
 */
public class RedisDelayingQueue<T> {
    static class TaskItem<T> {
        public String id;
        public T msg;
    }

    private Type type = new TypeReference<TaskItem<T>>() {
    }.getType();
    private Jedis jedis;
    private String queueKey;

    public RedisDelayingQueue(Jedis jedis, String queueKey) {
        this.jedis = jedis;
        this.queueKey = queueKey;
    }

    /**
     * 消息加到redis的zset中
     * @param msg
     */
    public void delay(T msg) {
        TaskItem<T> taskItem = new TaskItem<>();
        taskItem.id = UUID.randomUUID().toString();
        taskItem.msg = msg;

        jedis.zadd(queueKey, System.currentTimeMillis() + 5000, JSON.toJSONString(taskItem));
    }

    /**
     * 循环消费消息
     */
    public void loop() {
        while (!Thread.interrupted()) {
            // 只取一条
            Set values = jedis.zrangeByScore(queueKey, 0, System.currentTimeMillis(), 0, 1);
            if (values.isEmpty()) {
                try {
                    Thread.sleep(500); // 歇会继续
                } catch (InterruptedException e) {
                    break;
                }
                continue;
            }
            String s = (String) values.iterator().next();
            if (jedis.zrem(queueKey, s) > 0) { // 抢到了
                TaskItem task = JSON.parseObject(s, type); // fastjson 反序列化
                this.handleMsg(task.msg);
            }
        }
    }

    public void handleMsg(Object msg) {
        System.out.println(msg);
    }

    public static void main(String[] args) {
        Jedis jedis = new Jedis();
        RedisDelayingQueue queue = new RedisDelayingQueue<>(jedis, "q-demo");
        Thread producer = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                queue.delay("codehole" + i);
            }
        });

        Thread consumer = new Thread(queue::loop);
        producer.start();
        consumer.start();

        try {
            producer.join();
            Thread.sleep(6000);
            consumer.interrupt();
            consumer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

