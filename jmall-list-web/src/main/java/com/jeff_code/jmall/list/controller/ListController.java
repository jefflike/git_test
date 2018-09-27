package com.jeff_code.jmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.jeff_code.jmall.bean.SkuLsParams;
import com.jeff_code.jmall.bean.SkuLsResult;
import com.jeff_code.jmall.service.IListService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ListController {

    @Reference
    private IListService iListService;

    // http://localhost:8086/list.html?keyword=小米&catalog3Id=61&valueId=81&pageNo=1&pageSize=10
    // 传入的参数都传到了skuLsParams对应的值
    @RequestMapping("list.html")
    @ResponseBody
    public String getList(SkuLsParams skuLsParams){
        SkuLsResult skuLsResult = iListService.search(skuLsParams);

        // 将其变成字符串
        String string = JSON.toJSONString(skuLsResult);

        return string;
    }

}
