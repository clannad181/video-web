package com.cjym.videoweb.constant;

public class BaseConstant {
    /**
     * 密码盐值分隔符
     * */
    public static final String SALT_SPITE_IDX ="$";

    /**
     * 后台系统
     * */
    public static final String SYS_ADMIN="admin";

    /**
     * 前台系统
     * */
    public static final String SYS_FRONT="front";

    /**
     * refresh_token前缀
     * */
    public static final String REFRESH_TOKEN_PREFIX="refresh_token:";

    /**
     * 允许登录的token前缀
     * */
    public static final String USER_TOKENS_ZSET_PREFIX = "user_tokens:";

    /**
     * 同一个账号最大登录数
     * */
    public static final Integer MAX_SESSIONS=3;

    /**
     * token类型-access-token
     * */
    public static final String TOKEN_ACCESS="access";

    /**
     * token类型-refresh-token
     * */
    public static final String TOKEN_REFRESH="refresh";

    /**
     * 用户类型-管理员
     * */
    public static final Integer USER_TYPE_ADMIN=1;

    /**
     * 用户类型-用户
     * */
    public static final Integer USER_TYPE_USER=0;

    public static final Long DEFAULT_FRONT_USER_ROLE_ID = 9999L;

    /**
     * 前台视频推荐数量上限
     */
    public static final Integer VIDEO_RECOMMEND_SIZE = 6;

    /**
     * 相关视频推荐候选视频数量上限（先查候选id再批量查详情，防止候选集合过大）
     */
    public static final Integer RELATED_VIDEO_CANDIDATE_LIMIT = 500;

    /**
     * 最近点赞/投币视频数量上限
     */
    public static final Integer RECENT_ACTION_LIMIT = 20;

    /**
     * 前台个人数据中心榜单数量上限（最热/最受欢迎视频TOP数）
     */
    public static final Integer DATA_CENTER_TOP_SIZE = 5;

    // ==================== Token 有效期配置 key（存放在 web_config 表） ====================
    /** 前台 Access Token 有效期配置键 */
    public static final String FRONT_ACCESS_TOKEN_EXPIRY = "front_access_token_expiry";
    /** 前台 Refresh Token 有效期配置键 */
    public static final String FRONT_REFRESH_TOKEN_EXPIRY = "front_refresh_token_expiry";
    /** 后台 Access Token 有效期配置键 */
    public static final String BACKEND_ACCESS_TOKEN_EXPIRY = "backend_access_token_expiry";
    /** 后台 Refresh Token 有效期配置键 */
    public static final String BACKEND_REFRESH_TOKEN_EXPIRY = "backend_refresh_token_expiry";

    // ==================== WebConfig Redis 缓存 Key 常量 ====================
    /** WebConfig Redis 缓存 key 前缀 */
    public static final String WEB_CONFIG_REDIS_KEY_PREFIX = "web_config:list:";

    // ==================== WebConfig 分类与键常量 ====================
    /** WebConfig 默认分类 */
    public static final String WEB_CONFIG_CATEGORY_DEFAULT = "default";
    /** WebConfig review 分类 */
    public static final String WEB_CONFIG_CATEGORY_REVIEW = "review";
    /** WebConfig display 分类 */
    public static final String WEB_CONFIG_CATEGORY_DISPLAY = "display";
    /** WebConfig search 分类 */
    public static final String WEB_CONFIG_CATEGORY_SEARCH = "search";
    /** WebConfig token 有效期分类 */
    public static final String WEB_CONFIG_CATEGORY_TOKEN_EXPIRY = "token_security";
    /** WebConfig 默认角色配置键 */
    public static final String WEB_CONFIG_KEY_DEFAULT_ROLE = "default_role";
    /** WebConfig 视频免审配置键 */
    public static final String WEB_CONFIG_KEY_VIDEO_REVIEW = "video_review";
    /** WebConfig 评论免审配置键 */
    public static final String WEB_CONFIG_KEY_COMMENT_REVIEW = "comment_review";
    /** WebConfig 聚合搜索是否走 Elasticsearch 配置键 */
    public static final String WEB_CONFIG_KEY_ES_SEARCH = "es_search";

    // ==================== WebConfig 通用值常量 ====================
    /** WebConfig 布尔真值 */
    public static final String WEB_CONFIG_VALUE_TRUE = "true";
}
