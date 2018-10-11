package com.jeff_code.jmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.jeff_code.jmall.bean.OrderInfo;
import com.jeff_code.jmall.bean.PaymentInfo;
import com.jeff_code.jmall.bean.enums.PaymentStatus;
import com.jeff_code.jmall.payment.config.AlipayConfig;
import com.jeff_code.jmall.service.IOrderService;
import com.jeff_code.jmall.service.IPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    @Reference
    private IOrderService iOrderService;

    @Reference
    private IPaymentService iPaymentService;

    @Autowired
    private AlipayClient alipayClient;

    /**
     * 支付页面，订单的订单号，订单总额，选择支付方式（我们这里提交走的是alipay/submit控制器）
     * @param request
     * @return
     */
    @RequestMapping("index")
    public String index( HttpServletRequest request){
        // 获取orderId
        String orderId = request.getParameter("orderId");
        // 根据订单orderId 查询订单信息
        OrderInfo orderInfo = iOrderService.getOrderInfo(orderId);
        // 存储orderId
        request.setAttribute("orderId",orderId);
        // 存储总金额
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        return "index";
    }

    /**
     * 先将订单转换成支付，存放到支付的数据表中，再传递参数给支付宝网关生成二维码，开启延迟队列，防止回调信息未收到的不确定性
     * 延迟队列就是为了解决消息队列异步回调的不确定性的，当延迟队列的延时很长时，会一直存在队列中，导致队列冗长
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "alipay/submit",method = RequestMethod.POST)
    @ResponseBody
    public String submitPayment( HttpServletRequest request,HttpServletResponse response){
        // 上面传下来的orderId
        String orderId = request.getParameter("orderId");
        // 保存交易记录 mysql -- payment_info，支付的商品详情都是来自订单信息
        PaymentInfo paymentInfo = new PaymentInfo();
        // 调用根据订单Id查询订单信息
        OrderInfo orderInfo = iOrderService.getOrderInfo(orderId);

        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        // 测试信息：随意写的
        paymentInfo.setSubject("有钱随便买！");
        // 直接存到数据库中
        iPaymentService.savyPaymentInfo(paymentInfo);

        // 生成一个二维码
        // AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE); //获得初始化的AlipayClient
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址
        // 制作一个map
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",paymentInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("subject",paymentInfo.getSubject());
        map.put("total_amount",paymentInfo.getTotalAmount());
        // 将map 转换为字符串
        String jsonMap = JSON.toJSONString(map);
        alipayRequest.setBizContent(jsonMap);
//        alipayRequest.setBizContent("{" +
//                "    \"out_trade_no\":\"20150320010101001\"," +
//                "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
//                "    \"total_amount\":88.88," +
//                "    \"subject\":\"Iphone6 16G\"," +
//                "    \"body\":\"Iphone6 16G\"," +
//                "    \"passback_params\":\"merchantBizType%3d3C%26merchantBizNo%3d2016010101111\"," +
//                "    \"extend_params\":{" +
//                "    \"sys_service_provider_id\":\"2088511833207846\"" +
//                "    }"+
//                "  }");//填充业务参数
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=UTF-8");
        // 生成延迟队列查询订单outTradeNo的支付状态，修改支付的状态
        iPaymentService.sendDelayPaymentResult(paymentInfo.getOutTradeNo(),15,3);
        return form;
    }

    // 测试同步回调
    @RequestMapping("/alipay/callback/return")
    public String callbackReturn(){
        return "redirect:"+AlipayConfig.return_order_url;
    }

    // 异步回调
    @RequestMapping("callback/notify")
    @ResponseBody
    public String paymentNotify(@RequestParam Map<String,String> paramMap, HttpServletRequest request) throws AlipayApiException {
        // 将异步回调通知的参数封装到一个paramMap集合中
        boolean signVerified = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, "utf-8",AlipayConfig.sign_type); //调用SDK验证签名
        if(signVerified){
            //    TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            //   trade_status == TRADE_SUCCESS TRADE_FINISHED
            //   如果该订单未支付，且不能关闭才是成功！ paymentInfo 表中记录支付信息 【out_trade_no】
            String trade_status = paramMap.get("trade_status");
            if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){

                String out_trade_no = paramMap.get("out_trade_no");
                // 根据out_trade_no 查询paymentInfo 的支付状态  select * from paymentInfo where out_trade_no = ?
                PaymentInfo paymentInfoQuery  = new PaymentInfo();
                paymentInfoQuery.setOutTradeNo(out_trade_no);
                PaymentInfo paymentInfo = iPaymentService.getPaymentInfo(paymentInfoQuery);

                // 获取paymentInfo的支付状态
                if (paymentInfo.getPaymentStatus()==PaymentStatus.ClOSED || paymentInfo.getPaymentStatus()==PaymentStatus.PAID){
                    return "fail";
                }
                // 做更新 PaymentStatus.PAID
                // update paymentInfo set payment_status = PaymentStatus.PAID where id = paymentInfo.getId();
                // // update paymentInfo set payment_status = PaymentStatus.PAID where out_trade_no = out_trade_no;
                // 创建一个更新的对象
                paymentInfo.setPaymentStatus(PaymentStatus.PAID);
                paymentInfo.setCallbackTime(new Date());
                paymentInfo.setCallbackContent(paramMap.toString());

                iPaymentService.updatePaymentInfo(paymentInfo);
                // paymentService.updatePaymentInfoByOutTradeNo(out_trade_no,paymentInfo);

                // 发送通知给订单
                iPaymentService.sendPaymentResult(paymentInfo,"success");
                return "success";
            }else {
                return "fail";
            }

        }else{
            // TODO 验签失败则记录异常日志，并在response中返回failure.
            return "fail";
        }
    }

    @RequestMapping("sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(PaymentInfo paymentInfo,String result){
        // 手动发送通知
        iPaymentService.sendPaymentResult(paymentInfo,result);
        return "success";
    }
}
