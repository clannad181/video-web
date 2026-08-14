package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.FollowMapper;
import com.cjym.videoweb.mapper.UserInfoMapper;
import com.cjym.videoweb.model.entity.Follow;
import com.cjym.videoweb.model.entity.UserInfo;
import com.cjym.videoweb.service.FollowService;
import com.cjym.videoweb.utils.JWTUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public void follow(Long followUserId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        if (followUserId == null) {
            throw new GlobeException("UP主ID不能为空");
        }
        if (currentUserId.equals(followUserId)) {
            throw new GlobeException("不能关注自己");
        }
        // 已有取关记录则直接恢复
        if (baseMapper.restoreFollow(currentUserId, followUserId) > 0) {
            return;
        }
        // 已关注则幂等返回
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, currentUserId)
                .eq(Follow::getFollowUserId, followUserId);
        if (baseMapper.selectCount(wrapper) > 0) {
            return;
        }
        Follow follow = new Follow();
        follow.setUserId(currentUserId);
        follow.setFollowUserId(followUserId);
        baseMapper.insert(follow);
    }

    @Override
    public void unfollow(Long followUserId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        if (followUserId == null) {
            throw new GlobeException("UP主ID不能为空");
        }
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, currentUserId)
                .eq(Follow::getFollowUserId, followUserId);
        // 逻辑删除（幂等，未关注时删除0行）
        baseMapper.delete(wrapper);
    }

    @Override
    public boolean isFollowed(Long followUserId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null || followUserId == null) {
            return false;
        }
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, currentUserId)
                .eq(Follow::getFollowUserId, followUserId);
        return baseMapper.selectCount(wrapper) > 0;
    }

    // ==================== 私有方法 ====================

    /**
     * 从请求 JWT token 中获取当前用户ID
     */
    private Long getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            HttpServletRequest request = attributes.getRequest();
            String token = request.getHeader("authorization");
            if (StringUtils.isEmpty(token)) {
                return null;
            }
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            String username = JWTUtil.getUsername(token);
            UserInfo userInfo = userInfoMapper.selectByUserName(username);
            return userInfo != null ? userInfo.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
