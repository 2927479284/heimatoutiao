package com.heima.common.constants;

public class ArticleConstants {
    /**
     * 加载更多
     */
    public static final Integer LOADTYPE_LOAD_MORE = 1;
    /**
     * 加载更新
     */
    public static final Integer LOADTYPE_LOAD_NEW = 2;
    /**
     * 推荐频道标识
     */
    public static final String DEFAULT_TAG = "__all__";

    /**
     * 文章数据同步Topic
     */
    public static final String ARTICLE_ES_SYNC_TOPIC = "article.es.sync.topic";

    /**
     * 点赞行为权重
     */
    public static final Integer HOT_ARTICLE_LIKE_WEIGHT = 3;
    /**
     * 评论行为权重
     */
    public static final Integer HOT_ARTICLE_COMMENT_WEIGHT = 5;
    /**
     * 收藏行为权重
     */
    public static final Integer HOT_ARTICLE_COLLECTION_WEIGHT = 8;

    /**
     * 热点文章redisKey前缀
     */
    public static final String HOT_ARTICLE_FIRST_PAGE = "hot_article_first_page_";
}