package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.common.constants.WemediaConstants;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.utils.common.ThreadLocalUtil;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.service.WmNewsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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


        //准备：抽取内容的图片列表

        //第三部分：保存内容图片与文章关系



        //第四部分：保存封面图片与文章关系


        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
