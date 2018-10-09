package com.jeff_code.jmall.payment.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.jeff_code.jmall.bean.PaymentInfo;
import com.jeff_code.jmall.config.ActiveMQUtil;
import com.jeff_code.jmall.payment.mapper.PaymentInfoMapper;
import com.jeff_code.jmall.service.IPaymentService;
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
            session.commit();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

}
