package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.apis.article.IArticleClient;
import com.heima.audit.aliyun.SampleUtils;
import com.heima.common.constants.WemediaConstants;
import com.heima.common.exception.CustomException;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.*;
import com.heima.utils.common.ThreadLocalUtil;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 自媒体图文内容信息表 服务实现类
 * </p>
 *
 * @author itheima
 */
@Slf4j
@Service
@Transactional
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {

    @Override
    public ResponseResult list(WmNewsPageReqDto dto) {
        //1.设置分页请求参数默认值
        dto.checkParam();
        //2.拼接查询条件
        LambdaQueryWrapper<WmNews> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //2.1 设置固定查询条件
        lambdaQueryWrapper.eq(WmNews::getUserId, ThreadLocalUtil.getUserId() ); //根据用户ID查询
        //2.2 拼接动态查询条件
        if(dto.getChannelId()!=null){
            lambdaQueryWrapper.eq(WmNews::getChannelId, dto.getChannelId()); //根据频道ID查询
        }
        if(dto.getStatus()!=null){
            lambdaQueryWrapper.eq(WmNews::getStatus, dto.getStatus());//根据状态查询
        }
        if(StringUtils.isNotBlank(dto.getKeyword())){
            lambdaQueryWrapper.like(WmNews::getTitle,dto.getKeyword());//根据关键词模糊查询
        }
        if(dto.getBeginPubDate()!=null && dto.getEndPubDate()!=null){
            lambdaQueryWrapper.between(WmNews::getPublishTime, dto.getBeginPubDate(), dto.getEndPubDate());//根据发布时间范围查询
        }
        //2.3 设置根据发布时间倒排序
        lambdaQueryWrapper.orderByDesc(WmNews::getPublishTime);

        //3.执行分页查询
        IPage<WmNews> page = new Page<>(dto.getPage(),dto.getSize());
        this.page(page,lambdaQueryWrapper);

        //4.封装分页响应结果
        ResponseResult responseResult = new PageResponseResult(dto.getPage(),dto.getSize(),page.getTotal());
        responseResult.setData(page.getRecords());//设置每页列表数据

        return responseResult;
    }


    @Autowired
    private WmNewsMaterialService wmNewsMaterialService;

    @Autowired
    private WmMaterialService wmMaterialService;
    @Transactional
    @Override
    public ResponseResult submit(WmNewsDto dto, Integer isSumit) {
        //第一部分：判断参数
        if(StringUtils.isBlank(dto.getTitle()) || StringUtils.isBlank(dto.getContent()) )		 {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //第二部分：创建或修改文章
        //1.1 复制dto给wmNews
        WmNews wmNews = new WmNews();
        BeanUtils.copyProperties(dto, wmNews);
        //1.2 判断如果是自动布局设置布局方式临时为空
        if(dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)){
            wmNews.setType(null);
        }
        //1.3 处理封面图片，将参数中的图片列表数据转为逗号分割的字符串
        List<String> coverImageList = dto.getImages();
        if(coverImageList!=null && coverImageList.size()>0){
            String coverImageStr = StringUtils.join(coverImageList, ",");
            wmNews.setImages(coverImageStr);
        }
        //1.4 创建或更新文章
        ResponseResult orUpdateByWmNews = createOrUpdateByWmNews(isSumit,wmNews);
        if (orUpdateByWmNews !=null){
            return orUpdateByWmNews;
        }
        //准备：抽取内容的图片列表
        List<String> imageUrls = getStrings(dto);
        //第三部分：保存内容图片与文章关系
        // 3.1通过图片url查询对应的素材主键ID
        ResponseResult extracted = extracted(isSumit, wmNews, imageUrls);
        if (extracted !=null){
            return extracted;
        }
        //第四部分：保存封面图片与文章关系
        ResponseResult extracted1 = extracted(dto, isSumit, wmNews, coverImageList);
        if (extracted1 != null){
            return extracted1;
        }
        String text = extractText(wmNews);
        //第二部分：调用阿里云文本检测接口进行审核
        boolean flag = textAudit(wmNews, text);
        if(!flag){
            log.error("[自媒体文章自动审核]阿里云文本审核失败或不确定，文章id:{}", wmNews.getId());
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"]阿里云文本审核失败或不确定");
        }

        // TODO 图片审核暂时不写(已有文章自动审核)
        // 审核通过 续写
        if (wmNews.getPublishTime().getTime()>System.currentTimeMillis()){
            // 说明用户设定时间大于审核时间 [审核通过待发布]
            wmNews.setStatus(WmNews.Status.SUBMIT.getCode());
            wmNews.setReason("自动审核成功等待发布");
            updateById(wmNews);
        }else {
            // 说明发布时间已到，立即发布，调用App服务
            ResponseResult responseResult = saveOrUpdateApArticle(wmNews);
            if (responseResult != null){
                return responseResult;
            }
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Autowired
    private WmChannelService wmChannelService;
    @Autowired
    private WmUserService wmUserService;

    @Autowired
    private IArticleClient iArticleClient;
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
            log.error("[自媒体文章自动审核]创建APP文章数据失败，文章id:{}", wmNews.getId());
            throw new CustomException(AppHttpCodeEnum.APP_ARTICLE_CREATE_FAIL);
        }
        //3.得到响应里的articleId，更新到wmNews表中
        Long articleId = Long.valueOf(responseResult.getData()+"");
        wmNews.setArticleId(articleId);//APP文章ID
        this.updateById(wmNews);
        return  null;
    }

    @Autowired
    private SampleUtils sample;

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
                this.updateById(wmNews);
            } else if(suggestion.equals("review")){
                flag = false;
                wmNews.setStatus(WmNews.Status.ADMIN_AUTH.getCode()); //设置文章状态为待人工审核
                wmNews.setReason("阿里云文本审核不确定，进入人工审核");
                this.updateById(wmNews);
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

    /**
     * 保存封面图片与文章关系
     * @param dto
     * @param isSumit
     * @param wmNews
     * @param coverImageList
     * @return
     */
    private ResponseResult extracted(WmNewsDto dto, Integer isSumit, WmNews wmNews, List<String> coverImageList) {
        if (isSumit.equals(WmNews.Status.SUBMIT.getCode())){
            if (Objects.equals(dto.getType(), WemediaConstants.WM_NEWS_TYPE_AUTO)){
//                List<String> images = dto.getImages();
                if (coverImageList.size()>=3){
                    coverImageList = coverImageList.stream().limit(3).collect(Collectors.toList());
                    wmNews.setType(WemediaConstants.WM_NEWS_MANY_IMAGE);
                }else if(coverImageList.size()>=1 && coverImageList.size()<3){
                    coverImageList = coverImageList.stream().limit(1).collect(Collectors.toList());
                    wmNews.setType(WemediaConstants.WM_NEWS_SINGLE_IMAGE);
                }else {
                    coverImageList = new ArrayList<>();
                    wmNews.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
                }
                if (coverImageList.size()>0){
                    String join = StringUtils.join(coverImageList, ",");
                    wmNews.setImages(join);
                }
                updateById(wmNews);
            }
            // 选择了封面(单图/多图封面模式)
            if (coverImageList != null && coverImageList.size()>0){
                List<WmMaterial> list = wmMaterialService.list(Wrappers.<WmMaterial>lambdaQuery()
                        .in(WmMaterial::getUrl, coverImageList).select(WmMaterial::getId));
                // 3.2判断查询出来的素材列表与传入的素材列表是否数量匹配（不匹配则代表素材被删除）
                if (coverImageList.size() != list.size()){
                    return ResponseResult.errorResult(AppHttpCodeEnum.MATERIASL_REFERENCE_FAIL);
                }
                // 3.3 获取查询出来的素材list里面对应的主键ID
                List<Integer> collect = list.stream().map(WmMaterial::getId).collect(Collectors.toList());
                Integer num = 0;
                List<WmNewsMaterial> wmNewsMaterials = new ArrayList<>();
                for (Integer integer : collect) {
                    WmNewsMaterial wmNewsMaterial = new WmNewsMaterial();
                    wmNewsMaterial.setNewsId(wmNews.getId());
                    wmNewsMaterial.setMaterialId(integer);
                    wmNewsMaterial.setType(WemediaConstants.WM_COVER_REFERENCE);
                    wmNewsMaterial.setOrd(num);
                    wmNewsMaterials.add(wmNewsMaterial);
                    num++;
                }
                wmNewsMaterialService.saveBatch(wmNewsMaterials);
            }

        }
        return null;
    }

    /**
     * 保存内容图片与文章关系
     * @param isSumit
     * @param wmNews
     * @param imageUrls
     * @return
     */
    private ResponseResult extracted(Integer isSumit, WmNews wmNews, List<String> imageUrls) {
        if (isSumit.equals(WmNews.Status.SUBMIT.getCode()) && imageUrls.size() >0){
            List<WmMaterial> list = wmMaterialService.list(Wrappers.<WmMaterial>lambdaQuery()
                    .in(WmMaterial::getUrl, imageUrls).select(WmMaterial::getId));
            // 3.2判断查询出来的素材列表与传入的素材列表是否数量匹配（不匹配则代表素材被删除）
            if (imageUrls.size() != list.size()){
                return ResponseResult.errorResult(AppHttpCodeEnum.MATERIASL_REFERENCE_FAIL);
            }
            // 3.3 获取查询出来的素材list里面对应的主键ID
            List<Integer> collect = list.stream().map(WmMaterial::getId).collect(Collectors.toList());
            Integer num = 0;
            List<WmNewsMaterial> wmNewsMaterials = new ArrayList<>();
            for (Integer integer : collect) {
                WmNewsMaterial wmNewsMaterial = new WmNewsMaterial();
                wmNewsMaterial.setNewsId(wmNews.getId());
                wmNewsMaterial.setMaterialId(integer);
                wmNewsMaterial.setType(WemediaConstants.WM_CONTENT_REFERENCE);
                wmNewsMaterial.setOrd(num);
                wmNewsMaterials.add(wmNewsMaterial);
                num++;
            }
            wmNewsMaterialService.saveBatch(wmNewsMaterials);
        }
        return null;
    }

    /**
     * 返回dto中文章里面所有的图片url地址
     * @param dto
     * @return
     */
    private List<String> getStrings(WmNewsDto dto) {
        List<String> imageUrls = new ArrayList<>();
        String content = dto.getContent();
        if (StringUtils.isNotBlank(content)){
            // 文章内容为字符串 "[{},{}]"
            List<Map> maps = JSON.parseArray(content, Map.class);
            if (maps != null && maps.size()>0){
                // 开始获取文章中所有图片
                for (Map map : maps) {
                    String type = (String) map.get("type");
                    if (type.equals("image")){
                        String imageUrl = (String) map.get("value");// 获得图片地址
                        imageUrls.add(imageUrl);
                    }
                }
            }
        }
        return imageUrls;
    }

    /**
     * 更新或者创建文章
     * @param isSumit
     * @param wmNews
     * @return
     */
    private ResponseResult createOrUpdateByWmNews(Integer isSumit, WmNews wmNews) {
        wmNews.setUserId(ThreadLocalUtil.getUserId());
        wmNews.setStatus(isSumit);
        wmNews.setEnable(1);
        wmNews.setSubmitedTime(new Date());
        if (wmNews.getId() == null){
            wmNews.setCreatedTime(new Date());
            save(wmNews);
        }else {
            WmNews byId = getById(wmNews.getId());
            if (byId == null){
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
            }
            updateById(wmNews);
            // 删除文章与素材的关系，素材表 一个文章对应n个素材， 通过文章id删除所有关系
            wmNewsMaterialService.remove(Wrappers.<WmNewsMaterial>lambdaQuery().eq(WmNewsMaterial::getNewsId, wmNews.getId()));

        }
        return null;
    }
}
