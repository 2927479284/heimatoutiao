package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleHtmlService;
import com.heima.common.constants.ArticleConstants;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.search.vos.SearchArticleVo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 生成文章详情页并上传
 */
@Service
public class ArticleHtmlServiceImpl implements ArticleHtmlService {
    @Autowired
    private Configuration configuration;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ApArticleService apArticleService;

    /**
     * 异步生成文章详情页并上传
     * @param apArticle
     * @param content
     */
    @Async("taskExecutor")
    @Override
    public void generatorHtml(ApArticle apArticle, String content) {
        //1.准备FreeMarker模板所需数据
        JSONArray contentArr = JSONArray.parseArray(content);
        Map map = new HashMap();
        map.put("content",contentArr);

        //2.使用Template生成文章页面数据到StringWriter
        StringWriter out = new StringWriter();
        try {
            Template template = configuration.getTemplate("article.ftl");
            template.process(map,out);
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }

        //3.将文章页面数据上传到MinIO
        ByteArrayInputStream in = new ByteArrayInputStream(out.toString().getBytes(StandardCharsets.UTF_8));
        String url = fileStorageService.uploadHtmlFile("", apArticle.getId() + ".html", in);

        //4.更新ap_article表的static_url
        ApArticle apArticleDB = new ApArticle();
        apArticleDB.setId(apArticle.getId());//更新条件
        apArticleDB.setStaticUrl(url);//要更新的值
        apArticleService.updateById(apArticleDB);//sql : update ap_article set static_url=? where id=?
        //5.生成文章数据到KafKa
        createArticleESIndex(apArticle,content,url);
    }

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    /**
     * 送消息，创建索引
     * @param apArticle
     * @param content
     * @param url
     */
    private void createArticleESIndex(ApArticle apArticle, String content, String url) {
        SearchArticleVo vo = new SearchArticleVo();
        BeanUtils.copyProperties(apArticle,vo);
        vo.setContent(content);
        vo.setStaticUrl(url);
        kafkaTemplate.send(ArticleConstants.ARTICLE_ES_SYNC_TOPIC, JSON.toJSONString(vo));
    }
}
