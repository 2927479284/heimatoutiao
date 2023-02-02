package com.heima.model.comment.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.heima.model.common.annotation.IdEncrypt;
import lombok.Data;

import java.util.Date;

@Data
public class CommentDto {

    /**
     * 文章id
     */
    @IdEncrypt
    private Long articleId;

    /**
     * 最小时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date minDate;


    /**
     * 是否是首页 1-表示首页
     */
    private Integer index;

}