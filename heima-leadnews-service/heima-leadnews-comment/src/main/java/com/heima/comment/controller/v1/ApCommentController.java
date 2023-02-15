package com.heima.comment.controller.v1;

import com.heima.comment.service.ApCommentService;
import com.heima.model.comment.dtos.CommentDto;
import com.heima.model.comment.dtos.CommentLikeDto;
import com.heima.model.comment.dtos.CommentSaveDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comment")
public class ApCommentController {

    @Autowired
    private ApCommentService apCommentService;

    /**
     * 发表评论
     * @param dto
     * @return
     */
    @PostMapping("/save")
    public ResponseResult saveComment(@RequestBody CommentSaveDto dto){
        return apCommentService.save(dto);
    }

    /**
     * 评论点赞/取消
     * @param dto
     * @return
     */
    @PostMapping("/like")
    public ResponseResult like(@RequestBody CommentLikeDto dto){
        return apCommentService.like(dto);
    }


    /**
     * 加载评论
     * @param dto
     * @return
     */
    @PostMapping("/load")
    public ResponseResult load(@RequestBody CommentDto dto){
        return apCommentService.load(dto);
    }
}
