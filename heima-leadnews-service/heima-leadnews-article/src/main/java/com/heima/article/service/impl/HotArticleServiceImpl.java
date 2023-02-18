package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.wemedia.IWemediaClient;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.HotArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.vos.HotArticleVo;
import com.heima.model.wemedia.pojos.WmChannel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class HotArticleServiceImpl implements HotArticleService {


    @Autowired
    private ApArticleMapper apArticleMapper;

    @Autowired
    private IWemediaClient wemediaClient;


    @Autowired
    private CacheService cacheService;

    /**
     * 2023年2月17日
     * 因数据问题，这里计算的是前两年的文章
     */
    @Override
    public void computeHotArticle() {
        //1.查询最近几天（10天）符合要求的文章列表
        String beginDateTime = DateTime.now().plusYears(-2).toString("yyyy-MM-dd 00:00:00");
        List<ApArticle> apArticleList = apArticleMapper.loadLastDaysArtileList(beginDateTime);

        //2.为所有文章计算分值
        List<HotArticleVo> hotArticleVoList = computeArticleScore(apArticleList);

        //3.为所有频道（包含推荐频道）缓存分值最高的30条文章
        saveRedis(hotArticleVoList);
    }
    /**
     * 为所有文章计算分值
     * @param apArticleList
     * @return
     */
    private List<HotArticleVo> computeArticleScore(List<ApArticle> apArticleList) {
        List<HotArticleVo> hotArticleVoList = new ArrayList<>();
        if(apArticleList!=null && apArticleList.size()>0){
            for (ApArticle apArticle : apArticleList) {
                HotArticleVo hotArticleVo = new HotArticleVo();
                BeanUtils.copyProperties(apArticle,hotArticleVo);
                //为每个文章计算分值
                Integer score = computeSingleArticle(apArticle);
                hotArticleVo.setScore(score);
                hotArticleVoList.add(hotArticleVo);
            }
        }
        return hotArticleVoList;
    }

    /**
     * 为每个文章计算分值
     * @param apArticle
     * @return
     */
    private Integer computeSingleArticle(ApArticle apArticle) {
        Integer score = 0;

        //累加阅读分值
        if(apArticle.getViews()!=null && apArticle.getViews()>0){
            score += apArticle.getViews();
        }
        //累加点赞分值
        if(apArticle.getLikes()!=null && apArticle.getLikes()>0){
            score += apArticle.getLikes() * ArticleConstants.HOT_ARTICLE_LIKE_WEIGHT;
        }
        //累加评论分值
        if(apArticle.getComment()!=null && apArticle.getComment()>0){
            score += apArticle.getComment() * ArticleConstants.HOT_ARTICLE_COMMENT_WEIGHT;
        }
        //累加收藏分值
        if(apArticle.getCollection()!=null && apArticle.getCollection()>0){
            score += apArticle.getCollection() * ArticleConstants.HOT_ARTICLE_COLLECTION_WEIGHT;
        }

        return score;
    }


    /**
     * 为所有频道（包含推荐频道）缓存分值最高的30条文章
     * @param hotArticleVoList
     */
    private void saveRedis(List<HotArticleVo> hotArticleVoList) {
        //1.调用远程Feign查询所有频道列表
        List<WmChannel> wmChannels = wemediaClient.listAll();
        //2.从大的文章列表中筛选出每个频道对应的文章列表并缓存
        for (WmChannel wmChannel : wmChannels) {
            List<HotArticleVo> channelApArticleHotList = hotArticleVoList.stream().filter(x -> x.getChannelId().equals(wmChannel.getId())).collect(Collectors.toList());
            String redisKey = ArticleConstants.HOT_ARTICLE_FIRST_PAGE + wmChannel.getId();
            sortAndSaveRedis(channelApArticleHotList, redisKey);
        }

        //3.为推荐频道缓存文章列表
        sortAndSaveRedis(hotArticleVoList, ArticleConstants.HOT_ARTICLE_FIRST_PAGE + ArticleConstants.DEFAULT_TAG);
    }

    /**
     * 对文章列表按照分支倒排序，并存入redis
     * @param hotArticleVoList
     * @param redisKey
     */
    private void sortAndSaveRedis(List<HotArticleVo> hotArticleVoList, String redisKey) {
        if(hotArticleVoList!=null && hotArticleVoList.size()>0){
            //按照分值倒排序
            hotArticleVoList =  hotArticleVoList.stream().sorted(Comparator.comparing(HotArticleVo::getScore).reversed()).collect(Collectors.toList());
            //判断文章数量如果大于30条则取30条
            if(hotArticleVoList.size()>30){
                hotArticleVoList = hotArticleVoList.subList(0,30);
            }
            //将文章列表数据存入redis
            cacheService.set(redisKey, JSON.toJSONString(hotArticleVoList));
        }
    }

}
