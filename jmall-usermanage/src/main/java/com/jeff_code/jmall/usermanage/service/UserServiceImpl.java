package com.jeff_code.jmall.usermanage.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.jeff_code.jmall.bean.UserAddress;
import com.jeff_code.jmall.bean.UserInfo;
import com.jeff_code.jmall.config.RedisUtil;
import com.jeff_code.jmall.service.IUserInfoService;
import com.jeff_code.jmall.usermanage.mapper.UserAddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import com.jeff_code.jmall.usermanage.mapper.UserInfoMapper;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

/**
 * @Author: jefflike
 * @create: 2018/9/11
 * @describe:
 */
@Service
public class UserServiceImpl implements IUserInfoService {

    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<UserInfo> getAll() {
        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserAddress> getAdressById(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setId(userId);
        return userAddressMapper.select(userAddress);
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        // 1. 数据库中存的密码是加密过的，所以我们比对数据库也需要将前端传进来的页面进行加密
        String passwd = userInfo.getPasswd();
        // 此处可以加盐确保安全
        String newPassword = DigestUtils.md5DigestAsHex(passwd.getBytes());
        userInfo.setPasswd(newPassword);
        UserInfo user = userInfoMapper.selectOne(userInfo);

        // 2. 登陆成功则将数据存到redis里
        Jedis jedis = redisUtil.getJedis();
        if(user != null){
            String userKey = userKey_prefix + user.getId() + userinfoKey_suffix;
            // 设置到redis并且设置一个过期时间
            jedis.setex(userKey, userKey_timeOut, JSON.toJSONString(user));
        }
        jedis.close();
        return user;
    }

    @Override
    public UserInfo verify(String userId) {
        // 查看是否有这个userId的redis能不能取到值，取到则传到控制器完成认证
        String userKey = userKey_prefix + userId + userinfoKey_suffix;
        Jedis jedis = redisUtil.getJedis();
        String userJson = jedis.get(userKey);

        if(userJson != null){
            // 认证一次记得重置这个key的时间
            jedis.expire(userKey, userKey_timeOut);
            UserInfo map = JSON.parseObject(userJson, UserInfo.class);
            return map;
        }
        return null;
    }
}
