package com.heima.article;


import com.alibaba.fastjson.JSON;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.search.vos.SearchArticleVo;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class testApArticleMapperTest {


    @Autowired
    private ApArticleMapper apArticleMapper;


    /**
     * 测试首页查询
     */
    @Test
    public void testLoad(){
        ArticleHomeDto dto = new ArticleHomeDto();
        dto.setTag(ArticleConstants.DEFAULT_TAG); //推荐频道
        dto.setMinBehotTime(new Date());//最小时间（默认值）
        dto.setSize(10);

        List<ApArticle> apArticleList = apArticleMapper.loadArticleList(dto, 1);
//        for (ApArticle apArticle : apArticleList) {
//            System.out.println(apArticle);
//        }
        apArticleList.forEach(x-> System.out.println(x));
    }


    /**
     * 测试查询更多
     */
    @Test
    public void testLoadMore(){
        ArticleHomeDto dto = new ArticleHomeDto();
        dto.setTag("6"); //具体频道
        dto.setMinBehotTime(new Date());//最小时间
        dto.setSize(10);

        List<ApArticle> apArticleList = apArticleMapper.loadArticleList(dto, 1);
//        for (ApArticle apArticle : apArticleList) {
//            System.out.println(apArticle);
//        }
        apArticleList.forEach(x-> System.out.println(x));
    }


    /**
     * 测试查询更新
     */
    @Test
    public void testLoadNew(){
        ArticleHomeDto dto = new ArticleHomeDto();
        dto.setTag("6"); //具体频道
        dto.setMaxBehotTime(new Date(DateTime.now().minusYears(1).getMillis()));//最大时间
        dto.setSize(10);

        List<ApArticle> apArticleList = apArticleMapper.loadArticleList(dto, 2);
//        for (ApArticle apArticle : apArticleList) {
//            System.out.println(apArticle);
//        }
        apArticleList.forEach(x-> System.out.println(x));
    }



    @Autowired
    private RestHighLevelClient client;
    /**
     * 将文章历史数据批量导入ES中
     */
    @Test
    public void testImportToES(){
        //1.查询全部状态正常的文章列表
        List<SearchArticleVo> searchArticleList = apArticleMapper.loadSearchArticleList();

        //2.遍历文章列表，封装indexRequest，设置到bulkRequest中
        BulkRequest bulkRequest = new BulkRequest();
        searchArticleList.forEach(x->{

            IndexRequest indexRequest = new IndexRequest("app_info_article");
            indexRequest.source(JSON.toJSONString(x), XContentType.JSON).id(String.valueOf(x.getId()));

            bulkRequest.add(indexRequest);
        });

        //3.client执行批量插入数据到ES
        try {
            BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
            int status = bulkResponse.status().getStatus();
            System.out.println("批量插入响应状态吗：" + status);


            //4.对响应结果处理
            BulkItemResponse[] bulkResponseItems = bulkResponse.getItems();
            for (BulkItemResponse bulkResponseItem : bulkResponseItems) {
                String result = bulkResponseItem.getResponse().getResult().getLowercase();
                System.out.println("每个文档插入结果：" + result); // created
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
