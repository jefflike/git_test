package com.jeff_code.jmall.manage.mapper;

import com.jeff_code.jmall.bean.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

// 要自己写sql需要到配置文件中配置，指定我们的xml文件
public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {
    // 根据spuId 查询销售属性列表
    List<SpuSaleAttr> selectSpuSaleAttrList(long spuId);

    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(long skuId, long spuId);
}
