package com.heima.article.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.heima.article.service.ArticleHtmlService;
import com.heima.common.constants.ArticleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.data.Json;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 文章信息表，存储已发布的文章 服务实现类
 * </p>
 *
 * @author itheima
 */
@Slf4j
@Service
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {
    @Autowired
    private ApArticleMapper apArticleMapper;
    @Autowired
    private ApArticleContentMapper apArticleContentMapper;

    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;

    @Autowired
    private CacheService cacheService;

    /**
     * 根据参数加载文章列表
     * @param loadtype 1为加载更多  2为加载最新
     * @param dto
     * @return
     */
    @Override
    public ResponseResult load(ArticleHomeDto dto, int loadtype) {
        return ResponseResult.okResult(apArticleMapper.loadArticleList(dto, loadtype));
    }


    @Autowired
    private ArticleHtmlService articleHtmlService;

    @Override
    public ResponseResult saveOrUpdateArticle(ArticleDto dto) {
        //1.复制dto给apArticle
        ApArticle apArticle = new ApArticle();
        BeanUtils.copyProperties(dto, apArticle);

        //2.根据ID判断，如果有无值就创建APP文章相关数据
        if (dto.getId() == null) {
            //保存ap_article数据
            this.save(apArticle);

            //保存ap_article_content数据
            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setArticleId(apArticle.getId());//app主表的ID
            apArticleContent.setContent(dto.getContent());
            apArticleContentMapper.insert(apArticleContent);

            //保存ap_article_config数据
            ApArticleConfig apArticleConfig = new ApArticleConfig();
            apArticleConfig.setArticleId(apArticle.getId());//app主表ID
            apArticleConfig.setIsDelete(false);//未删除
            apArticleConfig.setIsDown(false);//未下架
            apArticleConfig.setIsComment(true);//允许评论
            apArticleConfig.setIsForward(true);//允许转发
            apArticleConfigMapper.insert(apArticleConfig);
        } else {
            //3.如果ID有值，就更新APP文章相关数据

            //查询ap_article判断是否存在
            ApArticle apArticleDB = this.getById(dto.getId());
            if(apArticleDB==null){
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "APP文章主表记录不存在");
            }

            //查询ap_article_content判断是否存在
            ApArticleContent apArticleContentDB = apArticleContentMapper.selectOne(
                    Wrappers.<ApArticleContent>lambdaQuery().eq(ApArticleContent::getArticleId, dto.getId()));
            if(apArticleContentDB==null){
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "APP文章内容记录不存在");
            }


            //更新ap_article
            this.updateById(apArticle);


            //更新ap_article_content
            apArticleContentDB.setContent(dto.getContent());
            apArticleContentMapper.updateById(apArticleContentDB);
        }
        //4.生成对应的文章内容html文件
        articleHtmlService.generatorHtml(apArticle,dto.getContent());
        //5.响应APP文章ID
        return ResponseResult.okResult(apArticle.getId());
    }


    @Override
    public ResponseResult loadV2(ArticleHomeDto dto, int loadtype, Boolean isFromIndex) {

        ArrayList<ApArticle> apArticleArrayList = new ArrayList<>();
        //1.判断是否来自首页 如果为首页查询优先查询redis里面的top数据
        if(isFromIndex){
            String redisKey = ArticleConstants.HOT_ARTICLE_FIRST_PAGE + dto.getTag();
            String articleListJson = cacheService.get(redisKey);
            List<ApArticle> apArticles = JSON.parseArray(articleListJson, ApArticle.class);
            apArticleArrayList.addAll(apArticles);
            if (apArticles.size()<30){
                dto.setSize(30-apArticles.size());
                List<ApArticle> apArticles1 = apArticleMapper.loadArticleList(dto, loadtype);
                apArticleArrayList.addAll(apArticles1);
            }
        }else {
            return load(dto,loadtype);
        }
        return ResponseResult.okResult(apArticleArrayList);
    }
}
