package com.heima.article;


import com.heima.article.mapper.ApArticleMapper;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class testApArticleMapperTest {


    @Autowired
    private ApArticleMapper apArticleMapper;


    /**
     * 测试首页查询
     */
    @Test
    public void testLoad(){
        ArticleHomeDto dto = new ArticleHomeDto();
        dto.setTag(ArticleConstants.DEFAULT_TAG); //推荐频道
        dto.setMinBehotTime(new Date());//最小时间（默认值）
        dto.setSize(10);

        List<ApArticle> apArticleList = apArticleMapper.loadArticleList(dto, 1);
//        for (ApArticle apArticle : apArticleList) {
//            System.out.println(apArticle);
//        }
        apArticleList.forEach(x-> System.out.println(x));
    }


    /**
     * 测试查询更多
     */
    @Test
    public void testLoadMore(){
        ArticleHomeDto dto = new ArticleHomeDto();
        dto.setTag("6"); //具体频道
        dto.setMinBehotTime(new Date());//最小时间
        dto.setSize(10);

        List<ApArticle> apArticleList = apArticleMapper.loadArticleList(dto, 1);
//        for (ApArticle apArticle : apArticleList) {
//            System.out.println(apArticle);
//        }
        apArticleList.forEach(x-> System.out.println(x));
    }


    /**
     * 测试查询更新
     */
    @Test
    public void testLoadNew(){
        ArticleHomeDto dto = new ArticleHomeDto();
        dto.setTag("6"); //具体频道
        dto.setMaxBehotTime(new Date(DateTime.now().minusYears(1).getMillis()));//最大时间
        dto.setSize(10);

        List<ApArticle> apArticleList = apArticleMapper.loadArticleList(dto, 2);
//        for (ApArticle apArticle : apArticleList) {
//            System.out.println(apArticle);
//        }
        apArticleList.forEach(x-> System.out.println(x));
    }
}
