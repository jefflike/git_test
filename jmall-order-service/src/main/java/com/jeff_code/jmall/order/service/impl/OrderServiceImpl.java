package com.jeff_code.jmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.jeff_code.jmall.bean.OrderDetail;
import com.jeff_code.jmall.bean.OrderInfo;
import com.jeff_code.jmall.bean.enums.ProcessStatus;
import com.jeff_code.jmall.config.ActiveMQUtil;
import com.jeff_code.jmall.config.RedisUtil;
import com.jeff_code.jmall.order.mapper.OrderDetailMapper;
import com.jeff_code.jmall.order.mapper.OrderInfoMapper;
import com.jeff_code.jmall.service.IOrderService;
import com.jeff_code.jmall.util.HttpClientUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;

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

    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Override
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

    // 生成一个流水号：
    public String getTradeNo(String userId){
        // 生成号
        String tradeCode = UUID.randomUUID().toString();
        // 放入redis
        Jedis jedis = redisUtil.getJedis();
        // 定义key
        String tradeNoKey="user:"+userId+":tradeCode";
        //
        jedis.setex(tradeNoKey,10*60,tradeCode);

        jedis.close();
        return tradeCode;
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

    @Override
    public OrderInfo getOrderInfo(String orderId) {
        //        OrderInfo orderInfo = new OrderInfo();
//        orderInfo.setId(orderId);
//        orderInfoMapper.selectOne(orderInfo);
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);
        // 根据orderId 查询订单详情
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderInfo.getId());
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);
        // 添加到orderInfo 中
        orderInfo.setOrderDetailList(orderDetailList);
        return orderInfo;
    }


    @Override
    public void updateOrderStatus(String orderId, ProcessStatus paid) {
        // 创建一个OrderInfo
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        // orderInfo.setOrderStatus(OrderStatus.PAID);
        orderInfo.setProcessStatus(paid);
        orderInfo.setOrderStatus(paid.getOrderStatus());
        // 修改状态
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }

    @Override
    public void sendOrderStatus(String orderId) {
        // 创建工厂
        Connection connection = activeMQUtil.getConnection();
        // 符合减库存的json 字符串
        String orderJson = initWareOrder(orderId);
        try {
            connection.start();
            //创建session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            // 创建队列
            Queue order_result_queue = session.createQueue("ORDER_RESULT_QUEUE");
            // 创建提供者
            MessageProducer producer = session.createProducer(order_result_queue);
            ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
            activeMQTextMessage.setText(orderJson);
            // 发送消息
            producer.send(activeMQTextMessage);
            session.commit();

            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private String initWareOrder(String orderId) {
        // 根据orderId 查询对应的订单信息
        OrderInfo orderInfo = getOrderInfo(orderId);
        Map map = initWareOrder(orderInfo);
        return JSON.toJSONString(map);
    }

    private Map initWareOrder(OrderInfo orderInfo) {
        // 创建map
        HashMap<String, Object> map = new HashMap<>();
        map.put("orderId",orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel",orderInfo.getConsigneeTel());
        map.put("orderComment",orderInfo.getOrderComment());
        map.put("orderBody","测试减库存。");
        map.put("deliveryAddress",orderInfo.getDeliveryAddress());
        map.put("paymentWay","2");
        // 仓库Id 给拆单预留的。
        // map.put("wareId",orderInfo.getWareId());
        // 少一个orderDetail。
        // details:[{skuId:101,skuNum:1,skuName:’小米手64G’},{skuId:201,skuNum:1,skuName:’索尼耳机’}]
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        List detailList = new ArrayList();
        // 遍历该集合
        for (OrderDetail orderDetail : orderDetailList) {
            // 存储一个map 集合
            Map detailMap = new HashMap();
            detailMap.put("skuId",orderDetail.getSkuId());
            detailMap.put("skuNum",orderDetail.getSkuNum());
            detailMap.put("skuName",orderDetail.getSkuName());
            detailList.add(detailMap);
        }
        map.put("details",detailList);
        return map;
    }
}