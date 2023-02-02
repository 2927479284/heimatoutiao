package com.heima.model.article.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class ArticleHomeDto {

    // 最大时间
    Date maxBehotTime;
    // 最小时间
    Date minBehotTime;
    // 分页size
    int size = 10; //默认10页
    // 频道ID 默认频道 all
    String tag = "__all__";
}
