package com.heima.comment.service.impl;

import com.heima.apis.user.IUserClient;
import com.heima.comment.service.ApCommentHotService;
import com.heima.comment.service.ApCommentService;
import com.heima.model.comment.dtos.CommentDto;
import com.heima.model.comment.dtos.CommentLikeDto;
import com.heima.model.comment.dtos.CommentSaveDto;
import com.heima.model.comment.pojos.ApComment;
import com.heima.model.comment.pojos.ApCommentLike;
import com.heima.model.comment.vos.CommentVo;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.common.ThreadLocalUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Log4j2
public class ApCommentServiceImpl implements ApCommentService {

    @Autowired
    private IUserClient userClient;

    @Autowired
    private MongoTemplate mongoTemplate;


    @Autowired
    private ApCommentHotService apCommentHotService;

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
        ApCommentLike apCommentLike = mongoTemplate.findOne(Query.query(Criteria.where("userId")
                .is(user.getId()).and("commentId")
                .is(dto.getCommentId())), ApCommentLike.class);

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
        //当前评论是普通评论且点赞数超过5，那么就提交异步计算热点评论的任务
        if(dto.getOperation()==0){
            if(apComment.getFlag()==0 && apComment.getLikes()>5){
                apCommentHotService.computeHotComment(apComment);
            }
        }
        //4.查询评论数据获取点赞数，封装到map
        apComment = mongoTemplate.findById(dto.getCommentId(), ApComment.class);

        Map map = new HashedMap();
        map.put("likes",apComment.getLikes()); //最点赞数

        return ResponseResult.okResult(map);
    }


    @Override
    public ResponseResult load(CommentDto dto) {
        //1.判断参数
        if(dto==null || dto.getArticleId()==null || dto.getArticleId()<0){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        int size = 10;//默认查询10条数据
        List<ApComment> apCommentList = new ArrayList<>();
        if (dto.getIndex() == 1) { //如果来自于首页请求， 查询热点评论列表+剩余普通评论列表
            //1）.查询热点评论列表（查询条件：文章ID、内容类型、flag=1， 结果：按照点赞数倒排序  限制查询5条）
            List<ApComment> apCommentHotList = mongoTemplate.find(Query.query(Criteria.where("objectId")
                    .is(dto.getArticleId()) //文章ID
                    .and("type").is(0) //内容类型 0-文章
                    .and("flag").is(1) //热点评论
                    .and("createdTime").lt(dto.getMinDate() //创建时间
                    )).with(Sort.by(Sort.Direction.DESC, "likes")//按照点赞数倒排序
            ).limit(5), ApComment.class);
            if (apCommentHotList == null) {
                apCommentHotList = new ArrayList<>();
            }
            apCommentList.addAll(apCommentHotList);
            //2）.查询剩余普通评论列表（查询条件：文章ID、内容类型、flag=0， 结果：按照创建时间倒排序  限制查询10-热点评论列表实际查询到的条数）
            List<ApComment> apCommentCommonList = mongoTemplate.find(Query.query(Criteria.where("objectId")
                    .is(dto.getArticleId()) //文章ID
                    .and("type").is(0) //内容类型 0-文章
                    .and("flag").is(0) //普通评论
                    .and("createdTime").lt(dto.getMinDate() //创建时间
                    )).with(Sort.by(Sort.Direction.DESC, "createdTime")//按照时间倒排序
            ).limit(size - apCommentHotList.size()), ApComment.class);

            apCommentList.addAll(apCommentCommonList);
        } else { //如果来自于非首页请求， 查询普通评论列表（查询条件：文章ID、内容类型、flag=0， 结果：按照创建时间倒排序  限制10条）
            apCommentList = mongoTemplate.find(Query.query(Criteria.where("objectId")
                    .is(dto.getArticleId()) //文章ID
                    .and("type").is(0) //内容类型 0-文章
                    .and("flag").is(0) //普通评论
                    .and("createdTime").lt(dto.getMinDate() //创建时间
                    )).with(Sort.by(Sort.Direction.DESC, "createdTime")//按照时间倒排序
            ).limit(size), ApComment.class);
        }
        if(apCommentList==null){
            apCommentList = new ArrayList<>();
        }

        //2.判断用户如果未登录，直接响应数据
        Integer userId = ThreadLocalUtil.getUserId();
        if(userId==0){
            return ResponseResult.okResult(apCommentList);
        }
        //3.如果已登录，处理哪些评论是当前用户点赞过的

        //3.1 获取所有评论ID列表
        List<String> commentIdList = apCommentList.stream().map(ApComment::getId).collect(Collectors.toList());


        //3.2 查询点赞记录mongo集合中所有当前用户点赞过的记录
        List<ApCommentLike> apCommentLikeList = mongoTemplate.find(Query.query(Criteria.where("userId").is(userId) //当前登录用户ID
                        .and("commentId").in(commentIdList) //评论ID集合
                        .and("operation").is(0) //类型是点赞
                )
                , ApCommentLike.class);

        //3.3 遍历并查找点过过的记录对应的评论数据
        List<CommentVo> commentVoList = new ArrayList<>();
        for (ApComment apComment : apCommentList) {

            //带标识的评论数据
            CommentVo commentVo = new CommentVo();
            BeanUtils.copyProperties(apComment,commentVo);
            commentVoList.add(commentVo);

            //用户点赞记录里，如果文章评论列表里，某个评论ID在当前用户点赞记录里出现了，则肯定能查到，count一定大于0，否则就跳过后续步骤执行下一次比较
            long count = apCommentLikeList.stream().filter(x -> x.getCommentId().equals(apComment.getId())).count();
            if(count>0){
                //如果点赞过，那么count>0, 标识下当前评论被点赞过
                commentVo.setOperation(0);
            }

        }

        //4.响应最终登录后用户获取到的所有评论数据
        return ResponseResult.okResult(commentVoList);
    }
}
