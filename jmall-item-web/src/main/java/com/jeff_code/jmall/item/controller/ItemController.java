package com.jeff_code.jmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.jeff_code.jmall.bean.SkuInfo;
import com.jeff_code.jmall.bean.SkuSaleAttrValue;
import com.jeff_code.jmall.bean.SpuSaleAttr;
import com.jeff_code.jmall.config.LoginRequire;
import com.jeff_code.jmall.service.IManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: jefflike
 * @create: 2018/9/18
 * @describe:
 */
@Controller
public class ItemController {
    @Reference
    private IManageService iManageService;

    @RequestMapping("{skuId}.html")
//    @LoginRequire
    public String skuInfoPage(@PathVariable String  skuId, HttpServletRequest request){
        // 第一步我们需要获取skuinfo的信息展示在页面上
        SkuInfo skuInfo = iManageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo", skuInfo);
        // 查出图片放到skuInfo中，具体在impl实现

        // 查出销售属性添加到skuInfo中
        // 销售属性，属性值显示
        List<SpuSaleAttr> spuSaleAttrList = iManageService.selectSpuSaleAttrListCheckBySku(skuInfo);
        // 将其存入作用域中
        request.setAttribute("saleAttrList",spuSaleAttrList);

        // 调用manageService查询所有的销售属性值
        List<SkuSaleAttrValue> skuSaleAttrValueListBySpu = iManageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());

        // 1. 创建空字符串，空map
        String jsonKey = "";
        Map<String, String> jsonMap = new HashMap<>();

        // 2. 遍历销售属性值 map("135|138", "35")
        for (int i = 0; i < skuSaleAttrValueListBySpu.size(); i++) {
            // 获取迭代的每一个值
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueListBySpu.get(i);

            // 2.1 只要不是空串就拼接|
            if(jsonKey.length() > 0 ){
                jsonKey += "|";
            }
            // 2.2 拼接saleAttrValueId
            jsonKey += skuSaleAttrValue.getSaleAttrValueId();
            // 3. 退出条件就是要么下一条不再是当前skuId，或者到达最后一条 加入map，并且置为空串
            if(i == skuSaleAttrValueListBySpu.size() - 1 || !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueListBySpu.get(i+1).getSkuId()) ){
                jsonMap.put(jsonKey, skuSaleAttrValue.getSkuId());
                jsonKey = "";
            }

        }


        String valuesSkuJson = JSON.toJSONString(jsonMap);
        System.out.println("valuesSkuJson" + valuesSkuJson);
        request.setAttribute("valuesSkuJson",valuesSkuJson);

        return "item";
    }
}
