package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.FollowMapper;
import com.cjym.videoweb.mapper.UserInfoMapper;
import com.cjym.videoweb.model.dto.FollowListDto;
import com.cjym.videoweb.model.entity.Follow;
import com.cjym.videoweb.model.entity.UserInfo;
import com.cjym.videoweb.model.vo.FansListVo;
import com.cjym.videoweb.model.vo.FollowListVo;
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
    public void follow(String followUserUid) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        // 按 uid 解析 UP主主键id（避免主键在网络上传输）
        Long followUserId = resolveFollowUserId(followUserUid);
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
    public void unfollow(String followUserUid) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        Long followUserId = resolveFollowUserId(followUserUid);
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, currentUserId)
                .eq(Follow::getFollowUserId, followUserId);
        // 逻辑删除（幂等，未关注时删除0行）
        baseMapper.delete(wrapper);
    }

    @Override
    public boolean isFollowed(String followUserUid) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }
        // 查询失败（uid为空或UP主不存在）视为未关注
        Long followUserId;
        try {
            followUserId = resolveFollowUserId(followUserUid);
        } catch (GlobeException e) {
            return false;
        }
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, currentUserId)
                .eq(Follow::getFollowUserId, followUserId);
        return baseMapper.selectCount(wrapper) > 0;
    }

    @Override
    public IPage<FollowListVo> pageList(FollowListDto dto) {
        Long currentUserId = requireLogin();
        int page = dto.getPage() != null ? dto.getPage() : 1;
        int size = dto.getSize() != null ? dto.getSize() : 10;
        return baseMapper.selectFollowPage(new Page<>(page, size), currentUserId, null);
    }

    @Override
    public IPage<FollowListVo> pageSearch(String keyword, Integer page, Integer size) {
        Long currentUserId = requireLogin();
        int pageNo = page != null ? page : 1;
        int pageSize = size != null ? size : 10;
        String trimmed = StringUtils.isEmpty(keyword) ? null : keyword.trim();
        return baseMapper.selectFollowPage(new Page<>(pageNo, pageSize), currentUserId, trimmed);
    }

    @Override
    public IPage<FansListVo> pageFans(String keyword, Integer page, Integer size) {
        Long currentUserId = requireLogin();
        int pageNo = page != null ? page : 1;
        int pageSize = size != null ? size : 10;
        String trimmed = StringUtils.isEmpty(keyword) ? null : keyword.trim();
        return baseMapper.selectFansPage(new Page<>(pageNo, pageSize), currentUserId, trimmed);
    }


    /**
     * 按 uid 解析 UP主主键id（uid为空或用户不存在时抛错）
     */
    private Long resolveFollowUserId(String followUserUid) {
        if (followUserUid == null || followUserUid.isBlank()) {
            throw new GlobeException("UP主uid不能为空");
        }
        UserInfo followUser = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUid, followUserUid));
        if (followUser == null) {
            throw new GlobeException("UP主不存在");
        }
        return followUser.getId();
    }

    /**
     * 获取当前登录用户ID，未登录抛异常
     */
    private Long requireLogin() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        return currentUserId;
    }

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
            return JWTUtil.getUserId(token);
        } catch (Exception e) {
            return null;
        }
    }
}
