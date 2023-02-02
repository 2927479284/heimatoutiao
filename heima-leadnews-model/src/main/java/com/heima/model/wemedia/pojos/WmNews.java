package com.heima.model.wemedia.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 自媒体图文内容信息表
 * </p>
 *
 * @author itheima
 */
@Data
@TableName("wm_news")
public class WmNews implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 自媒体用户ID
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 图文内容
     */
    @TableField("content")
    private String content;

    /**
     * 封面布局方式
            0 无图文章
            1 单图文章
            3 多图文章
     */
    @TableField("type")
    private Integer type;

    /**
     * 图文频道ID
     */
    @TableField("channel_id")
    private Integer channelId;

    @TableField("labels")
    private String labels;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private Date createdTime;

    /**
     * 提交时间
     */
    @TableField("submited_time")
    private Date submitedTime;

    /**
     * 当前状态
     0 草稿
     1 提交（待自动审核）
     2 审核失败（自动审核失败或人工审核失败）
     3 待人工审核
     4 人工审核通过
     8 自动审核通过（待发布）
     9 已发布
     */
    @TableField("status")
    private Integer status;

    /**
     * 文章定时发布时间
     */
    @TableField("publish_time")
    private Date publishTime;

    /**
     * 拒绝理由
     */
    @TableField("reason")
    private String reason;

    /**
     * APP端文章ID
     */
    @TableField("article_id")
    private Long articleId;

    /**
     * 图片地址，多个图片地址用逗号拼接
     */
    @TableField("images")
    private String images;

    /**
     * 0-未上架 1-已上架
     */
    @TableField("enable")
    private Integer enable;
    
     //状态枚举类
    @Alias("WmNewsStatus")
    public enum Status{
        NORMAL(0),SUBMIT(1),FAIL(2),ADMIN_AUTH(3),ADMIN_SUCCESS(4),SUCCESS(8),PUBLISHED(9);
        Integer code;
        Status(Integer code){
            this.code = code;
        }
        public Integer getCode(){
            return this.code;
        }
    }

}