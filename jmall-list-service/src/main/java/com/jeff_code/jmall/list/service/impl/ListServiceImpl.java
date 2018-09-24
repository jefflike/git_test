package com.jeff_code.jmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.jeff_code.jmall.bean.SkuLsInfo;
import com.jeff_code.jmall.service.IListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @Author: jefflike
 * @create: 2018/9/21
 * @describe:
 */
@Service
public class ListServiceImpl implements IListService {


    @Autowired
    private JestClient jestClient;

    // 保存的index ，type
    public static final String ES_INDEX="jmall";

    public static final String ES_TYPE="SkuInfo";

    @Override
    public void saveSkuInfo(SkuLsInfo skuLsInfo) {
        // 做保存数据
        // es ：查询：是Search ，添加：
        // put jmall/SkuInfo/1
        Index index = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();

        try {
            DocumentResult execute = jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
