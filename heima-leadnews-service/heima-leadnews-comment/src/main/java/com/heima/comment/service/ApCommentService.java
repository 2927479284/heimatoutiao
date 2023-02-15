package com.heima.comment.service;

import com.heima.model.comment.dtos.CommentDto;
import com.heima.model.comment.dtos.CommentLikeDto;
import com.heima.model.comment.dtos.CommentSaveDto;
import com.heima.model.common.dtos.ResponseResult;

public interface ApCommentService {

    /**
     * 保存评论内容
     * @param dto
     * @return
     */
    public ResponseResult save(CommentSaveDto dto);


    /**
     * 点赞或取消点赞评论
     * @param dto
     * @return
     */
    ResponseResult like(CommentLikeDto dto);

    /**
     * 加载评论
     * @param dto
     * @return
     */
    ResponseResult load(CommentDto dto);
}
