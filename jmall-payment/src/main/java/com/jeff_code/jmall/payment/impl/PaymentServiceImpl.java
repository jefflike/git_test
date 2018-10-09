package com.jeff_code.jmall.payment.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.jeff_code.jmall.bean.PaymentInfo;
import com.jeff_code.jmall.payment.mapper.PaymentInfoMapper;
import com.jeff_code.jmall.service.IPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class PaymentServiceImpl implements IPaymentService {

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;
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
}
