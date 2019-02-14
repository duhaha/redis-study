package com.duhaha.redis.service;

import java.io.Serializable;

/**
 * @Author yym
 * @Date 2019/1/30 13:08
 * @Description
 */
public class User implements Serializable {
    private String userName;
    private Integer age;

    public User(String userName, Integer age) {
        this.userName = userName;
        this.age = age;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", age=" + age +
                '}';
    }
}
