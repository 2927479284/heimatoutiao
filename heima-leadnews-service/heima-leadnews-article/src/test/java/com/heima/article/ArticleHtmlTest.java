package com.heima.article;


import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.article.service.ApArticleContentService;
import com.heima.article.service.ApArticleService;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


@SpringBootTest
@RunWith(SpringRunner.class)
public class ArticleHtmlTest {



    @Autowired
    private ApArticleContentService apArticleContentService;

    @Autowired
    private Configuration configuration;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ApArticleService apArticleService;

    @Test
    public void testGenerateHtmlAndUpload() throws IOException, TemplateException {
        //1. 为文章详情的ftl模板准备map数据
        //1.1 根据文章ID查询文章详情对象
        Long articleId = 1501404634026209282L;
        ApArticleContent apArticleContent = apArticleContentService.getOne(Wrappers.<ApArticleContent>lambdaQuery()
                        .eq(ApArticleContent::getArticleId, articleId));


        //1.2 将content字符串转为数组对象
        JSONArray content = JSONArray.parseArray(apArticleContent.getContent());

        //1.3 将数组对象封装到MAP中
        Map map = new HashedMap();
        map.put("content",content);

        //2. 使用template生成详情页数据到输出流
        //2.1 构建空的输出流
        StringWriter stringWriter = new StringWriter();
        //2.2 使用configuration获取template对象
        Template template = configuration.getTemplate("article.ftl");
        //2.3 生成数据写入到输出流
        template.process(map,stringWriter);

        //3. 使用fileStorageService将文件输入流上传，得到URL
        //3.1 将输出流转为输入流

        ByteArrayInputStream inputStream = new ByteArrayInputStream(stringWriter.toString().getBytes());

        //3.2 上传输入流获取URL
        String url = fileStorageService.uploadHtmlFile("", articleId + ".html", inputStream);
        //4.更新URL到ap_article表
        ApArticle apArticle = new ApArticle();
        apArticle.setId(articleId);
        apArticle.setStaticUrl(url);
        apArticleService.updateById(apArticle);
    }

    @Test
    public void test1(){
        System.out.println("11111");
    }
}
