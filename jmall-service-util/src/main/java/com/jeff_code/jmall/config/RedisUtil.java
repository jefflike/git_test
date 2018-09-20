package com.jeff_code.jmall.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @Author: jefflike
 * @create: 2018/9/19
 * @describe:
 */
public class RedisUtil {

    private JedisPool jedisPool;

    // 此时我们的形参还都没有值，所以我们要在配置文件中传入值
    public void initJedisPool(String host,int port,int database){
        // 创建JedisPoolConfig
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        // 总数
        jedisPoolConfig.setMaxTotal(200);
        // 获取连接时等待的最大毫秒
        jedisPoolConfig.setMaxWaitMillis(10*1000);
        // 最少剩余数
        jedisPoolConfig.setMinIdle(10);
        // 如果到最大数，设置等待
        jedisPoolConfig.setBlockWhenExhausted(true);
        // 在获取连接时，检查是否有效
        jedisPoolConfig.setTestOnBorrow(true);
        //
        jedisPool = new JedisPool(jedisPoolConfig,host,port,20*1000);
    }
    // 创建一个方法，获取Jedis对象
    public Jedis getJedis(){
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }

}
