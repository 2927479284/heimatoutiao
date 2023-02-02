package com.heima.model.mess;

import lombok.Data;

@Data
public class ArticleBehaviorData {

    /**
     * 修改文章的字段类型
      */
    private ArticleBehaviorType type;
    /**
     * 文章ID
     */
    private Long articleId;


    public enum ArticleBehaviorType{
        COLLECTION,COMMENT,LIKES,VIEWS;
    }
}