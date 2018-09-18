package com.jeff_code.jmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jeff_code.jmall.bean.SkuInfo;
import com.jeff_code.jmall.bean.SpuSaleAttr;
import com.jeff_code.jmall.service.IManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @Author: jefflike
 * @create: 2018/9/17
 * @describe:
 */
@Controller
public class SkuManageController {

    @Reference
    private IManageService iManageService;

    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<SpuSaleAttr> spuSaleAttrList(String spuId){
        return iManageService.getSpuSaleAttrList(spuId);
    }

    @RequestMapping(value = "saveSku",method = RequestMethod.POST)
    @ResponseBody
    public String saveSku(SkuInfo skuInfo){
        iManageService.saveSkuInfo(skuInfo);
        return "success";
    }
}
