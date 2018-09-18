package com.jeff_code.jmall.manage.mapper;

import com.jeff_code.jmall.bean.BaseAttrInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @Author: jefflike
 * @create: 2018/9/13
 * @describe:
 */
public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {
    // 根据三级分类Id 查询BaseAttrInfo。 必须借助 xml来实现
    List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(long catalog3Id);
    // mybatis 建立xml 的规则。命名规则 xxx.xml xxx应该跟接口的名称一致！
}
