package com.heima.model.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskTypeEnum {

    WM_NEWS(1001, 1,"自媒体文章"),
    FETCH_NEWS(1002, 2,"爬虫文章");
    private final int taskType; //对应具体业务
    private final int priority; //业务不同级别
    private final String desc; //描述信息
}