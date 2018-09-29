package com.jeff_code.jmall.order.controller;

import java.util.ArrayList;
import java.util.List;
import com.alibaba.dubbo.config.annotation.Reference;
import com.jeff_code.jmall.bean.CartInfo;
import com.jeff_code.jmall.bean.OrderDetail;
import com.jeff_code.jmall.bean.OrderInfo;
import com.jeff_code.jmall.bean.UserAddress;
import com.jeff_code.jmall.bean.enums.OrderStatus;
import com.jeff_code.jmall.bean.enums.ProcessStatus;
import com.jeff_code.jmall.config.LoginRequire;
import com.jeff_code.jmall.service.ICartService;
import com.jeff_code.jmall.service.IOrderService;
import com.jeff_code.jmall.service.IUserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;


/**
 * @Author: jefflike
 * @create: 2018/9/11
 * @describe:
 */
@Controller
public class OrderController {

    @Reference
    private IUserInfoService iUserInfoService;

    @Reference
    private ICartService iCartService;

    @Reference
    private IOrderService iOrderService;
    //  http://localhost:8081/tarde?orderId=1
/*    @RequestMapping("/trade")
    @ResponseBody
    public List<UserAddress> findUserAddressByUserId(String userId){
        return userService.getAdressById(userId);
    }*/

    @RequestMapping("trade")
    @LoginRequire
    public String  trade(HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        // 得到选中的购物车列表
        List<CartInfo> cartCheckedList = iCartService.getCartCheckedList(userId);
        // 收货人地址
        List<UserAddress> userAddressList = iUserInfoService.getAdressById(userId);
        request.setAttribute("userAddressList",userAddressList);
        // 订单信息集合
        List<OrderDetail> orderDetailList=new ArrayList<>(cartCheckedList.size());
        for (CartInfo cartInfo : cartCheckedList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            orderDetailList.add(orderDetail);
        }
        request.setAttribute("orderDetailList",orderDetailList);
        OrderInfo orderInfo=new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());

        return "trade";
    }

    @RequestMapping(value = "submitOrder",method = RequestMethod.POST)
    @LoginRequire(autoRedirect = true)
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){
        // 将订单数据 添加到数据库中 orderInfo ,oderDetail
        // 取得userId
        String userId = (String) request.getAttribute("userId");

        // 校验 获取流水号
        String tradeNo = request.getParameter("tradeNo");
        boolean result = iOrderService.checkTradeCode(userId, tradeNo);
        if (!result){
            // 验证失败！
            request.setAttribute("errMsg","该页面已失效，请重新结算!");
            return "tradeFail";
        }

        // 校验库存 ： 每个订单都需要校验
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            // 调用验库存
            boolean flag = iOrderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
            // 校验失败
            if (!flag){
                request.setAttribute("errMsg","库存不足，请重新下单!");
                return "tradeFail";
            }
        }
        // 验价： 购物车的价格是否正确！ 购物车商品的价格，跟skuInfo.price 是否一致！ true ，过！ false 不过！
        orderInfo.setUserId(userId);


        // 订单状态，进程状态赋值
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        // 计算一下总金额
        orderInfo.sumTotalAmount();
        String orderId = iOrderService.saveOrder(orderInfo);
        // 如果校验成功则删除redis
        iOrderService.delTradeCode(userId);
        // mysql --- 伪删除！
        // 支付的时候，需要根据orderId
        return "redirect://payment.gmall.com/index?orderId="+orderId;
    }

}

