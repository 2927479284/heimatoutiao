package com.heima.article.feign;

import com.heima.apis.article.IArticleClient;
import com.heima.article.service.ApArticleService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 文章调用端
 */
@RestController
public class ArticleClient implements IArticleClient {


    @Autowired
    private ApArticleService apArticleService;

    @Override
    @PostMapping("/api/v1/article/save")
    public ResponseResult saveOrUpdateArticle(@RequestBody ArticleDto dto) {
        return apArticleService.saveOrUpdateArticle(dto);
    }


    @Override
    @PostMapping("/api/v1/article/deleteArticle")
    public void deleteArticle(@RequestBody Long id) {
        System.out.println("aaaa");
        apArticleService.removeById(id);
    }
}
