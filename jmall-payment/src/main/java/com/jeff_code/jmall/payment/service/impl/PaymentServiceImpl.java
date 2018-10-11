package com.jeff_code.jmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.jeff_code.jmall.bean.PaymentInfo;
import com.jeff_code.jmall.bean.enums.PaymentStatus;
import com.jeff_code.jmall.config.ActiveMQUtil;
import com.jeff_code.jmall.payment.mapper.PaymentInfoMapper;
import com.jeff_code.jmall.service.IPaymentService;
import org.apache.activemq.ScheduledMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import org.apache.activemq.command.ActiveMQMapMessage;
import javax.jms.*;

@Service
public class PaymentServiceImpl implements IPaymentService {

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Autowired
    private AlipayClient alipayClient;

    @Override
    public void savyPaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }


    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery) {
        return  paymentInfoMapper.selectOne(paymentInfoQuery);
    }

    @Override
    public void updatePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.updateByPrimaryKeySelective(paymentInfo);
    }

    @Override
    public void updatePaymentInfoByOutTradeNo(String out_trade_no, PaymentInfo paymentInfo) {
        // 非主键更新！
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",out_trade_no);
        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);
    }

    /**
     * 查看支付状态，向支付宝查询接口发请求，查看订单的状态
     * @param paymentInfoQuery
     * @return
     */
    @Override
    public boolean checkPayment(PaymentInfo paymentInfoQuery) {
        // 具体实现过程
//        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key","RSA2");
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
//        select trade_status from xxx_table where out_trade_no = ?
//        HashMap<Object, Object> map = new HashMap<>();
//        map.put("out_trade_no",paymentInfoQuery.getOutTradeNo());
//        request.setBizContent(JSON.toJSONString(map));

        request.setBizContent("{" +
                "\"out_trade_no\":\""+paymentInfoQuery.getOutTradeNo()+"\" }");
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            if ("TRADE_SUCCESS".equals(response.getTradeStatus()) || "TRADE_FINISHED".equals(response.getTradeStatus())){
                System.out.println("调用成功");
                // 更新订单状态
                // 根据 out_trade_no 更新paymentInfo
                PaymentInfo paymentInfoUpd = new PaymentInfo();
                paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
                updatePaymentInfoByOutTradeNo(paymentInfoQuery.getOutTradeNo(),paymentInfoUpd);
                // 发送支付结果给订单
                sendPaymentResult(paymentInfoQuery,"success");
                return true;
            }
        } else {
            System.out.println("调用失败");
            return false;
        }
        return false;
    }

    @Override
    public void sendPaymentResult(PaymentInfo paymentInfo, String result) {
        // 创建工厂连接
        Connection connection = activeMQUtil.getConnection();
        try {
            connection.start();
            // 创建session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            // 创建队列
            Queue payment_result_queue = session.createQueue("PAYMENT_RESULT_QUEUE");
            // 创建提供者
            MessageProducer producer = session.createProducer(payment_result_queue);
            // 创建发送消息的对象{orderId result}
            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("orderId",paymentInfo.getOrderId());
            mapMessage.setString("result",result);

            // 准备发送消息
            producer.send(mapMessage);
            // 事务开启必须要提交
            session.commit();
            // 关闭
            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @param outTradeNo 第三方交易编号
     * @param delaySec 多长时间
     * @param checkCount 检验的次数
     */
    @Override
    public void sendDelayPaymentResult(String outTradeNo, int delaySec, int checkCount) {
        // 发送消息到队列中
        Connection connection = activeMQUtil.getConnection();
        // 打开连接
        try {
            connection.start();
            // 创建session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            // 创建队列
            Queue payment_result_check_queue = session.createQueue("PAYMENT_RESULT_CHECK_QUEUE");
            // 创建消息提供者
            MessageProducer producer = session.createProducer(payment_result_check_queue);
            // 发送的内容是什么？
            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("outTradeNo",outTradeNo);
            mapMessage.setInt("delaySec",delaySec);
            mapMessage.setInt("checkCount",checkCount);
            // 还要对消息队列进行设置时间
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,delaySec*1000);
            // 发送消息
            producer.send(mapMessage);
            // 有事务，必须提交
            session.commit();
            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closePayment(String orderId) {
        // 更新paymentInfo PaymentStatus - CLOSE
        // update paymentInfo set PaymentStatus = PaymentStatus.CLOSE where orderId = orderId
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus(PaymentStatus.ClOSED);

        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderId",orderId);

        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);

    }

}
