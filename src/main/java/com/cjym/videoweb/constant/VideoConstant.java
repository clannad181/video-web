package com.cjym.videoweb.constant;

public class VideoConstant {
    /** 剧集审核状态：待审核 */
    public static final int REVIEW_PENDING = 0;
    /** 剧集审核状态：审核通过 */
    public static final int REVIEW_APPROVED = 1;
    /** 剧集审核状态：审核拒绝 */
    public static final int REVIEW_REJECTED = 2;
    /** 审核状态最大值 */
    public static final int REVIEW_STATUS_MAX = REVIEW_REJECTED;

    /** 视频状态：待发布（用户投稿后等待审核发布） */
    public static final int VIDEO_STATUS_PENDING = 1;
    /** 视频状态：已发布（前台仅可播放已发布的视频） */
    public static final int VIDEO_STATUS_PUBLISHED = 2;

    /** 前台视频排序类型：最新（按发布时间倒序） */
    public static final String SORT_LATEST = "latest";
    /** 前台视频排序类型：最火热（按播放量倒序） */
    public static final String SORT_HOT = "hot";
    /** 前台视频排序类型：最多收藏（按收藏数倒序） */
    public static final String SORT_FAVORITE = "favorite";
    public static final Integer VIDEO_STATUS_DRAFT = 0;
}
