package com.heima.comment.service.impl;

import com.heima.apis.user.IUserClient;
import com.heima.comment.service.ApCommentService;
import com.heima.model.comment.dtos.CommentSaveDto;
import com.heima.model.comment.pojos.ApComment;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.common.ThreadLocalUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Log4j2
public class ApCommentServiceImpl implements ApCommentService {

    @Autowired
    private IUserClient userClient;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public ResponseResult save(CommentSaveDto dto) {

        Integer userId = ThreadLocalUtil.getUserId();
        //1.调用feign接口查询用户是否存在
        ApUser user = userClient.findById(userId);
        if(user==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "app用户不存在");
        }

        //TODO 2.调用feign接口查询文章是否存在

        //3.判断文章内容是否超过140字
        if(dto.getContent().length()>140){
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"内容超过140字");
        }

        //TODO 4.安全过滤（本文内容2阶段检测）

        //5.构建评论数据并保存到mongo集合中
        ApComment apComment = new ApComment();
        apComment.setObjectId(dto.getArticleId());//文章ID
        apComment.setType(0);//内容类型，0-文章 1-动态
        apComment.setUserId(user.getId());//评论用户ID
        apComment.setUserName(user.getName());//评论用户名
        apComment.setImage(user.getImage());//评论用户头像
        apComment.setFlag(0);//0-普通评论 1-热门评论
        apComment.setLikes(0);//点赞数
        apComment.setReply(0);//回复数
        apComment.setContent(dto.getContent());//评论内容
        apComment.setCreatedTime(new Date());
        apComment.setUpdatedTime(new Date());
        mongoTemplate.save(apComment);

        //7.响应数据
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
