package com.heima.wemedia.service;

import com.heima.model.wemedia.pojos.WmNews;

import java.util.List;

/**
 * 异步调用接口
 */
public interface WmNewsAuditService {
    /**
     * 审核文章
     * @param wmNews
     */
    public void auditWmNews(WmNews wmNews);


    /**
     * 后台文章下架 APP端文章删除 异步feign远程调用app端服务
     * @param id
     */
    void deleteArticle(Long id);

}
