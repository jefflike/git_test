package com.jeff_code.jmall.cart.mapper;

import com.jeff_code.jmall.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @Author: jefflike
 * @create: 2018/9/27
 * @describe:
 */
public interface CartInfoMapper extends Mapper<CartInfo> {
    List<CartInfo> selectCartListWithCurPrice(String userId);
}
