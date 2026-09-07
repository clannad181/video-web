package com.cjym.videoweb.constant;

/**
 * 用户画像相关常量
 */
public class UserProfileConstant {

    /** 兴趣维度：标签 */
    public static final String ITEM_TYPE_TAG = "tag";

    /** 兴趣维度：分类 */
    public static final String ITEM_TYPE_CATEGORY = "category";

    /** 画像统计时间窗：仅统计最近180天的观看历史（天） */
    public static final int PROFILE_WINDOW_DAYS = 180;

    /** 兴趣时间衰减半衰期：每过30天兴趣权重减半（天） */
    public static final int HALF_LIFE_DAYS = 30;
}
