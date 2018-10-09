package com.jeff_code.jmall.service;


import com.jeff_code.jmall.bean.PaymentInfo;

public interface IPaymentService {
    void  savyPaymentInfo(PaymentInfo paymentInfo);
    // 根据outtradeNo 查询PaymentInfo
    PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery);
    // 更新paymentInfo
    void updatePaymentInfo(PaymentInfo paymentInfo);
    // 根据out_trade_no更新
    void updatePaymentInfoByOutTradeNo(String out_trade_no, PaymentInfo paymentInfo);

    // 发送支付成功的通知给订单
    void sendPaymentResult(PaymentInfo paymentInfo,String result);
}

