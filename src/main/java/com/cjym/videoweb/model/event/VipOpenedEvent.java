package com.cjym.videoweb.model.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 大会员开通成功事件：支付回调开通大会员后发布，用于解耦站内消息通知等后续动作，
 * 业务侧只发布事件，不感知具体通知实现
 */
@Getter
public class VipOpenedEvent {

    /** 开通用户id */
    private final Long userId;

    /** 开通月数 */
    private final Integer durationMonths;

    /** 新到期时间 */
    private final LocalDateTime expireTime;

    public VipOpenedEvent(Long userId, Integer durationMonths, LocalDateTime expireTime) {
        this.userId = userId;
        this.durationMonths = durationMonths;
        this.expireTime = expireTime;
    }
}
