package com.heima.apis.article;


import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 文章统一feign接口
 */
@FeignClient(value = "leadnews-article")
public interface IArticleClient {
    /**
     * 保存或更新APP文章相关表信息
     * @param articleDto
     * @return
     */
    @PostMapping("/api/v1/article/save")
    public ResponseResult saveOrUpdateArticle(@RequestBody ArticleDto articleDto);
}
