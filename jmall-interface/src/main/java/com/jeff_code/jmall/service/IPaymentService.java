package com.jeff_code.jmall.service;


import com.jeff_code.jmall.bean.PaymentInfo;

public interface IPaymentService {
    // 存入paymentInfo信息
    void  savyPaymentInfo(PaymentInfo paymentInfo);

    // 根据outtradeNo 查询PaymentInfo
    PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery);

    // 更新paymentInfo
    void updatePaymentInfo(PaymentInfo paymentInfo);

    // 根据out_trade_no更新
    void updatePaymentInfoByOutTradeNo(String out_trade_no, PaymentInfo paymentInfo);

    // 发送支付成功的通知给订单
    void sendPaymentResult(PaymentInfo paymentInfo,String result);

    // 查询支付宝的支付状态
    boolean checkPayment(PaymentInfo paymentInfoQuery);

    // 定义延迟队列接口
    void sendDelayPaymentResult(String outTradeNo,int delaySec ,int checkCount);

    // 关闭paymentInfo的状态
    void closePayment(String orderId);
}

