package com.heima.article.service;

import com.heima.model.article.pojos.ApArticle;

/**
 * 生成文章详情页并上传
 */
public interface ArticleHtmlService {

    /**
     * 生成文章详情页并上传
     * @param apArticle
     * @param content
     */
    void generatorHtml(ApArticle apArticle, String content);
}