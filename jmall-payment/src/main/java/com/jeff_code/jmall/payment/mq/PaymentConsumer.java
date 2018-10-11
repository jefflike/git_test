package com.jeff_code.jmall.payment.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jeff_code.jmall.bean.PaymentInfo;
import com.jeff_code.jmall.service.IPaymentService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * @Author: jefflike
 * @create: 2018/10/11
 * @describe:
 */
@Component
public class PaymentConsumer {
    @Reference
    private IPaymentService iPaymentService;

    // 验证是否支付成功
    @JmsListener(destination = "PAYMENT_RESULT_CHECK_QUEUE",containerFactory = "jmsQueueListener")
    public void  consumeSkuDeduct(MapMessage mapMessage) throws JMSException {
        // 从消息提供者中取得消息
        String outTradeNo = mapMessage.getString("outTradeNo");
        int delaySec = mapMessage.getInt("delaySec");
        int checkCount = mapMessage.getInt("checkCount");
        // 调用checkPayment();
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);
        System.out.println("开始检查");
        boolean flag = iPaymentService.checkPayment(paymentInfo);
        // 什么时候停止调用？ true:停止调用，什么时候继续调用false ：继续调用[次数-1];
        if (!flag && checkCount>0){
            System.out.println("再次发送 checkCount="+checkCount);
            // 继续调用
            iPaymentService.sendDelayPaymentResult(outTradeNo,delaySec,checkCount-1);
        }

    }
}
