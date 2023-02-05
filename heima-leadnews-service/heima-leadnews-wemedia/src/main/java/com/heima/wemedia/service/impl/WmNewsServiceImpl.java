package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.common.constants.WemediaConstants;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.utils.common.ThreadLocalUtil;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.service.WmMaterialService;
import com.heima.wemedia.service.WmNewsMaterialService;
import com.heima.wemedia.service.WmNewsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.data.Json;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.cms.PasswordRecipientId;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Array;
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
                return  ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
            }
            updateById(wmNews);
            // 删除文章与素材的关系，素材表 一个文章对应n个素材， 通过文章id删除所有关系
            wmNewsMaterialService.remove(Wrappers.<WmNewsMaterial>lambdaQuery().eq(WmNewsMaterial::getNewsId,wmNews.getId()));

        }
        //准备：抽取内容的图片列表
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

        //第三部分：保存内容图片与文章关系
        // 3.1通过图片url查询对应的素材主键ID
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

        //第四部分：保存封面图片与文章关系
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
            if (coverImageList!= null && coverImageList.size()>0){
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



        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
