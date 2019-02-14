package com.duhaha.redis.scan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author yym
 * @Date 2019/2/12 9:28
 * @Description 鉴于keys的O(n)时间复杂度 没有offset和limit限制需要全部扫描引发的问题，redis提出scan解决
 * <p>
 * scan的特点：
 * 1、 复杂度虽然也是 O(n)，但是它是通过游标分步进行的，不会阻塞线程;
 * 2、 提供 limit 参数，可以控制每次返回结果的最大条数，limit 只是一个 hint，返回的结果可多可少;
 * 3、 同 keys 一样，它也提供模式匹配功能;
 * 4、 服务器不需要为游标保存状态，游标的唯一状态就是 scan 返回给客户端的游标整数;
 * 5、 返回的结果可能会有重复，需要客户端去重复，这点非常重要;
 * 6、 遍历的过程中如果有数据修改，改动后的数据能不能遍历到是不确定的;
 * 7、 单次返回的结果是空的并不意味着遍历结束，而要看返回的游标值是否为零;
 */
@Component
public class RedisScan {

    @Autowired
    private Jedis jedis;

    /**
     * 使用jedis的方式
     */
    public void scan(final String pattern, int count) {
        String cursor = ScanParams.SCAN_POINTER_START;
        while (true) {
            ScanResult<String> result = jedis.scan(cursor, new ScanParams().match(pattern).count(count));
            List<String> stringList = result.getResult();
            System.err.println(stringList);

            if ("0".equals(result.getStringCursor())) {
                return;
            } else {
                cursor = result.getStringCursor();
            }
        }
    }


    @Autowired
    private JedisConnection jedisConnection;

    /**
     * jedisConnection的方式
     */
    public void jedisConnectionScan(final String pattern, int count) {
        ScanOptions scanOptions = ScanOptions.scanOptions().match(pattern).count(count).build();
        Cursor<byte[]> cursor = jedisConnection.scan(scanOptions);
        while (cursor.hasNext()) {
            System.err.println(new String(cursor.next(), Charset.forName("utf-8")));
        }
    }

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 方法同上
     *
     * @param pattern
     * @param count
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Object> scanCursor(final String pattern, int count) {
        return (List<Object>) redisTemplate.execute((RedisCallback<List<Object>>) connection -> {
            List<Object> list = new ArrayList<>();
            Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(pattern).count(count).build());
            while (cursor.hasNext()) {
                list.add(new String(cursor.next()));
            }
            return list;
        });
    }

}
