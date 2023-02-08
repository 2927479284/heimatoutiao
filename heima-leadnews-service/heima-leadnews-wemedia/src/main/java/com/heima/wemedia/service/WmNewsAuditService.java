package com.heima.wemedia.service;

import com.heima.model.wemedia.pojos.WmNews;

import java.util.List;

/**
 * 异步调用阿里云审核接口
 */
public interface WmNewsAuditService {
    /**
     * 审核文章
     * @param wmNews
     */
    public void auditWmNews(WmNews wmNews);
}
