package com.heima.comment.service;

import com.heima.model.comment.dtos.CommentSaveDto;
import com.heima.model.common.dtos.ResponseResult;

public interface ApCommentService {

    /**
     * 保存评论内容
     * @param dto
     * @return
     */
    public ResponseResult save(CommentSaveDto dto);
}
