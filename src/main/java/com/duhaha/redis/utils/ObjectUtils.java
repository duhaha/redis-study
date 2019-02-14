package com.duhaha.redis.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author yym
 * @Date 2019/1/30 13:20
 * @Description
 */
public class ObjectUtils {
    public static Map<String, Object> convert2Map(Object object) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<>();
        Class<?> clazz = object.getClass();

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            map.put(field.getName(), field.get(object));
        }
        return map;
    }

    /**
     * 将字符串转换成二进制码
     * @param value
     * @return
     */
    public static String convert2Binary(String value){
        char[] chars = value.toCharArray();
        StringBuffer buffer = new StringBuffer();
        for (char c : chars) {
            buffer.append(Integer.toBinaryString((int)c));
            buffer.append(",");
        }
        return buffer.toString();
    }

    public static void main(String[] args) {
        String hello = convert2Binary("hello");
        System.err.println(hello);
    }


}
