package com.cjym.videoweb.constant;

public class ReportConstant {

    /** 举报类型：色情低俗 */
    public static final int TYPE_PORNOGRAPHY = 1;
    /** 举报类型：政治敏感 */
    public static final int TYPE_POLITICAL = 2;
    /** 举报类型：暴恐血腥 */
    public static final int TYPE_VIOLENCE = 3;
    /** 举报类型：虚假信息 */
    public static final int TYPE_MISINFORMATION = 4;
    /** 举报类型：侵权抄袭 */
    public static final int TYPE_COPYRIGHT = 5;
    /** 举报类型：恶意引战 */
    public static final int TYPE_TROLLING = 6;
    /** 举报类型：其他 */
    public static final int TYPE_OTHER = 7;
    /** 举报类型最大值 */
    public static final int TYPE_MAX = TYPE_OTHER;
    /** 举报类型最小值 */
    public static final int TYPE_MIN = TYPE_PORNOGRAPHY;

    /** 处理状态：待处理 */
    public static final int STATUS_PENDING = 0;
    /** 处理状态：已受理（确认违规） */
    public static final int STATUS_ACCEPTED = 1;
    /** 处理状态：已驳回（未违规） */
    public static final int STATUS_REJECTED = 2;
    /** 处理状态最大值 */
    public static final int STATUS_MAX = STATUS_REJECTED;
}
