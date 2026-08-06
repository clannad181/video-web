package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.UserInfoMapper;
import com.cjym.videoweb.mapper.VideoEpisodeMapper;
import com.cjym.videoweb.mapper.VideoMapper;
import com.cjym.videoweb.mapper.WatchHistoryMapper;
import com.cjym.videoweb.model.dto.WatchHistorySaveDto;
import com.cjym.videoweb.model.entity.UserInfo;
import com.cjym.videoweb.model.entity.Video;
import com.cjym.videoweb.model.entity.VideoEpisode;
import com.cjym.videoweb.model.entity.WatchHistory;
import com.cjym.videoweb.model.vo.WatchHistoryVo;
import com.cjym.videoweb.service.WatchHistoryService;
import com.cjym.videoweb.utils.JWTUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 观看历史记录 Service 实现
 */
@Service
public class WatchHistoryServiceImpl extends ServiceImpl<WatchHistoryMapper, WatchHistory> implements WatchHistoryService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private VideoEpisodeMapper videoEpisodeMapper;

    @Override
    public void record(WatchHistorySaveDto dto) {
        if (dto.getVideoId() == null) {
            throw new GlobeException("视频ID不能为空");
        }
        if (dto.getWatchedSeconds() == null || dto.getWatchedSeconds() < 0) {
            throw new GlobeException("观看时长不能为空");
        }
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }

        // 查询是否已有相同用户+视频+剧集的记录
        LambdaQueryWrapper<WatchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WatchHistory::getUserId, currentUserId)
                .eq(WatchHistory::getVideoId, dto.getVideoId());
        if (dto.getEpisodeId() != null) {
            wrapper.eq(WatchHistory::getEpisodeId, dto.getEpisodeId());
        } else {
            wrapper.isNull(WatchHistory::getEpisodeId);
        }
        WatchHistory existing = baseMapper.selectOne(wrapper);

        // 构建播放跳转链接
        String playLink = buildPlayLink(dto.getVideoId(), dto.getEpisodeId());

        if (existing != null) {
            // 已有记录，更新进度和观看时间
            existing.setWatchedSeconds(dto.getWatchedSeconds());
            existing.setPlayLink(playLink);
            existing.setWatchTime(LocalDateTime.now());
            baseMapper.updateById(existing);
        } else {
            // 新记录，插入
            WatchHistory wh = new WatchHistory();
            wh.setUserId(currentUserId);
            wh.setVideoId(dto.getVideoId());
            wh.setEpisodeId(dto.getEpisodeId());
            wh.setWatchedSeconds(dto.getWatchedSeconds());
            wh.setPlayLink(playLink);
            wh.setWatchTime(LocalDateTime.now());
            baseMapper.insert(wh);
        }
    }

    @Override
    public IPage<WatchHistoryVo> pageHistory(Integer page, Integer size) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        Page<WatchHistoryVo> pageParam = new Page<>(page, size);
        IPage<WatchHistoryVo> result = baseMapper.selectWatchHistoryPage(pageParam, currentUserId);
        // 格式化观看时长为 MM:SS
        for (WatchHistoryVo vo : result.getRecords()) {
            vo.setWatchedTime(formatSeconds(vo.getWatchedSeconds()));
        }
        return result;
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

    /**
     * 将秒数格式化为 MM:SS 或 HH:MM:SS
     */
    private String formatSeconds(Long seconds) {
        if (seconds == null || seconds <= 0) {
            return "00:00";
        }
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        }
        return String.format("%02d:%02d", minutes, secs);
    }

    /**
     * 根据视频ID和剧集ID构建播放跳转链接
     * <p>
     * 有剧集：/admin/video/{videoAvid}/episode/{episodeAvid}
     * 无剧集：/admin/video/{videoAvid}
     */
    private String buildPlayLink(Long videoId, Long episodeId) {
        Video video = videoMapper.selectById(videoId);
        if (video == null || video.getAvid() == null) {
            return null;
        }
        if (episodeId != null) {
            VideoEpisode episode = videoEpisodeMapper.selectById(episodeId);
            if (episode != null && episode.getAvid() != null) {
                return "/admin/video/" + video.getAvid() + "/episode/" + episode.getAvid();
            }
        }
        return "/admin/video/" + video.getAvid();
    }
}
