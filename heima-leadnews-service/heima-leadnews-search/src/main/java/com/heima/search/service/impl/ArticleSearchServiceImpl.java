package com.heima.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.search.service.ApUserSearchService;
import com.heima.search.service.ArticleSearchService;
import com.heima.utils.common.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ArticleSearchServiceImpl implements ArticleSearchService {


    @Autowired
    private RestHighLevelClient client;


    @Autowired
    private ApUserSearchService apUserSearchService;
    @Override
    public ResponseResult search(UserSearchDto dto) throws IOException {

        Integer userId = ThreadLocalUtil.getUserId();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //搜索条件一：根据关键字进行多字段搜索（从标题和内容中搜索）
        boolQueryBuilder.must(QueryBuilders.queryStringQuery(dto.getSearchWords())
                .field("title").field("content").defaultOperator(Operator.OR));

        //搜索条件二：根据发布时间进行范围搜索（小于最小时间）
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("publishTime").lt(dto.getMinBehotTime().getTime()));

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder); //设置搜索条件为组合搜索

        if(dto.getPageSize()<=0){
            dto.setPageSize(10);
        }
        searchSourceBuilder.size(dto.getPageSize()); //结果设置1：限制查询条数

        searchSourceBuilder.sort("publishTime", SortOrder.DESC); //结果设置2：发布时间倒排序

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<font style='color:red; font-size:inherit;' >");
        highlightBuilder.postTags("</font>");
        searchSourceBuilder.highlighter(highlightBuilder);//结果设置3：高亮设置

        SearchRequest searchRequest = new SearchRequest("app_info_article");
        searchRequest.source(searchSourceBuilder);


        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        SearchHit[] searchHits = searchResponse.getHits().getHits();


        List<Map> mapList = new ArrayList<>();
        if(searchHits!=null && searchHits.length>0){
            for (SearchHit searchHit : searchHits) {
                String articleJson = searchHit.getSourceAsString();
                Map articleObject =  JSON.parseObject(articleJson,Map.class);

                Map<String, HighlightField> highlightFieldMap = searchHit.getHighlightFields();
                //取出带高亮标签的标题
                if(highlightFieldMap!=null && highlightFieldMap.size()>0){
                    HighlightField highlightField = highlightFieldMap.get("title");
                    String hTitle = highlightField.fragments()[0].toString();//带高亮标签的标题
                    articleObject.put("h_title",hTitle);
                } else {
                    articleObject.put("h_title",articleObject.get("title"));
                }

                mapList.add(articleObject);
            }
        }
        apUserSearchService.insert(dto.getSearchWords(),userId);
        return ResponseResult.okResult(mapList);
    }
}
