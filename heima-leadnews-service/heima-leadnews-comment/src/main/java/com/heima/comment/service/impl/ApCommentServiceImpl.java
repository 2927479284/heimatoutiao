package com.heima.comment.service.impl;

import com.heima.apis.user.IUserClient;
import com.heima.comment.service.ApCommentService;
import com.heima.model.comment.dtos.CommentLikeDto;
import com.heima.model.comment.dtos.CommentSaveDto;
import com.heima.model.comment.pojos.ApComment;
import com.heima.model.comment.pojos.ApCommentLike;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.common.ThreadLocalUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

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


    @Override
    public ResponseResult like(CommentLikeDto dto) {

        Integer userId = ThreadLocalUtil.getUserId();
        //1.调用feign接口查询用户是否存在
        ApUser user = userClient.findById(userId);
        if(user==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "app用户不存在");
        }

        //2.查询评论判断是否存在
        ApComment apComment = mongoTemplate.findById(dto.getCommentId(), ApComment.class);
        if(apComment==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "评论不存在" );
        }

        //3.查询点赞记录
        ApCommentLike apCommentLike = mongoTemplate.findOne(Query.query(Criteria.where("userId").is(user.getId()).and("commentId").is(dto.getCommentId())), ApCommentLike.class);

        if(apCommentLike==null){ //3.1如果点赞记录不存在

            //3.1.1 如果操作类型是取消点赞，响应错误码提示不能取消点赞
            if(dto.getOperation()==1){
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"点赞记录不存在，不能取消点赞");
            }

            //3.1.2 如果操作类型是点赞，更新点赞数+1且保存点赞记录

            //更新评论点赞数+1
            mongoTemplate.findAndModify(Query.query(Criteria.where("id").is(dto.getCommentId())),new Update().inc("likes",1),ApComment.class);

            //保存点赞记录
            apCommentLike = new ApCommentLike();
            apCommentLike.setUserId(user.getId());//点赞用户ID
            apCommentLike.setCommentId(dto.getCommentId());//评论ID
            apCommentLike.setOperation(dto.getOperation());
            apCommentLike.setCreatedTime(new Date());//创建时间
            apCommentLike.setUpdatedTime(new Date());//更新时间
            mongoTemplate.save(apCommentLike);


        } else { //3.2.如果点赞记录存在
            //3.2.1 如果操作类型是点赞且数据也是点赞，响应错误码提示重复点赞
            if(dto.getOperation()==0 && apCommentLike.getOperation()==0){
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "不能重复点赞");
            }

            //3.2.2 如果超过类型是取消点赞且数据也是取消点赞，响应错误码提示重复取消点赞
            if(dto.getOperation()==1 && apCommentLike.getOperation()==1){
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "不能重复取消点赞");
            }

            //3,2.3 判断如果操作类型是点赞，则更新点赞次数+1
            if(dto.getOperation()==0){
                mongoTemplate.findAndModify(Query.query(Criteria.where("id").is(dto.getCommentId())),
                        new Update().inc("likes",1).set("updatedTime",new Date()),
                        ApComment.class);

            } else {//3.2.4 判断如果操作类型是取消点赞，则更新点赞次数-1
                mongoTemplate.findAndModify(Query.query(Criteria.where("id").is(dto.getCommentId())),
                        new Update().inc("likes",-1).set("updatedTime",new Date()),
                        ApComment.class);
            }

            //3.2.5 更新点赞记录
            apCommentLike.setUpdatedTime(new Date());
            apCommentLike.setOperation(dto.getOperation());
            mongoTemplate.save(apCommentLike);
        }

        //4.查询评论数据获取点赞数，封装到map
        apComment = mongoTemplate.findById(dto.getCommentId(), ApComment.class);

        Map map = new HashedMap();
        map.put("likes",apComment.getLikes()); //最点赞数

        return ResponseResult.okResult(map);
    }
}
