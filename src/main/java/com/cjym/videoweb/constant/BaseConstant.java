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
     * token类型
     * */
    public static final String TOKEN_ACCESS="access";

    public static final String TOKEN_REFRESH="refresh";
}
