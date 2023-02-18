package com.heima.article.job;

import com.heima.article.service.HotArticleService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 热门文章定时计算的任务
 */
@Component
@Slf4j
public class HotArticleComputeJob {

    @Autowired
    private HotArticleService hotArticleService;


    @XxlJob("computeHotArticleJob")
    public ReturnT<String> computeHotArticle(String param) throws Exception {

        log.info("[定时计算热门文章任务]开始执行");
        hotArticleService.computeHotArticle();
        log.info("[定时计算热门文章任务]执行结束");
        return ReturnT.SUCCESS;
    }
}