package com.jeff_code.jmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.jeff_code.jmall.bean.*;
import com.jeff_code.jmall.config.RedisUtil;
import com.jeff_code.jmall.manage.constant.ManageConst;
import com.jeff_code.jmall.manage.mapper.*;
import com.jeff_code.jmall.service.IManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

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
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private RedisUtil redisUtil;

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
        // 根据三级分类ID 查询baseAttrInfo ，还需要查询baseAttrValue;
//        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
//        baseAttrInfo.setCatalog3Id(catalog3Id);
//        return baseAttrInfoMapper.select(baseAttrInfo);
//        设计到两张以上的表查询，则使用 自定义的xml。
//          getBaseAttrInfoListByCatalog3Id(catalog3Id) 自定义的
        return  baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(Long.parseLong(catalog3Id));
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
        // 更新之前先删除，此时因为baseAttrInfo的id有自增注解所以我们这个baseAttrInfo可以获得插入时的id
        baseAttrValue.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue);

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        // 当集合有值，循环遍历到
        if (attrValueList != null  && attrValueList.size() > 0) {
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

        // spu_sale_attr_value 也清空
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

    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        return spuImageMapper.select(spuImage);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSpuSaleAttrList(Long.parseLong(spuId));
        return spuSaleAttrList;
    }

    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {

        // sku_info
        if (skuInfo.getId()==null || skuInfo.getId().length()==0){
            // 设置id 为自增
            skuInfo.setId(null);
            skuInfoMapper.insertSelective(skuInfo);
        }else {
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }

        //        sku_img,
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuInfo.getId());
        skuImageMapper.delete(skuImage);

        // insert
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (skuImageList!=null && skuImageList.size()>0){
            for (SkuImage image : skuImageList) {
                /* "" 区别 null*/
                if (image.getId()!=null && image.getId().length()==0){
                    image.setId(null);
                }
                // skuId 必须赋值
                image.setSkuId(skuInfo.getId());
                skuImageMapper.insertSelective(image);
            }
        }
//        sku_attr_value,
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuInfo.getId());
        skuAttrValueMapper.delete(skuAttrValue);

        // 插入数据
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (skuAttrValueList!=null && skuAttrValueList.size()>0){
            for (SkuAttrValue attrValue : skuAttrValueList) {
                if (attrValue.getId()!=null && attrValue.getId().length()==0){
                    attrValue.setId(null);
                }
                // skuId
                attrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insertSelective(attrValue);
            }
        }
//        sku_sale_attr_value,
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuInfo.getId());
        skuSaleAttrValueMapper.delete(skuSaleAttrValue);
//      插入数据
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (skuSaleAttrValueList!=null && skuSaleAttrValueList.size()>0){
            for (SkuSaleAttrValue saleAttrValue : skuSaleAttrValueList) {
                if (saleAttrValue.getId()!=null && saleAttrValue.getId().length()==0){
                    saleAttrValue.setId(null);
                }
                // skuId
                saleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insertSelective(saleAttrValue);
            }
        }

    }

    public SkuInfo getSkuInfo(String skuId){
        // try-catch
        SkuInfo skuInfo =null;
        try {
            Jedis jedis = redisUtil.getJedis();
            // 定义sku:skuId:info
            String skuInfoKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
            // 从redis取得数据
            String skuJson  = jedis.get(skuInfoKey);
            if (skuJson==null || "".equals(skuJson)){
//                此时redis没有这个数据
                System.out.println("没有命中缓存！");
                // 要从数据库中取得数据,准备一个锁
                // set sku:33:info ok px 10000 nx
                String skuLockKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKULOCK_SUFFIX;
                // 执行这条命令
                String lockKey   = jedis.set(skuLockKey, "OK", "NX", "PX", ManageConst.SKULOCK_EXPIRE_PX);
                if ("OK".equals(lockKey)){
                    System.out.println("获得分布式锁");
                    // 从数据库中取得数据放入缓存
                    skuInfo = getSkuInfoDB(skuId);
                    // 将对象放入redis
                    // jedis.set(userKey,JSON.toJSONString(skuInfo));
                    // 设置key的过期时间
                    jedis.setex(skuInfoKey,ManageConst.SKUKEY_TIMEOUT,JSON.toJSONString(skuInfo));
                    jedis.close();
                    return skuInfo;
                }else {
                    // 睡一会
                    Thread.sleep(1000);
                    // 自旋
                    return getSkuInfo(skuId);
                }
            }else {
                skuInfo = JSON.parseObject(skuJson, SkuInfo.class);
                jedis.close();
                return  skuInfo;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return getSkuInfoDB(skuId);
    }

    public List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {

        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(Long.parseLong(skuInfo.getId()),Long.parseLong(skuInfo.getSpuId()));
    }

    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        List<SkuSaleAttrValue> skuSaleAttrValues = skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
        return skuSaleAttrValues;
    }

    public SkuInfo getSkuInfoDB(String skuId){
        // skuInfo 信息放入redis中
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        // 为skuInfo的skuImageList 属性赋值 只需要根据skuId 查询skuImage 表中的数据即可
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuInfo.getId());
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImageList);

        // 平台属性值集合
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(skuAttrValueList);

        return skuInfo;
    }

}
