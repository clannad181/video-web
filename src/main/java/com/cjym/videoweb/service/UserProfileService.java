package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.entity.UserProfile;

import java.util.List;

/**
 * 用户画像 Service
 * <p>
 * 基于用户观看历史（视频标签与分类）计算兴趣权重并落表存储，
 * 供推荐等模块内部使用，不对外提供接口。
 */
public interface UserProfileService extends IService<UserProfile> {

    /**
     * 重建单个用户的画像：基于最近 {@code PROFILE_WINDOW_DAYS} 天的观看历史，
     * 按「观看时长占比 × 时间衰减」聚合计算兴趣标签与分类得分，覆盖写入 user_profile 表
     *
     * @param userId 用户ID
     */
    void rebuildProfile(Long userId);

    /**
     * 查询用户的兴趣画像（标签与分类混合，按兴趣得分降序），
     * 供推荐等模块内部使用，不对外提供接口
     *
     * @param userId 用户ID
     * @return 兴趣画像列表（无画像或用户不存在时为空列表）
     */
    List<UserProfile> listInterests(Long userId);
}
