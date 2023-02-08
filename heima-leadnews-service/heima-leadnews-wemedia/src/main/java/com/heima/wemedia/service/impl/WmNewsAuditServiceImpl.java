package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.apis.article.IArticleClient;
import com.heima.audit.aliyun.SampleUtils;
import com.heima.audit.tess4j.Tess4jClient;
import com.heima.common.exception.CustomException;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.wemedia.service.*;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 异步调用接口 实现类
 */
@Service
public class WmNewsAuditServiceImpl implements WmNewsAuditService {

    @Autowired
    private SampleUtils sample;

    @Autowired
    private WmNewsService wmNewsService;
    /**
     * 阿里云审核文章
     * @param wmNews
     */
    @Override
    @Async("taskExecutor")
    public void auditWmNews(WmNews wmNews) {
        String text = extractText(wmNews);
        // textAudit(wmNews, text); 使用阿里云内容安全接口审核文本
        boolean b = dfaAudit(wmNews, text);
        // TODO 图片审核暂时不写(已有文章自动审核)
        if(b){
            // 审核通过 续写
            if (wmNews.getPublishTime().getTime()>System.currentTimeMillis()){
                // 说明用户设定时间大于审核时间 [审核通过待发布]
                wmNews.setStatus(WmNews.Status.SUBMIT.getCode());
                wmNews.setReason("自动审核成功等待发布");
                wmNewsService.updateById(wmNews);
            }else {
                // 说明发布时间已到，立即发布，调用App服务
                wmNews.setStatus(WmNews.Status.PUBLISHED.getCode());//设置当前文章为已发布状态
                wmNews.setReason("文章审核通过已发布");
                saveOrUpdateApArticle(wmNews);
            }
        }
    }



    /**
     * 使用阿里云内容安全接口审核文本
     * @param wmNews
     * @param text
     * @return
     */
    private boolean textAudit(WmNews wmNews, String text) {
        boolean flag = true; //默认审核通过
        try {
            Map<String,String> map = sample.checkText(text);
            String suggestion = map.get("suggestion");
            if(suggestion.equals("block")){
                flag = false;
                wmNews.setStatus(WmNews.Status.FAIL.getCode()); //设置文章状态为审核失败
                wmNews.setReason("阿里云文本审核失败");
                wmNewsService.updateById(wmNews);
            } else if(suggestion.equals("review")){
                flag = false;
                wmNews.setStatus(WmNews.Status.ADMIN_AUTH.getCode()); //设置文章状态为待人工审核
                wmNews.setReason("阿里云文本审核不确定，进入人工审核");
                wmNewsService.updateById(wmNews);
            }else {
                wmNews.setStatus(WmNews.Status.PUBLISHED.getCode());//设置当前文章为已发布状态
                wmNews.setReason("文章审核通过已发布");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 抽取文章中全部的文本
     * @param wmNews
     * @return
     */
    private String extractText(WmNews wmNews) {
        //文本来源： 标题 + 标签 + 内容文本
        StringBuffer text = new StringBuffer();
        text.append(wmNews.getTitle());//标题
        text.append(wmNews.getLabels());//标签
        if(StringUtils.isNotBlank(wmNews.getContent())){
            List<Map> mapList = JSON.parseArray(wmNews.getContent(), Map.class);
            if(mapList!=null && mapList.size()>0){
                for (Map<String,String> map : mapList) {
                    String type = map.get("type");
                    if(type.equals("text")){
                        String contentText = map.get("value");
                        text.append(contentText);//内容文本
                    }
                }
            }
        }
        return text.toString();
    }

    @Autowired
    private IArticleClient iArticleClient;
    @Autowired
    private WmUserService wmUserService;
    @Autowired
    private WmChannelService wmChannelService;
    /**
     * 创建或更新APP文章
     * @param wmNews
     * @return
     */
    private ResponseResult saveOrUpdateApArticle(WmNews wmNews){
        ArticleDto articleDto = new ArticleDto();
        articleDto.setId(wmNews.getArticleId());
        articleDto.setTitle(wmNews.getTitle());
        articleDto.setContent(wmNews.getContent());
        articleDto.setLabels(wmNews.getLabels());
        articleDto.setImages(wmNews.getImages());
        articleDto.setPublishTime(wmNews.getPublishTime());
        if (wmNews.getCreatedTime() == null){
            articleDto.setCreatedTime(new Date());
        }
        articleDto.setChannelId(wmNews.getChannelId());
        // 查询当前文章的频道名称
        WmChannel channel = wmChannelService.getById(wmNews.getChannelId());
        articleDto.setChannelName(channel.getName());
        // 查询当前文章发布用户
        WmUser wmUser = wmUserService.getById(wmNews.getUserId());
        articleDto.setAuthorId(wmUser.getApAuthorId().longValue());
        articleDto.setAuthorName(wmUser.getName());
        ResponseResult responseResult = iArticleClient.saveOrUpdateArticle(articleDto);
        if (responseResult.getCode() != 200){
            throw new CustomException(AppHttpCodeEnum.APP_ARTICLE_CREATE_FAIL);
        }
        //3.得到响应里的articleId，更新到wmNews表中
        Long articleId = Long.valueOf(responseResult.getData()+"");
        wmNews.setArticleId(articleId);//APP文章ID
        wmNewsService.updateById(wmNews);
        return  null;
    }

    @Autowired
    private WmSensitiveService sensitiveService;

    /**
     * DFA本地敏感词库审核
     * @param wmNews
     * @param text
     * @return
     */
    private boolean dfaAudit(WmNews wmNews, String text) {
        boolean flag = true; //默认审核通过

        //1.查询全部敏感词数据
        List<WmSensitive> sensitiveList = sensitiveService.list(Wrappers.<WmSensitive>lambdaQuery().select(WmSensitive::getSensitives));
        List<String> sensitiveStrList = sensitiveList.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());

        //2.将敏感词初始化到DFA词库中
        SensitiveWordUtil.initMap(sensitiveStrList);

        //3.将文本拿到DFA词库中进行匹配得到MAP
        Map<String, Integer> map = SensitiveWordUtil.matchWords(text);

        //4.根据MAP结果处理
        if(map.size()>0){
            wmNews.setStatus(WmNews.Status.FAIL.getCode()); //设置文章状态为审核失败
            wmNews.setReason("DFA文本审核失败");
            wmNewsService.updateById(wmNews);

            flag = false;
        }
        return flag;
    }

    @Autowired
    private Tess4jClient tess4jClient;

    @Autowired
    private FileStorageService fileStorageService;
    /**
     * OCR识别全部图片中的文本
     * @param allImageSet
     * @return
     */
    private String ocrText(Set<String> allImageSet) {
        StringBuffer text = new StringBuffer();
        for (String url : allImageSet) {
            byte[] bytes = fileStorageService.downLoadFile(url);

            //从byte[]转换为butteredImage
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            try {
                BufferedImage imageFile = ImageIO.read(in);
                //识别图片的文字
                String result = tess4jClient.doOCR(imageFile);
                text.append(result);
            } catch (IOException | TesseractException e) {
                e.printStackTrace();
            }
        }
        return text.toString();
    }


    @Override
    @Async("taskExecutor")
    public void deleteArticle(Long id) {
        iArticleClient.deleteArticle(id);
    }
}
