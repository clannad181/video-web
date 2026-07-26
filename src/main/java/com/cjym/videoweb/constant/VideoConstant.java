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
}
