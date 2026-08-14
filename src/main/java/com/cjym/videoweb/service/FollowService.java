package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.entity.Follow;

public interface FollowService extends IService<Follow> {

    /**
     * 关注UP主（幂等，已关注则直接返回成功）
     */
    void follow(Long followUserId);

    /**
     * 取消关注UP主（幂等，未关注时不做任何操作）
     */
    void unfollow(Long followUserId);

    /**
     * 判断当前用户是否已关注该UP主（未登录或参数为空返回 false）
     */
    boolean isFollowed(Long followUserId);
}
