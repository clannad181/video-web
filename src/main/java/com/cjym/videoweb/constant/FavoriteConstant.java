package com.cjym.videoweb.constant;

public class FavoriteConstant {

    /** 默认收藏夹名称 */
    public static final String DEFAULT_FOLDER_NAME = "默认收藏夹";
    /** 收藏夹前台标识前缀 */
    public static final String FOLDER_PREFIX = "fd";
    /** 收藏夹名称最大长度 */
    public static final int FOLDER_NAME_MAX_LENGTH = 20;
    /** 收藏夹自定义封面URL最大长度 */
    public static final int FOLDER_COVER_MAX_LENGTH = 500;
    /** 收藏夹描述最大长度 */
    public static final int FOLDER_DESC_MAX_LENGTH = 200;
    /** 每用户收藏夹数量上限 */
    public static final int FOLDER_COUNT_MAX = 50;
    /** 收藏夹可见性：公开 */
    public static final int FOLDER_PUBLIC = 0;
    /** 收藏夹可见性：私密 */
    public static final int FOLDER_PRIVATE = 1;
    /** 收藏状态：已收藏 */
    public static final int FAVORITE_ACTIVE = 1;
    /** 收藏状态：已取消 */
    public static final int FAVORITE_CANCELLED = 0;
}
