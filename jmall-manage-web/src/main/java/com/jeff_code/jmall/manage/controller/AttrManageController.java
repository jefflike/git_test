package com.jeff_code.jmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jeff_code.jmall.bean.BaseAttrInfo;
import com.jeff_code.jmall.bean.BaseAttrValue;
import com.jeff_code.jmall.bean.SkuLsInfo;
import com.jeff_code.jmall.service.IListService;
import com.jeff_code.jmall.service.IManageService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @Author: jefflike
 * @create: 2018/9/13
 * @describe:
 */
@Controller
public class AttrManageController {
    @Reference
    private IManageService iManageService;

    @Reference
    private IListService iListService;

    @RequestMapping(value = "saveAttr",method = RequestMethod.POST)
    @ResponseBody
    public String saveAttrInfo(BaseAttrInfo baseAttrInfo){
        iManageService.saveAttrInfo(baseAttrInfo);
        return "success";
    }

    @RequestMapping(value = "getAttrValueList")
    @ResponseBody
    public List<BaseAttrValue> getAttrValueList(String attrId){
        BaseAttrInfo attrInfo = iManageService.getAttrInfo(attrId);
        return attrInfo.getAttrValueList();
    }

    // 商品上架 根据商品id进行上架
    // http://localhost:8082/onSale?skuId=33
    @RequestMapping("onSale")
    @ResponseBody
    public String onSale(String skuId){
        // 使用skuId查找到skuattrvalue
        BaseAttrInfo attrInfo = iManageService.getAttrInfo(skuId);

        SkuLsInfo skuLsInfo = new SkuLsInfo();
        try {
            BeanUtils.copyProperties(skuLsInfo, attrInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        iListService.saveSkuInfo(skuLsInfo);
        return "success";
    }


}
