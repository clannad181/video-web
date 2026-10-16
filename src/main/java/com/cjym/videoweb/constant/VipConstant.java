package com.cjym.videoweb.constant;

public class VipConstant {

    /** 记录状态：生效中 */
    public static final int STATUS_ACTIVE = 0;
    /** 记录状态：已到期（定时任务置为1） */
    public static final int STATUS_EXPIRED = 1;
    /** 记录状态：已撤销（管理端撤销置为2，记录保留可追溯） */
    public static final int STATUS_REVOKED = 2;

    /** 按时长开通时单次最大时长（月） */
    public static final int MAX_DURATION_MONTHS = 120;
}
