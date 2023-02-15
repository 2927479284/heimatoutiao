package com.heima.comment.service;

import com.heima.model.comment.pojos.ApComment;

public interface ApCommentHotService {

    /**
     * 计算热点评论
     * @param apComment
     */
    void computeHotComment(ApComment apComment);
}
