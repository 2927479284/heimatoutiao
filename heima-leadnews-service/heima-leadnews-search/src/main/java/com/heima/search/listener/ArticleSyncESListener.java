package com.heima.search.listener;


import com.alibaba.fastjson.JSON;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.search.vos.SearchArticleVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

/**
 * 同步数据到ES的MQ监听器
 */
@Slf4j
@Component
public class ArticleSyncESListener {

    @Autowired
    private RestHighLevelClient client;


    @KafkaListener(topics = ArticleConstants.ARTICLE_ES_SYNC_TOPIC)
    public void receiveMsg(ConsumerRecord<String,String> consumerRecord){
        Optional<ConsumerRecord<String, String>> optional = Optional.ofNullable(consumerRecord);
        optional.ifPresent(x->{
            SearchArticleVo searchArticleVo = JSON.parseObject(x.value(), SearchArticleVo.class);
            String articleId = String.valueOf(searchArticleVo.getId());
            IndexRequest indexRequest = new IndexRequest("app_info_article");
            indexRequest.source(x.value(), XContentType.JSON).id(articleId);
            try {
                IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
                boolean flag = "created".equals(indexResponse.getResult().getLowercase()) ?  true :  false;
                log.info("[MQ异步更新文章到ES]结果：{}", flag);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
