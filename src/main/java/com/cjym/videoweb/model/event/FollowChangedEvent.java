package com.cjym.videoweb.model.event;

import lombok.Getter;

/**
 * 关注关系变更事件：关注/取关成功后发布，用于解耦 ES 索引等外部同步逻辑，
 * 业务侧只发布事件，不感知具体同步实现
 */
@Getter
public class FollowChangedEvent {

    /** 关注者用户id */
    private final Long userId;

    /** 被关注者（UP主）用户id */
    private final Long followUserId;

    public FollowChangedEvent(Long userId, Long followUserId) {
        this.userId = userId;
        this.followUserId = followUserId;
    }
}
