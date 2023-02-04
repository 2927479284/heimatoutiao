package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;

/**
 * <p>
 * 自媒体图文内容信息表 服务类
 * </p>
 *
 * @author itheima
 */
public interface WmNewsService extends IService<WmNews> {


    /**
     * 分页查询自媒体文章列表
     * @param dto
     * @return
     */
    ResponseResult list(WmNewsPageReqDto dto);

    /**
     * 保存草稿或提交审核
     * @param dto
     * @param isSumit 状态 提交为1  草稿为0
     * @return
     */
    ResponseResult submit(WmNewsDto dto, Integer isSumit);
}
