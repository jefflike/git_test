package com.jeff_code.jmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jeff_code.jmall.bean.BaseAttrInfo;
import com.jeff_code.jmall.bean.BaseCatalog1;
import com.jeff_code.jmall.bean.BaseCatalog2;
import com.jeff_code.jmall.bean.BaseCatalog3;
import com.jeff_code.jmall.service.IManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @Author: jefflike
 * @create: 2018/9/13
 * @describe:
 */
@Controller
public class ManageController {
    @Reference
    private IManageService iManageService;

    @RequestMapping("index")
    public String index(){
        return "index";
    }

    @RequestMapping("attrListPage")
    public String attrListPage(){
        return "attrListPage";
    }

    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<BaseCatalog1> getCatalog1(){
        return iManageService.getCatalog1();
    }

    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<BaseCatalog2> getCatalog2(String catalog1Id){
        return iManageService.getCatalog2(catalog1Id);
    }

    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<BaseCatalog3> getCatalog3(String catalog2Id){
        return iManageService.getCatalog3(catalog2Id);
    }

    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<BaseAttrInfo> attrInfoList(String catalog3Id){
        return iManageService.getAttrList(catalog3Id);
    }
}
