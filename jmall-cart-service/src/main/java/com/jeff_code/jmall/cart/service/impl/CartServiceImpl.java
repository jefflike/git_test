package com.jeff_code.jmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.jeff_code.jmall.bean.CartInfo;
import com.jeff_code.jmall.bean.SkuInfo;
import com.jeff_code.jmall.cart.constant.CartConst;
import com.jeff_code.jmall.cart.mapper.CartInfoMapper;
import com.jeff_code.jmall.config.RedisUtil;
import com.jeff_code.jmall.service.ICartService;
import com.jeff_code.jmall.service.IManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * @Author: jefflike
 * @create: 2018/9/27
 * @describe:
 */
@Service
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Reference
    private IManageService iManageService;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {
        // 1. 查询购物车中是否存在该商品
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        CartInfo cartInfoExist = cartInfoMapper.selectOne(cartInfo);

//        2. 存在 + num
        if(cartInfoExist != null){
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum() + skuNum);
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);
        }else {
            //        3. 不存在，则添加至购物车,CartInfo 对象中的数据从 skuInfo 得来
            SkuInfo skuInfo = iManageService.getSkuInfo(skuId);

            CartInfo cartInfo1 = new CartInfo();

            cartInfo1.setSkuId(skuId);
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setSkuPrice(skuInfo.getPrice());
            cartInfo1.setSkuName(skuInfo.getSkuName());
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo1.setUserId(userId);
            cartInfo1.setSkuNum(skuNum);
            cartInfoExist = cartInfo1;

        }

//        4. 最后，更新到redis
        Jedis jedis = redisUtil.getJedis();
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+ CartConst.USER_CART_KEY_SUFFIX;
        // 购物车实体类的字符串
        jedis.hset(userCartKey,skuId, JSON.toJSONString(cartInfoExist));
        // 用户key的过期时间
        String userInfoKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USERINFOKEY_SUFFIX;
        Long ttl = jedis.ttl(userInfoKey);

        jedis.expire(userCartKey,ttl.intValue());
        jedis.close();
    }

    @Override
    public List<CartInfo> getCartList(String userId) {
        Jedis jedis = redisUtil.getJedis();
        // 购物车key user:userId:cart
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;

        // 取到所有的values，这里就是cartInfo的集合，但是需要序列化
        List<String> cartJsons = jedis.hvals(userCartKey);
        System.out.println(cartJsons);
        Set<String> hkeys = jedis.hkeys(userCartKey);
        System.out.println(hkeys);
        if(cartJsons != null && cartJsons.size() > 0){
            ArrayList<CartInfo> cartInfoList  = new ArrayList<>();
            for (String cartJson : cartJsons) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                cartInfoList.add(cartInfo);
            }

            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    // 根据字符串进行比较
                    return o1.getId().compareTo(o2.getId());
                }
            });
            jedis.close();
            return  cartInfoList;
        }else {
            // 从数据库查询数据
            List<CartInfo> cartInfoList =  loadCartCache(userId);
            return cartInfoList;
        }

    }

    /**
     * 从数据库获取
     * @param userId
     * @return
     */
    public  List<CartInfo> loadCartCache(String userId){
        // 从数据库获取到cartInfolist的信息
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);
        if (cartInfoList==null && cartInfoList.size()==0){
            return null;
        }
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        Map<String,String> map = new HashMap<>(cartInfoList.size());
        for (CartInfo cartInfo : cartInfoList) {
            String cartJson = JSON.toJSONString(cartInfo);
            // key 都是同一个，值会产生重复覆盖！
            map.put(cartInfo.getSkuId(),cartJson);
        }
        // 将java list - redis hash，一次set多个值
        jedis.hmset(userCartKey,map);
        jedis.close();
        return  cartInfoList;
    }

    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartListCK, String userId) {
        List<CartInfo> cartInfoListDB = cartInfoMapper.selectCartListWithCurPrice(userId);

        for (CartInfo cartInfoCK : cartListCK) {
            // 定义一个标识
            boolean isMatch =false;
            for (CartInfo cartInfoDB : cartInfoListDB) {
                // skuId 一样则进行合并：数量相加
                if (cartInfoCK.getSkuId().equals(cartInfoDB.getSkuId())){
                    // 数量相加,具体逻辑具体实现，这里就是相加
                    cartInfoDB.setSkuNum(cartInfoCK.getSkuNum()+cartInfoDB.getSkuNum());
                    // 放入数据库
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                    isMatch=true;
                    break;
                }
            }
//            如果数据库里没有，那么直接将这一条插入到数据库
            if (!isMatch){
                // insert --- mysql
//                插入数据库时要加上用户id-userId，cookie存放的购物车没有这个信息
                cartInfoCK.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCK);
            }
        }
        // 将更新过的，插入过的数据通通的查询
        List<CartInfo> cartInfoList = loadCartCache(userId);

        // 以上代码全部都是合并的是购物车列表【不区分选中还是未选中】
        // cookie --- > mysql 二层循环嵌套
        // 外层使用数据库
        for (CartInfo cartInfoDB : cartInfoList) {
            // 内层使用cookie
            for (CartInfo cartInfoCK : cartInfoList) {
                // skuId ,isChecked = 1
                if (cartInfoDB.getSkuId().equals(cartInfoCK.getSkuId())){
                    if ("1".equals(cartInfoCK.getIsChecked())){
                        // 看项目经理需求 [cookie 5 + mysql 4 ？ 9]
                        cartInfoDB.setIsChecked(cartInfoCK.getIsChecked());
                        // 保存一下选中的状态
                        checkCart(cartInfoDB.getSkuId(),cartInfoCK.getIsChecked(),userId);
                    }
                }
            }
        }
        return cartInfoList;
    }

    public  void  checkCart(String skuId,String isChecked,String userId){
        // 更新购物车中的isChecked标志
        Jedis jedis = redisUtil.getJedis();
        // 取得购物车中的信息
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        String cartJson = jedis.hget(userCartKey, skuId);
        // 将cartJson 转换成对象
        CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
        cartInfo.setIsChecked(isChecked);
        String cartCheckdJson = JSON.toJSONString(cartInfo);
        jedis.hset(userCartKey,skuId,cartCheckdJson);
        // 新增到已选中购物车
        String userCheckedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
        if ("1".equals(isChecked)){
//            更新到购物车
            jedis.hset(userCheckedKey,skuId,cartCheckdJson);
        }else{
//            从redis的购物车删除这一项
            jedis.hdel(userCheckedKey,skuId);
        }
        jedis.close();
    }


}
