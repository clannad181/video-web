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

    // ==================== Token 有效期配置 key（存放在 web_config 表） ====================
    /** 前台 Access Token 有效期配置键 */
    public static final String FRONT_ACCESS_TOKEN_EXPIRY = "front_access_token_expiry";
    /** 前台 Refresh Token 有效期配置键 */
    public static final String FRONT_REFRESH_TOKEN_EXPIRY = "front_refresh_token_expiry";
    /** 后台 Access Token 有效期配置键 */
    public static final String BACKEND_ACCESS_TOKEN_EXPIRY = "backend_access_token_expiry";
    /** 后台 Refresh Token 有效期配置键 */
    public static final String BACKEND_REFRESH_TOKEN_EXPIRY = "backend_refresh_token_expiry";
}
