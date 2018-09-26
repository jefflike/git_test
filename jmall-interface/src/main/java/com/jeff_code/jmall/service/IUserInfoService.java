package com.jeff_code.jmall.service;

import com.jeff_code.jmall.bean.UserAddress;
import com.jeff_code.jmall.bean.UserInfo;

import java.util.List;

/**
 * @Author: jefflike
 * @create: 2018/9/11
 * @describe:
 */
public interface IUserInfoService {
    // 查询所有用户的接口
    List<UserInfo> getAll();

    List<UserAddress> getAdressById(String userId);

    UserInfo login(UserInfo userInfo);

    UserInfo verify(String userId);
}
