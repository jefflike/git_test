package com.jeff_code.jmall.usermanage.controller;

import com.jeff_code.jmall.bean.UserInfo;
import com.jeff_code.jmall.service.IUserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @Author: jefflike
 * @create: 2018/9/11
 * @describe:
 */
@Controller
public class ManageController {
    @Autowired
    private IUserInfoService iUserInfoService;

    @RequestMapping("/getAll")
    @ResponseBody
    public List<UserInfo> findAll() {
        return iUserInfoService.getAll();
    }
}
