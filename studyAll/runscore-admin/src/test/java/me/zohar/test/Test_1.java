package me.zohar.test;

import me.zohar.runscore.common.utils.ThreadPoolUtils;
import me.zohar.runscore.constants.Constant;
import me.zohar.runscore.merchant.domain.MerchantOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Test_1 {
   // @Autowired
   // private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void set(){
        redisTemplate.opsForValue().set("myKey2","myValue122222222222222222");
        System.out.println(redisTemplate.opsForValue().get("myKey2"));

        MerchantOrder merchantOrder=new MerchantOrder();
        merchantOrder.setId("88888888888222222222");
        System.out.println(Constant.商户订单ID);
       // ThreadPoolUtils.getPaidMerchantOrderPool().schedule(() -> {
            System.out.println("设置订单号》》》》》="+merchantOrder.getId());
//        redisTemplate.opsForList().leftPush("list","a");
//        redisTemplate.opsForList().leftPush("list","b");
//        redisTemplate.opsForList().leftPush("list","c");
//        redisTemplate.opsForList().leftPush("list","d");
           //redisTemplate.opsForList().leftPush(Constant.商户订单ID, merchantOrder.getId());
        redisTemplate.opsForList().leftPush(Constant.商户订单ID, merchantOrder.getId());
       //}, 1, TimeUnit.SECONDS);

       // String orderNo = redisTemplate.opsForList().rightPop(Constant.商户订单ID, 5L, TimeUnit.SECONDS);
       // System.out.println("获取值redis的值》》》"+orderNo);

       // String orderNoLlist = redisTemplate.opsForList().rightPop("list", 5L, TimeUnit.SECONDS);
      //  System.out.println("获取值redis的值》》》orderNoLlist="+orderNoLlist);

    }
}
