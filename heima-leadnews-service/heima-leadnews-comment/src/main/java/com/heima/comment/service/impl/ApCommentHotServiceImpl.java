package com.heima.comment.service.impl;

import com.heima.comment.service.ApCommentHotService;
import com.heima.model.comment.pojos.ApComment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
public class ApCommentHotServiceImpl implements ApCommentHotService {


    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     *  执行计算热门评论的异步任务
     * @param apComment
     */
    @Async("taskExecutor")
    @Override
    public void computeHotComment(ApComment apComment) {

        //1.根据文章ID查询所有的热点评论，结果按照点赞数倒排序
        List<ApComment> apCommentList = mongoTemplate.find(Query.query(Criteria
                        .where("objectId").is(apComment.getObjectId())  //文章ID
                        .and("type").is(apComment.getType()) //内容类型
                        .and("flag").is(1) //热点评论
                ).with(Sort.by(Sort.Direction.DESC, "likes"))//根据点赞数倒排序
                , ApComment.class);

        if(apCommentList==null){
            apCommentList = new ArrayList<>();
        }

        //2.判断热点评论列表数量，如果小于5，直接将当前评论保存为热点评论
        if(apCommentList.size()<5){
            apComment.setFlag(1);//热点评论
            apComment.setUpdatedTime(new Date());
            mongoTemplate.save(apComment);
        } else {
            //3.如果大于等于5，进行比较（当前评论的点赞数量与评论列表中最后一条评论的点赞数量比较）
            ApComment apCommentLast = apCommentList.get(apCommentList.size() - 1);

            if(apComment.getLikes()>apCommentLast.getLikes()){

                //将最后一条评论设置为普通评论
                apCommentLast.setFlag(0);
                apCommentLast.setUpdatedTime(new Date());
                mongoTemplate.save(apCommentLast);

                //将当前评论设置为热点评论
                apComment.setFlag(1);//热点评论
                apComment.setUpdatedTime(new Date());
                mongoTemplate.save(apComment);
            }

        }
    }
}
