package com.jeff_code.jmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jeff_code.jmall.bean.BaseAttrInfo;
import com.jeff_code.jmall.bean.BaseAttrValue;
import com.jeff_code.jmall.service.IManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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


}
