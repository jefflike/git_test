package com.jeff_code.jmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jeff_code.jmall.bean.CartInfo;
import com.jeff_code.jmall.bean.SkuInfo;
import com.jeff_code.jmall.config.LoginRequire;
import com.jeff_code.jmall.service.ICartService;
import com.jeff_code.jmall.service.IManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author: jefflike
 * @create: 2018/9/27
 * @describe:
 */
@Controller
public class CartController {
    @Reference
    private ICartService iCartService;

    @Reference
    private IManageService iManageService;

    @Autowired
    private CartCookieHandler cartCookieHandler;

    /**
     * LoginRequire的目的就是为了获取userId，否则确定是否需要跳转登录页面
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response){
        String skuNum = request.getParameter("skuNum");
        String skuId = request.getParameter("skuId");

        String userId = (String) request.getAttribute("userId");

        // 判断用户是否登陆
        if (userId!=null){
            // 已经登录
            iCartService.addToCart(skuId,userId,Integer.parseInt(skuNum));
        }else {
            // 未登录 数据放入cookie 中。
            cartCookieHandler.addToCart(request,response,skuId,userId,Integer.parseInt(skuNum));
        }
        // 需要保存skuInfo
        SkuInfo skuInfo = iManageService.getSkuInfo(skuId);

        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);

        return "success";
    }

    /**
     * 展示购物车
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public  String cartList(HttpServletRequest request,HttpServletResponse response){
        // 判断用户是否登录，登录了从redis中，redis中没有，从数据库中取
        // 没有登录，从cookie中取得
        String userId = (String) request.getAttribute("userId");
        if (userId!=null){
            // 从cookie中查找购物车
            List<CartInfo> cartListFromCookie = cartCookieHandler.getCartList(request);
            List<CartInfo> cartList = null;
            // cookie购物车不为空
            if (cartListFromCookie!=null && cartListFromCookie.size()>0){
                // 开始合并
                cartList=iCartService.mergeToCartList(cartListFromCookie,userId);
                // 删除cookie中的购物车
                cartCookieHandler.deleteCartCookie(request,response);
            }else{
                // 从redis中取得，或者从数据库中
                cartList= iCartService.getCartList(userId);
            }
            request.setAttribute("cartList",cartList);
        }else{
            List<CartInfo> cartList = cartCookieHandler.getCartList(request);
            request.setAttribute("cartList",cartList);
        }
        return "cartList";
    }

    /**
     * 获取选中的状态
     */
    @RequestMapping("checkCart")
    @LoginRequire(autoRedirect = false)
    @ResponseBody
    public void checkCart(HttpServletRequest request,HttpServletResponse response){
        String skuId = request.getParameter("skuId");
//        返回值是0和1
        String isChecked = request.getParameter("isChecked");
        // 获取用户Id ，来判断用户是否登录
        String userId = (String) request.getAttribute("userId");

        if (userId!=null){
            // 登录了，记录当前的商品状态
            iCartService.checkCart(skuId,isChecked,userId);
        }else{
            // 操作cookie
            cartCookieHandler.checkCart(request,response,skuId,isChecked);
        }
    }

    /**
     * 未登录状态直接生成订单的情况
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("toTrade")
    @LoginRequire(autoRedirect = true)
    public String toTrade(HttpServletRequest request,HttpServletResponse response){
        // 被选中的商品进行结算 【redis = cookie】
        String userId = (String) request.getAttribute("userId");
        // 选中的合并
        List<CartInfo> cookieHandlerCartList  = cartCookieHandler.getCartList(request);
        if (cookieHandlerCartList!=null && cookieHandlerCartList.size()>0){
            // 符合什么条件进行合并 "skuId 相等, isChecked=1"
            iCartService.mergeToCartList(cookieHandlerCartList,userId);
            // 合并被选中的商品，然后删除！
            cartCookieHandler.deleteCartCookie(request,response);
        }
        // 重定向到订单控制器
        return "redirect://order.jmall.com/trade";
    }

}
