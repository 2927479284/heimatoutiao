package com.heima.article.listener;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.article.service.ApArticleConfigService;
import com.heima.common.constants.WmNewsMessageConstants;
import com.heima.model.article.pojos.ApArticleConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class ArticleDownOrUpListener {


    @Autowired
    private ApArticleConfigService apArticleConfigService;


    @KafkaListener(topics = WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_TOPIC)
    public void msg(ConsumerRecord<String,String> consumerRecord){
        Optional<ConsumerRecord<String, String>> optional = Optional.ofNullable(consumerRecord);
        optional.ifPresent(x->{
            Map data = JSON.parseObject(x.value(),Map.class);
            Long articleId = Long.valueOf(data.get("articleId")+""); //app文章ID
            Integer enable = Integer.valueOf(data.get("enable")+""); //自媒体文章上下架状态  0-已下架  1-已上架
            boolean isDown = true; //APP文章上下架状态，默认已下架的   0-false-已上架   1-true-已下架
            if(enable==1){
                isDown = false;
            }
            //根据APP文章ID更新APP文章上下架状态  update ap_article_config set is_down=? where article_id=?
            apArticleConfigService.update(Wrappers
                    .<ApArticleConfig>lambdaUpdate()
                    .eq(ApArticleConfig::getArticleId, articleId)
                    .set(ApArticleConfig::getIsDown,isDown)
            );
        });
    }
}
