package com.jeff_code.jmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jeff_code.jmall.bean.BaseSaleAttr;
import com.jeff_code.jmall.bean.SpuImage;
import com.jeff_code.jmall.bean.SpuInfo;
import com.jeff_code.jmall.service.IManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @Author: jefflike
 * @create: 2018/9/14
 * @describe:
 */
@Controller
public class SpuManageController {

    @Reference
    private IManageService iManageService;

    @RequestMapping("spuListPage")
    public String spuListPage(){
        return "spuListPage";
    }

    @ResponseBody
    @RequestMapping("spuList")
    public List<SpuInfo> spuList(String catalog3Id){
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        return iManageService.getSpuInfoList(spuInfo);
    }

    @ResponseBody
    @RequestMapping("baseSaleAttrList")
    public List<BaseSaleAttr> baseSaleAttrList(){
        return iManageService.getBaseSaleAttrList();
    }

    @RequestMapping(value = "saveSpuInfo",method = RequestMethod.POST)
    @ResponseBody
    public String saveSpuInfo(SpuInfo spuInfo){

        // 保存
        iManageService.saveSpuInfo(spuInfo);
        return "success";
    }

    @ResponseBody
    @RequestMapping("spuImageList")
    public List<SpuImage> spuImageList(String spuId){
        return iManageService.getSpuImageList(spuId);
    }
}
