package com.jeff_code.jmall.usermanage.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.jeff_code.jmall.bean.UserAddress;
import com.jeff_code.jmall.bean.UserInfo;
import com.jeff_code.jmall.service.IUserInfoService;
import com.jeff_code.jmall.usermanage.mapper.UserAddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import com.jeff_code.jmall.usermanage.mapper.UserInfoMapper;

import java.util.List;

/**
 * @Author: jefflike
 * @create: 2018/9/11
 * @describe:
 */
@Service
public class UserServiceImpl implements IUserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

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
}
