package com.jeff_code.jmall.order.controller;

import java.util.List;
import com.alibaba.dubbo.config.annotation.Reference;
import com.jeff_code.jmall.bean.UserAddress;
import com.jeff_code.jmall.service.IUserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @Author: jefflike
 * @create: 2018/9/11
 * @describe:
 */
@Controller
public class OrderController {

    @Reference
    private IUserInfoService userService;
    //  http://localhost:8081/tarde?orderId=1
    @RequestMapping("/trade")
    @ResponseBody
    public List<UserAddress> findUserAddressByUserId(String userId){
        return userService.getAdressById(userId);
    }


}

