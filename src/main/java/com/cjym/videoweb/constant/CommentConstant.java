package com.cjym.videoweb.constant;

public class CommentConstant {

    /** 评论状态：正常 */
    public static final int STATUS_NORMAL = 0;
    /** 评论状态：用户删除 */
    public static final int STATUS_USER_DELETED = 1;
    /** 评论状态：运营屏蔽 */
    public static final int STATUS_ADMIN_BLOCKED = 2;

    /** 点赞状态：已点赞 */
    public static final int LIKE_ACTIVE = 1;
    /** 点赞状态：已取消 */
    public static final int LIKE_CANCELLED = 0;

    /** 排序方式：按热度（点赞数） */
    public static final String SORT_BY_LIKE = "like";
    /** 排序方式：按时间 */
    public static final String SORT_BY_TIME = "time";
}
