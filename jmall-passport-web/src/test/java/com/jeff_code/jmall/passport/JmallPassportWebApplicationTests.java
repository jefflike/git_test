package com.jeff_code.jmall.passport;

import com.jeff_code.jmall.passport.config.JwtUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JmallPassportWebApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Test
    public void test01(){
        String key = "jeff_code";
        String ip="192.168.67.201";
        Map map = new HashMap();
        map.put("userId","1001");
        map.put("nickName","marry");
        String token = JwtUtil.encode(key, map, ip);
        Map<String, Object> decode = JwtUtil.decode(token, key, "192.168.67.102");

        System.out.println(token);
        System.out.println(decode);
    }

}
