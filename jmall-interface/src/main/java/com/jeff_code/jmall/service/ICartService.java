package com.jeff_code.jmall.service;


import com.jeff_code.jmall.bean.CartInfo;

import java.util.List;

public interface ICartService {
    /**
     *
     * @param skuId 商品id
     * @param userId 用户id
     * @param skuNum 商品数据
     */
    void  addToCart(String skuId, String userId, Integer skuNum);

    /**
     *
     * @param userId 用户Id
     * @return
     */
   List<CartInfo> getCartList(String userId);

    List<CartInfo> loadCartCache(String userId);

    /**
     * 合并购物车
     * @param cartListCK cookie 中的购物车
     * @param userId 用户Id
     * @return
     */
    List<CartInfo> mergeToCartList(List<CartInfo> cartListCK, String userId);

    void checkCart(String skuId, String isChecked, String userId);

    List<CartInfo> getCartCheckedList(String userId);
}
