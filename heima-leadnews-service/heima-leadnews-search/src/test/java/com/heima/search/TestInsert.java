package com.heima.search;


import com.heima.model.search.pojos.ApAssociateWords;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestInsert {



    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 新增101个关键词，做关键词联想词系列
     */
    @Test
    public void insert(){

        for (int i = 0; i < 100; i++) {
            ApAssociateWords apAssociateWords = new ApAssociateWords();
            apAssociateWords.setAssociateWords("黑马"+i);
            apAssociateWords.setCreatedTime(new Date());
            apAssociateWords.setId(String.valueOf(i));
            mongoTemplate.save(apAssociateWords);
        }
    }
}
