package com.jeff_code.jmall.service;

import com.jeff_code.jmall.bean.SkuLsInfo;
import com.jeff_code.jmall.bean.SkuLsParams;
import com.jeff_code.jmall.bean.SkuLsResult;

/**
 * @Author: jefflike
 * @create: 2018/9/21
 * @describe:
 */
public interface IListService {
    // 真正的es的数据模型
    void saveSkuInfo(SkuLsInfo skuLsInfo);

    // 通过检索获取返回的es
    SkuLsResult search(SkuLsParams skuLsParams);
}
