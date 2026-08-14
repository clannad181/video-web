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

    /** 前台视频排序类型：最新（按发布时间倒序） */
    public static final String SORT_LATEST = "latest";
    /** 前台视频排序类型：最火热（按播放量倒序） */
    public static final String SORT_HOT = "hot";
}
