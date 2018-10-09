package com.jeff_code.jmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.jeff_code.jmall.bean.*;
import com.jeff_code.jmall.service.IListService;
import com.jeff_code.jmall.service.IManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    private IListService iListService;

    @Reference
    private IManageService iManageService;

    // http://localhost:8086/list.html?keyword=小米&catalog3Id=61&valueId=81&pageNo=1&pageSize=10
    // 传入的参数都传到了skuLsParams对应的值
    @RequestMapping("list.html")
    public String getList(SkuLsParams skuLsParams, HttpServletRequest request){
        // 设置分页大小
        skuLsParams.setPageSize(3);

        // 第一个功能是动态生成dsl 语句 ，第二个功能是将dsl 语句查询出的结果封装SkuLsResult，包含高亮name，三级分类id，desc等
        SkuLsResult skuLsResult = iListService.search(skuLsParams);
        // 将其变成字符串
        String string = JSON.toJSONString(skuLsResult);
        System.out.println("S="+string);
        // 将商品集合信息保存，到前台进行显示
        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();
        request.setAttribute("skuLsInfoList",skuLsInfoList);

        // 查询结果：根据skuAttrValueList
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        // SELECT * FROM base_attr_info ai INNER JOIN base_attr_value av ON ai.id = av.attr_id WHERE av.id IN (81,14,83)
        // 调用服务根据valueId 查询数据
        List<BaseAttrInfo> baseAttrInfoList = iManageService.getAttrList(attrValueIdList);
        // 后台需要存储一个urlParam 做URL拼接
        String urlParam = makeUrlParam(skuLsParams);
        System.out.println("urlParam="+urlParam);

        // 声明一个集合用来存放面包屑对象
        ArrayList<BaseAttrValue> baseAttrValueArrayList = new ArrayList<>();
        // 平台属性值：mysql 中查询出来
        // valueId=81&valueId=14  skuLsParams.getValueId()
        // itco
        for (Iterator<BaseAttrInfo> iterator = baseAttrInfoList.iterator(); iterator.hasNext(); ) {
            BaseAttrInfo baseAttrInfo =  iterator.next();
            // 通过baseAttrInfo 获取到baseAttrValueList
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            for (BaseAttrValue baseAttrValue : attrValueList) {
                // urlParam 给附上值
                baseAttrValue.setUrlParam(urlParam);
                // 判断去重复 skuLsParams.getValueId()
                if (skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
                    // 循环 valueId=81&valueId=14
                    for (String valueId : skuLsParams.getValueId()) {
                        // 将重复的valueId 带走
                        if (valueId.equals(baseAttrValue.getId())){
                            iterator.remove();
                            // 构建一个baseAttrValue实体类
                            BaseAttrValue baseAttrValueSelected = new BaseAttrValue();
                            // 属性名：属性值名称
                            baseAttrValueSelected.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());
                            // 实际还要做一个在url上追加条件valueId，去重复操作
                            String makeUrlParam = makeUrlParam(skuLsParams, valueId);
                            // 重新赋予查询条件
                            baseAttrValueSelected.setUrlParam(makeUrlParam);
                            baseAttrValueArrayList.add(baseAttrValueSelected);
                        }
                    }
                }
            }
        }
        // 保存面包屑集合
        // 从哪里能得到属性名baseAttrInfo，属性值的名称baseAttrValue
        request.setAttribute("baseAttrValuesList",baseAttrValueArrayList);

        // 保存平台属性集合
        request.setAttribute("baseAttrInfoList",baseAttrInfoList);

        // 分页
        request.setAttribute("totalPages",skuLsResult.getTotalPages());
        request.setAttribute("pageNo",skuLsParams.getPageNo());

        // 保存一个keyword
        request.setAttribute("keyword",skuLsParams.getKeyword());
        request.setAttribute("urlParam",urlParam);

        // 问题：valueId 可能会出现重复？
        // 解决重复：

        // http://list.gmall.com/list.html?keyword=%E5%B0%8F%E7%B1%B3&valueId=81&valueId=14&valueId=81
        return "list";
    }

    // 制作最开始的连接条件
    // 方法中可变数组的好处是：后面可以省略，注意：动态数组必须放到方法参数的最后
    private String makeUrlParam(SkuLsParams skuLsParams,String ... excludeValueIds) {
        // http://localhost:8086/list.html?keyword=小米&catalog3Id=61&valueId=81&pageNo=1&pageSize=10
        String urlParam = "";
        if (skuLsParams.getKeyword()!=null && skuLsParams.getKeyword().length()>0){
            // http://localhost:8086/list.html?keyword=小米
            urlParam+="keyword="+skuLsParams.getKeyword();
        }
        // catalog3Id
        if (skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length()>0){
            if (urlParam.length()>0){
                urlParam+="&";
            }
            // http://localhost:8086/list.html?keyword=小米&catalog3Id=61
            urlParam+="catalog3Id="+skuLsParams.getCatalog3Id();
        }

        if (skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
            // 循环拼接 itar
            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                String valueId  = skuLsParams.getValueId()[i];
                // http://localhost:8086/list.html?keyword=小米&catalog3Id=61&valueId=81&valueId=13

                // 做一个判断excludeValueIds 是否有数据
                if (excludeValueIds!=null && excludeValueIds.length>0){
                    // 循环查找 因为每次点击面包屑的时候，都是点击一个，从数组得到的数据只有一个值，小标就是0；
//                    for (int j = 0; j < excludeValueIds.length; j++) {
                    // 取得添加面包屑的valueId
                    String excludeValueId = excludeValueIds[0];
//                    }
                    if (excludeValueId.equals(valueId)){
                        // 说明：url中已经有平台属性值的Id存在，则后续拼接条件查找的时候，省略
                        // conitnue break return 三者区别？
                        continue;
                    }
                }

                if (urlParam.length()>0){
                    urlParam+="&";
                }
                urlParam+="valueId="+valueId;
            }
        }
        return urlParam;
    }

}
