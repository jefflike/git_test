package com.jeff_code.jmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.jeff_code.jmall.bean.OrderDetail;
import com.jeff_code.jmall.bean.OrderInfo;
import com.jeff_code.jmall.config.RedisUtil;
import com.jeff_code.jmall.order.mapper.OrderDetailMapper;
import com.jeff_code.jmall.order.mapper.OrderInfoMapper;
import com.jeff_code.jmall.service.IOrderService;
import com.jeff_code.jmall.util.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @Author: jefflike
 * @create: 2018/9/29
 * @describe:
 */
@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisUtil redisUtil;


    public String saveOrder(OrderInfo orderInfo) {
        // 设置创建时间
        orderInfo.setCreateTime(new Date());
        // 设置失效时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        orderInfo.setExpireTime(calendar.getTime());
        // 生成第三方支付编号
        String outTradeNo = "JEFF_CODE" + System.currentTimeMillis() + "" + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfoMapper.insertSelective(orderInfo);

        // 插入订单详细信息
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }
// 为了跳转到支付页面使用。支付会根据订单id进行支付。
        String orderId = orderInfo.getId();
        return orderId;
    }

    // 校验：页面提交到后台的流水号，跟redis 中的流水号进行校验
    public boolean checkTradeCode(String userId,String tradeCodeNo){
        // 取得redis 中的数据
        Jedis jedis = redisUtil.getJedis();
        // 定义key
        String tradeNoKey="user:"+userId+":tradeCode";
        String tradeCode  = jedis.get(tradeNoKey);
        if (tradeCode!=null && tradeCode.equals(tradeCodeNo)){
            return  true;
        }else {
            return false;
        }
    }

    // 远程调用仓储系统进行库存校验
    @Override
    public boolean checkStock(String skuId, Integer skuNum) {
        // 调用 库存系统接口
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);
        if ("1".equals(result)){
            return true;
        }else {
            return false;
        }
    }

    // 删除流水号
    public void delTradeCode(String userId){
        // 放入redis
        Jedis jedis = redisUtil.getJedis();
        // 定义key
        String tradeNoKey="user:"+userId+":tradeCode";

        jedis.del(tradeNoKey);

        jedis.close();
    }

}