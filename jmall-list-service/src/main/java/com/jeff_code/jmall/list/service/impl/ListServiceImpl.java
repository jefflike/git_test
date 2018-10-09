package com.jeff_code.jmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.jeff_code.jmall.bean.SkuLsInfo;
import com.jeff_code.jmall.bean.SkuLsParams;
import com.jeff_code.jmall.bean.SkuLsResult;
import com.jeff_code.jmall.config.RedisUtil;
import com.jeff_code.jmall.service.IListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: jefflike
 * @create: 2018/9/21
 * @describe:
 */
@Service
public class ListServiceImpl implements IListService {


    @Autowired
    private JestClient jestClient;

    @Autowired
    private RedisUtil redisUtil;

    // 保存的index ，type
    public static final String ES_INDEX="jmall";

    public static final String ES_TYPE="SkuInfo";

    @Override
    public void saveSkuInfo(SkuLsInfo skuLsInfo) {
        // 做保存数据
        // es ：查询：是Search ，添加：
        // put jmall/SkuInfo/1，将skuLsInfo信息上架
        Index index = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();

        try {
            DocumentResult execute = jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public SkuLsResult search(SkuLsParams skuLsParams) {
        SkuLsResult skuLsResult = null;
        // 1.编写dsl 语句
        String query = makeQueryStringForSearch(skuLsParams);
        // 2.执行，得到查询的结果集
        Search search = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();
        SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 3.将查询到的结果集转换成自己封装好的返回对象
        skuLsResult = makeResultForSearch(skuLsParams, searchResult);
        return skuLsResult;
    }

    private SkuLsResult makeResultForSearch(SkuLsParams skuLsParams, SearchResult searchResult) {
        SkuLsResult skuLsResult = new SkuLsResult();
        // 将SearchResult 转换成SkuLsResult 本质：将SkuLsResult下的所有属性赋值即可！
        // 该集合中的SkuLsInfo 数据来源于？SearchResult
        ArrayList<SkuLsInfo> skuLsInfoArrayList = new ArrayList<>();
        // 获取SearchResult中的数据
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        // 循环该集合
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            //  skulsInfo 取出的数据 "skuName": "小米6X",
            SkuLsInfo skuLsInfo = hit.source;
            // 设置高亮
            if (hit.highlight!=null && hit.highlight.size()>0){
                // 取得skuName高亮的集合
                List<String> list = hit.highlight.get("skuName");
                // 取得高亮中的数据 // <span style='color: red'> 小米 </span>
                String skuNameHl  = list.get(0);
                // 从新将skuName 赋值，此时名称为高亮
                skuLsInfo.setSkuName(skuNameHl);
            }
            skuLsInfoArrayList.add(skuLsInfo);
        }
        skuLsResult.setSkuLsInfoList(skuLsInfoArrayList);
        // 设置总条数
        skuLsResult.setTotal(searchResult.getTotal());
        // 总页数totalPages
        // long page = searchResult.getTotal()%skuLsParams.getPageSize()==0?searchResult.getTotal()/skuLsParams.getPageSize():(searchResult.getTotal()/skuLsParams.getPageSize())+1;
        long page = (searchResult.getTotal()+skuLsParams.getPageSize()-1)/skuLsParams.getPageSize();
        skuLsResult.setTotalPages(page);
        // 从聚合中取得valueId
        MetricAggregation aggregations = searchResult.getAggregations();
        // 取得分组的名称
        TermsAggregation groupby_attr = aggregations.getTermsAggregation("groupby_attr");
        // groupby_attr:buckets
        List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
        // 声明一个集合对象来存储valueId值
        ArrayList<String> arrayList = new ArrayList<>();
        for (TermsAggregation.Entry bucket : buckets) {
            // 取得平台属性值的Id
            String valueId = bucket.getKey();
            // 将平台属性值的Id 放入一个集合中
            arrayList.add(valueId);
        }
        skuLsResult.setAttrValueIdList(arrayList);
        return skuLsResult;
    }

    private String makeQueryStringForSearch(SkuLsParams skuLsParams) {
        // 将dsl 语句，变为动态。将查询条件赋予dsl中
        // 1.先构建一个查询对象 query
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 构建bool
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 构建查询条件 skuName
        if (skuLsParams.getKeyword()!=null && skuLsParams.getKeyword().length()>0){
            // 构建一个must,match { "skuName": "R730" }{ "skuName": "R730" }
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", skuLsParams.getKeyword());
            // bool:must:match
            boolQueryBuilder.must(matchQueryBuilder);
            // 设置高亮 highlight
            HighlightBuilder highlighter = searchSourceBuilder.highlighter();
            highlighter.field("skuName");
            highlighter.preTags("<span style='color: red'>");
            highlighter.postTags("</span>");
            searchSourceBuilder.highlight(highlighter);
        }
        // 设置catalog3Id
        if (skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length()>0){
            // 创建term
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", skuLsParams.getCatalog3Id());
            // bool:filter ：term
            boolQueryBuilder.filter(termQueryBuilder);
        }
        // 平台属性值
        if (skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
            for (int i = 0; i <  skuLsParams.getValueId().length; i++) {
                String valueId = skuLsParams.getValueId()[i];
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                // bool:filter ：term
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }
        // 设置分页
        // 从第几条数据开始？
        // （pageNo-1）*pageSize
        int from = (skuLsParams.getPageNo()-1)*skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(skuLsParams.getPageSize());
        // 设置排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);
        // 聚合aggs terms.field {平台属性值}
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_attr);

        // 主要执行是query
        searchSourceBuilder.query(boolQueryBuilder);
        String query = searchSourceBuilder.toString();
        System.out.println("query:="+query);
        return query;
    }


    @Override
    public void incrHotScore(String skuId) {
        // 取得redis
        Jedis jedis = redisUtil.getJedis();
        // 定义记录商品访问次数的key 每次访问的时候，需要将访问次数+1
        Double score = jedis.zincrby("hotScore", 1, "skuId:" + skuId);
        // 记录多少次来更新一次es
        if (score%2==0){
            // 更新es
            updateHotScore(skuId,  Math.round(score));
        }
    }

    private void updateHotScore(String skuId, long hotScore) {
        // dsl 语句 更新语句
        String query="{\n" +
                "   \"doc\":{\n" +
                "     \"hotScore\":"+hotScore+"\n" +
                "   }\n" +
                "}";
        // 准备更新
        Update update = new Update.Builder(query).index(ES_INDEX).type(ES_TYPE).id(skuId).build();
        // 执行
        try {
            jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
