package com.heima.article.service;


import com.baomidou.mybatisplus.extension.service.IService;

import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;

/**
 * <p>
 * 文章信息表，存储已发布的文章 服务类
 * </p>
 *
 * @author itheima
 */
public interface ApArticleService extends IService<ApArticle> {
    /**
     * 根据参数加载文章列表
     * @param loadtype 1为加载更多  2为加载最新
     * @param dto
     * @return
     */
    ResponseResult load(ArticleHomeDto dto, int loadtype);


    /**
     * 保存app端相关文章
     * @param dto
     * @return
     */
    ResponseResult saveOrUpdateArticle(ArticleDto dto);
}
