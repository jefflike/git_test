package com.jeff_code.jmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.jeff_code.jmall.bean.*;
import com.jeff_code.jmall.manage.mapper.*;
import com.jeff_code.jmall.service.IManageService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @Author: jefflike
 * @create: 2018/9/13
 * @describe:
 */
@Service
public class ManageServiceImpl implements IManageService {
    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private  SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        return baseAttrInfoMapper.select(baseAttrInfo);
    }

    /**
     * 在这个方法里将insert和update都写到这里
     * 特别注意防止主键是''空字符串的情况
     *
     * @param baseAttrInfo
     */
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        // 1.判断baseAttrInfo的id是否为''或者长度为0，是的话则为insert
        if (baseAttrInfo.getId() == null || baseAttrInfo.getId().length() == 0) {
            // 此时就是insert
            baseAttrInfo.setId(null);
            baseAttrInfoMapper.insert(baseAttrInfo);
        } else {
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        }
/*        if (baseAttrInfo.getId()!=null && baseAttrInfo.getId().length()>0){
            // 做更新
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        }else {
            // 做插入 mysql 主键自增，必须当前字段为null
            if (baseAttrInfo.getId().length()==0){ // id="";
                baseAttrInfo.setId(null);
            }
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }*/
        // baseAttrValue先清空再添加
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        // 更新之前先删除
        baseAttrValue.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue);

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        // 当集合有值，循环遍历到
        if (attrValueList.size() > 0 && attrValueList != null) {
            for (BaseAttrValue attrValue : attrValueList) {
                // 防止当前的id为"";
                if (attrValue.getId() != null || attrValue.getId().length() == 0 ) {
                    attrValue.setId(null);
                }

                attrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(attrValue);
            }
        }
    }

    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {
        // 创建属性对象
        BaseAttrInfo attrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        // 创建属性值对象
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        // 根据attrId字段查询对象
        baseAttrValue.setAttrId(attrInfo.getId());
        List<BaseAttrValue> attrValueList = baseAttrValueMapper.select(baseAttrValue);
        // 给属性对象中的属性值集合赋值
        attrInfo.setAttrValueList(attrValueList);
        return attrInfo;
    }

    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo){
        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        // 判断SpuInfo,id是否为""
        if(spuInfo.getId() == null || spuInfo.getId().length()==0){
            spuInfo.setId(null);
            spuInfoMapper.insertSelective(spuInfo);
        }

        // 插入图片路径，先删除再插入
        SpuImage spuImage = new SpuImage();
        spuImage.setId(spuInfo.getId());
        spuImageMapper.delete(spuImage);

        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if(spuImageList != null && spuImageList.size()>0){
            for (SpuImage image : spuImageList) {
                if (image.getId()==null || image.getId().length()==0){
                    image.setId(null);
                }

                // 要赋值spuId = spuInfo.id,因为前端这个值没有赋值，我们在后端赋值
                image.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(image);
            }
        }

        // spu_sale_attr  销售属性
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setId(spuInfo.getId());
        spuSaleAttrMapper.delete(spuSaleAttr);

        //        spu_sale_attr_value 也清空
        SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
        spuSaleAttrValue.setSpuId(spuInfo.getId());
        spuSaleAttrValueMapper.delete(spuSaleAttrValue);

        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if(spuSaleAttrList != null && spuSaleAttrList.size() > 0){
            for (SpuSaleAttr saleAttr : spuSaleAttrList) {
                if (saleAttr.getId()==null || saleAttr.getId().length()==0){
                    saleAttr.setId(null);
                }
                // 赋值spuId
                saleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(saleAttr);

                // spu_sale_attr_value 插入数据
                List<SpuSaleAttrValue> spuSaleAttrValueList = saleAttr.getSpuSaleAttrValueList();
                if(spuSaleAttrValueList != null && spuSaleAttrValueList.size()> 0){
                    for (SpuSaleAttrValue saleAttrValue : spuSaleAttrValueList) {
                        if (saleAttrValue.getId() == null || saleAttrValue.getId().length() == 0) {
                            saleAttrValue.setId(null);
                        }
                        saleAttrValue.setSpuId(spuInfo.getId());
                        // 插入数据
                        spuSaleAttrValueMapper.insertSelective(saleAttrValue);
                    }
                }
            }
        }
    }

}
