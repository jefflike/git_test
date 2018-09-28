package com.jeff_code.jmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.jeff_code.jmall.bean.CartInfo;
import com.jeff_code.jmall.bean.SkuInfo;
import com.jeff_code.jmall.config.CookieUtil;
import com.jeff_code.jmall.service.IManageService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: jefflike
 * @create: 2018/9/27
 * @describe:
 */
@Component
public class CartCookieHandler {

    // 定义购物车名称
    private String cookieCartName = "CART";
    // 设置cookie 过期时间
    private int COOKIE_CART_MAXAGE=7*24*3600;
    @Reference
    private IManageService iManageService;

    public void addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, String userId, Integer skuNum) {
        String cartJson = CookieUtil.getCookieValue(request, cookieCartName, true);

        List<CartInfo> cartInfoList = new ArrayList<>();
        boolean ifExist=false;
        // 购物车里有数据
        if (cartJson!=null){
            // 这是一个list，单独提取出去
            cartInfoList = JSON.parseArray(cartJson, CartInfo.class);
            for (CartInfo cartInfo : cartInfoList) {
                if (cartInfo.getSkuId().equals(skuId)){
                    cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
                    // 价格设置
                    cartInfo.setSkuPrice(cartInfo.getCartPrice());
                    ifExist=true;
                    break;
                }
            }
        }
        // //购物车里没有对应的商品 或者 没有购物车
        if (!ifExist){
            //把商品信息取出来，新增到购物车
            SkuInfo skuInfo = iManageService.getSkuInfo(skuId);
            CartInfo cartInfo=new CartInfo();

            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());

            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(skuNum);
            cartInfoList.add(cartInfo);
        }

        // 将更新的购物车数据放到cookie
        String newCartJson = JSON.toJSONString(cartInfoList);
        CookieUtil.setCookie(request, response, cookieCartName, newCartJson, COOKIE_CART_MAXAGE, true);

    }

    public List<CartInfo> getCartList(HttpServletRequest request) {
        String cartJson = CookieUtil.getCookieValue(request, cookieCartName, true);
//        把cartInfoList字符串解析成List<CartInfo>
        List<CartInfo> cartInfoList = JSON.parseArray(cartJson, CartInfo.class);
        return cartInfoList;

    }

    public void deleteCartCookie(HttpServletRequest request,HttpServletResponse response){
        CookieUtil.deleteCookie(request,response,cookieCartName);
    }

    public  void  checkCart(HttpServletRequest request,HttpServletResponse response,String skuId,String isChecked){
        //  取出购物车中的商品
        List<CartInfo> cartList = getCartList(request);
        // 循环修改isChecked字段
        for (CartInfo cartInfo : cartList) {
            if (cartInfo.getSkuId().equals(skuId)){
                cartInfo.setIsChecked(isChecked);
            }
        }
        // 保存到cookie
        String newCartJson = JSON.toJSONString(cartList);
        CookieUtil.setCookie(request,response,cookieCartName,newCartJson,COOKIE_CART_MAXAGE,true);
    }

}
