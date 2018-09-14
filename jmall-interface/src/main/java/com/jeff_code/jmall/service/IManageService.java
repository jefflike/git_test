package com.jeff_code.jmall.service;

import com.jeff_code.jmall.bean.BaseAttrInfo;
import com.jeff_code.jmall.bean.BaseCatalog1;
import com.jeff_code.jmall.bean.BaseCatalog2;
import com.jeff_code.jmall.bean.BaseCatalog3;

import java.util.List;

/**
 * @Author: jefflike
 * @create: 2018/9/13
 * @describe:
 */
public interface IManageService {
    public List<BaseCatalog1> getCatalog1();

    public List<BaseCatalog2> getCatalog2(String catalog1Id);

    public List<BaseCatalog3> getCatalog3(String catalog2Id);

    public List<BaseAttrInfo> getAttrList(String catalog3Id);

    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    BaseAttrInfo getAttrInfo(String attrId);
}
