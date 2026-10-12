package com.cjym.videoweb.constant;

public class MessageConstant {

    /** 消息类型：UP主视频发布成功 */
    public static final int TYPE_VIDEO_PUBLISHED = 1;
    /** 消息类型：稿件审核通知 */
    public static final int TYPE_VIDEO_REVIEW = 2;
    /** 消息类型：稿件被举报下架通知 */
    public static final int TYPE_VIDEO_TAKEDOWN = 3;
    /** 消息类型：服务器运行状态通知 */
    public static final int TYPE_SERVER_STATUS = 4;
    /** 消息类型：评论通知（视频收到新评论） */
    public static final int TYPE_COMMENT = 5;
    /** 消息类型：举报处理结果通知 */
    public static final int TYPE_REPORT = 6;
    /** 消息类型：评论被运营屏蔽通知 */
    public static final int TYPE_COMMENT_BLOCKED = 7;
    /** 消息类型：大会员到期通知 */
    public static final int TYPE_VIP_EXPIRE = 8;
    /** 消息类型最小值 */
    public static final int TYPE_MIN = TYPE_VIDEO_PUBLISHED;
    /** 消息类型最大值 */
    public static final int TYPE_MAX = TYPE_VIP_EXPIRE;

    /** 已读状态：未读 */
    public static final int READ_UNREAD = 0;
    /** 已读状态：已读 */
    public static final int READ_READ = 1;

    // ==================== 服务器告警去重 ====================
    /** 服务器告警 Redis 去重 key 前缀 */
    public static final String MONITOR_ALERT_REDIS_KEY_PREFIX = "monitor:alert:";
    /** 服务器告警去重窗口（分钟）：同一指标告警后该时间内不重复发送 */
    public static final long MONITOR_ALERT_INTERVAL_MINUTES = 30;

    // ==================== 服务器告警默认阈值（%） ====================
    /** CPU 使用率告警默认阈值 */
    public static final double DEFAULT_CPU_THRESHOLD = 80;
    /** 内存使用率告警默认阈值 */
    public static final double DEFAULT_MEMORY_THRESHOLD = 85;
    /** 磁盘使用率告警默认阈值 */
    public static final double DEFAULT_DISK_THRESHOLD = 90;

    // ==================== 消息标题与内容模板 ====================
    /** 视频发布成功-标题 */
    public static final String TITLE_VIDEO_PUBLISHED = "视频发布成功";
    /** 视频发布成功-内容 */
    public static final String CONTENT_VIDEO_PUBLISHED = "恭喜！您的视频《%s》已发布成功，快去分享给朋友们吧！";
    /** 稿件审核通知-标题 */
    public static final String TITLE_VIDEO_REVIEW = "稿件审核通知";
    /** 稿件审核通过-内容 */
    public static final String CONTENT_VIDEO_REVIEW_APPROVED = "您的视频《%s》第%d集已审核通过";
    /** 稿件审核拒绝-内容 */
    public static final String CONTENT_VIDEO_REVIEW_REJECTED = "您的视频《%s》第%d集审核未通过，请修改后重新投稿";
    /** 稿件举报下架-标题 */
    public static final String TITLE_VIDEO_TAKEDOWN = "稿件下架通知";
    /** 稿件举报下架-内容 */
    public static final String CONTENT_VIDEO_TAKEDOWN = "您的视频《%s》因被举报违规已下架，如有疑问请联系管理员";
    /** 服务器状态告警-标题 */
    public static final String TITLE_SERVER_STATUS = "服务器运行状态告警";
    /** 服务器状态告警-内容 */
    public static final String CONTENT_SERVER_STATUS = "服务器%s使用率 %.1f%% 超过阈值 %.1f%%，请及时关注处理";
    /** 评论通知-标题 */
    public static final String TITLE_COMMENT_NOTIFY = "收到新评论";
    /** 评论通知-内容 */
    public static final String CONTENT_COMMENT_NOTIFY = "您的视频《%s》收到了一条新评论，快去看看吧！";
    /** 举报处理结果-标题 */
    public static final String TITLE_REPORT_NOTIFY = "举报处理结果通知";
    /** 举报处理结果-受理内容 */
    public static final String CONTENT_REPORT_ACCEPTED = "您举报的视频《%s》经核实存在违规，已作下架处理";
    /** 举报处理结果-驳回内容 */
    public static final String CONTENT_REPORT_REJECTED = "您举报的视频《%s》经核实未发现违规，举报已驳回";
    /** 评论屏蔽通知-标题 */
    public static final String TITLE_COMMENT_BLOCKED = "评论屏蔽通知";
    /** 评论屏蔽通知-内容 */
    public static final String CONTENT_COMMENT_BLOCKED = "您的视频《%s》下有一条评论因违规已被运营屏蔽";
    /** 大会员到期通知-标题 */
    public static final String TITLE_VIP_EXPIRE = "大会员到期通知";
    /** 大会员到期通知-内容 */
    public static final String CONTENT_VIP_EXPIRE = "您的大会员已到期，如需继续享受大会员权益请及时续费";
}
