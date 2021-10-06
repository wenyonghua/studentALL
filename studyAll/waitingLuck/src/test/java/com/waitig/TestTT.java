package com.waitig;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
public class TestTT {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void ttt(){
       // redisTemplate.
              //  redisTemplate.opsForValue();
        redisTemplate.opsForValue().set("name","lzj");
        System.out.println("dsfsf");
    }
}
