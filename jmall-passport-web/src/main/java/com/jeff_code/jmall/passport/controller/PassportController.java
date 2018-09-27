package com.jeff_code.jmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jeff_code.jmall.bean.UserInfo;
import com.jeff_code.jmall.passport.config.JwtUtil;
import com.jeff_code.jmall.service.IUserInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: jefflike
 * @create: 2018/9/25
 * @describe:
 */
@Controller
public class PassportController {

    @Value("${token.key}")
    private String signKey;

    @Reference
    private IUserInfoService iUserInfoService;

    @RequestMapping("index")
    public String index(HttpServletRequest request){
        // 在url中获取这个字段作为登陆后页面的跳转
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl", originUrl);
        return "index";
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(HttpServletRequest request, UserInfo userInfo){
        // 到数据库里看看有没有这个用户
        UserInfo loginUser = iUserInfoService.login(userInfo);
        // 对返回的user进行判断，为空说明没有这个用户，不为空说明已经取到并且信息放到redis了，此时我们需要做一个token放到redis
        if(loginUser != null){
            // token三个部分，key公钥，私有部分userinfo，salt加密盐值我们就用服务器的ip来加密
            // 1.key，把key存在配置文件中，注入项目中使用

            // 2. 私钥，定义一个hashmap，存上userinfo的数据
            HashMap<String, Object> map = new HashMap<>();
            map.put("userId", loginUser.getId());
            map.put("userNickName", loginUser.getNickName());

            // 3. saltheader中获取当前服务器的ip地址
            // 要获取这个字段，nginx配置反向代理时需要配置proxy_set_header X-forwarded-for $proxy_add_x_forwarded_for，否则取到ip为null;
//            String salt = request.getRemoteAddr();
            String salt = request.getHeader("X-forwarded-for");

            // 使用工具类生成token
            String token = JwtUtil.encode(signKey, map, salt);
            return token;
        }else {
            // 没有查询到，返回一个失败
            return "fail";
        }
    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        // 获取传进来的token
        String token = request.getParameter("token");
        // 解密需要ip地址
        String currentIp = request.getParameter("currentIp");
        // 解密token token，这个map就是私钥的个人信息
        Map<String, Object> map = JwtUtil.decode(token, signKey, currentIp);
        if(map != null){
            String userId = (String) map.get("userId");
            // 通过userId 验证 redis 中是否有userInfo对象
            UserInfo userInfo = iUserInfoService.verify(userId);

            if(userInfo != null){
                return "success";
            }
        }
        return "fail";
    }
}
