package com.itheima.mongo.test;

import com.itheima.mongo.pojo.ApComment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MongoTest01 {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 测试mongo查询
     */
    @Test
    public void testSelect(){
        //1.查询所有
        List<ApComment> all = mongoTemplate.findAll(ApComment.class);
        System.out.println("查询所有："+all);
        //2.根据条件查询
        List<ApComment> apCommentList = mongoTemplate.find(Query.query(Criteria.where("authorName").is("admin1")), ApComment.class);
        System.out.println("根据条件查询："+apCommentList);

    }



}
