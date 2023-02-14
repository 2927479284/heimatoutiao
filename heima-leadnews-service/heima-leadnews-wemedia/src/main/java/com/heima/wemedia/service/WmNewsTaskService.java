package com.heima.wemedia.service;

import com.heima.model.wemedia.pojos.WmNews;

public interface WmNewsTaskService {


    /**
     * 将自媒体文章转换为TASK存入DB和CACHE中
     * @param wmNews
     */
    void addTask(WmNews wmNews);

    /**
     * 监听拉取任务并发布文章
     */
    void listenerPublishWmNews();
}
