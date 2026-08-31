package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.VideoEpisodeMapper;
import com.cjym.videoweb.mapper.VideoMapper;
import com.cjym.videoweb.mapper.WatchHistoryMapper;
import com.cjym.videoweb.model.dto.WatchHistorySaveDto;
import com.cjym.videoweb.model.dto.WatchHistorySearchDto;
import com.cjym.videoweb.model.entity.Video;
import com.cjym.videoweb.model.entity.VideoEpisode;
import com.cjym.videoweb.model.entity.WatchHistory;
import com.cjym.videoweb.model.vo.DailyActiveVo;
import com.cjym.videoweb.model.vo.DailyIncrementVo;
import com.cjym.videoweb.model.vo.WatchHistoryGroupVo;
import com.cjym.videoweb.model.vo.WatchHistoryMonthVo;
import com.cjym.videoweb.model.vo.WatchHistoryVo;
import com.cjym.videoweb.service.WatchHistoryService;
import com.cjym.videoweb.utils.JWTUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 观看历史记录 Service 实现
 */
@Service
public class WatchHistoryServiceImpl extends ServiceImpl<WatchHistoryMapper, WatchHistory> implements WatchHistoryService {

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

        // 构建播放跳转链接
        String playLink = buildPlayLink(dto.getVideoId(), dto.getEpisodeId());
        WatchHistory wh = new WatchHistory();
        wh.setUserId(currentUserId);
        wh.setVideoId(dto.getVideoId());
        wh.setEpisodeId(dto.getEpisodeId());
        wh.setWatchedSeconds(dto.getWatchedSeconds());
        wh.setPlayLink(playLink);
        wh.setWatchTime(LocalDateTime.now());
        baseMapper.insert(wh);
    }

    @Override
    public void recordPlay(Long videoId, Long episodeId) {
        if (videoId == null) {
            throw new GlobeException("视频ID不能为空");
        }
        // 未登录不记录观看历史
        if (getCurrentUserId() == null) {
            return;
        }
        WatchHistorySaveDto dto = new WatchHistorySaveDto();
        dto.setVideoId(videoId);
        dto.setEpisodeId(episodeId);
        dto.setWatchedSeconds(0L);
        record(dto);
    }

    //todo 分页没用
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

    @Override
    public IPage<WatchHistoryMonthVo> pageHistoryByMonth(WatchHistorySearchDto dto) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        // 查询窗口限制：最多查询2年内的数据（未传时间时默认最近2年）
        LocalDate today = LocalDate.now();
        LocalDate earliest = today.minusYears(2);
        LocalDate startDate = dto.getStartDate() != null ? dto.getStartDate() : earliest;
        LocalDate endDate = dto.getEndDate() != null ? dto.getEndDate() : today;
        if (startDate.isBefore(earliest)) {
            throw new GlobeException("仅支持查询2年内的观看历史");
        }
        if (endDate.isAfter(today)) {
            throw new GlobeException("结束时间不能晚于今天");
        }
        if (startDate.isAfter(endDate)) {
            throw new GlobeException("开始时间不能晚于结束时间");
        }
        int page = dto.getPage() != null ? dto.getPage() : 1;
        int size = dto.getSize() != null ? dto.getSize() : 10;
        String keyword = StringUtils.isEmpty(dto.getKeyword()) ? null : dto.getKeyword().trim();

        // 第一步：按月份分页（每页 size 个月，以月首日表示月份）
        Page<LocalDate> monthPageParam = new Page<>(page, size);
        IPage<LocalDate> monthResult = baseMapper.selectHistoryMonthPage(monthPageParam, currentUserId, keyword,
                startDate, endDate);
        Page<WatchHistoryMonthVo> voPage = new Page<>(page, size);
        voPage.setTotal(monthResult.getTotal());
        List<LocalDate> months = monthResult.getRecords();
        if (months.isEmpty()) {
            return voPage;
        }

        // 第二步：查询这些月份的全部记录，按天分组（同一视频同一天仅保留最新一条）
        List<WatchHistoryVo> records = baseMapper.selectHistoryByMonths(currentUserId, keyword,
                startDate, endDate, months);
        Map<LocalDate, List<WatchHistoryVo>> dayGroupMap = new LinkedHashMap<>();
        Set<String> seen = new HashSet<>();
        for (WatchHistoryVo vo : records) {
            vo.setWatchedTime(formatSeconds(vo.getWatchedSeconds()));
            if (vo.getWatchTime() == null || !seen.add(vo.getWatchTime().toLocalDate() + "_" + vo.getVideoId())) {
                continue;
            }
            dayGroupMap.computeIfAbsent(vo.getWatchTime().toLocalDate(), k -> new ArrayList<>()).add(vo);
        }

        // 第三步：按月份组装（月份倒序、月内日期倒序，每个月的数据完整返回）
        List<WatchHistoryMonthVo> monthVos = new ArrayList<>();
        for (LocalDate monthFirstDay : months) {
            YearMonth monthKey = YearMonth.from(monthFirstDay);
            List<WatchHistoryGroupVo> days = new ArrayList<>();
            for (Map.Entry<LocalDate, List<WatchHistoryVo>> entry : dayGroupMap.entrySet()) {
                if (!YearMonth.from(entry.getKey()).equals(monthKey)) {
                    continue;
                }
                WatchHistoryGroupVo group = new WatchHistoryGroupVo();
                group.setDate(entry.getKey());
                group.setItems(entry.getValue());
                days.add(group);
            }
            WatchHistoryMonthVo monthVo = new WatchHistoryMonthVo();
            monthVo.setMonth(monthFirstDay.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            monthVo.setDays(days);
            monthVos.add(monthVo);
        }
        voPage.setRecords(monthVos);
        return voPage;
    }

    @Override
    public void deleteHistory(String videoUid, String episodeUid) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        if (StringUtils.isEmpty(videoUid)) {
            throw new GlobeException("视频avid不能为空");
        }
        // 按 avid 解析视频
        Video video = videoMapper.selectOne(new LambdaQueryWrapper<Video>()
                .eq(Video::getAvid, videoUid));
        if (video == null) {
            throw new GlobeException("视频不存在");
        }
        LambdaQueryWrapper<WatchHistory> wrapper = new LambdaQueryWrapper<WatchHistory>()
                .eq(WatchHistory::getUserId, currentUserId)
                .eq(WatchHistory::getVideoId, video.getId());
        if (!StringUtils.isEmpty(episodeUid)) {
            // 按 avid 解析剧集，并校验归属该视频
            VideoEpisode episode = videoEpisodeMapper.selectOne(new LambdaQueryWrapper<VideoEpisode>()
                    .eq(VideoEpisode::getAvid, episodeUid));
            if (episode == null || !Objects.equals(episode.getVideoId(), video.getId())) {
                throw new GlobeException("剧集不存在");
            }
            wrapper.eq(WatchHistory::getEpisodeId, episode.getId());
        }
        // 逻辑删除该用户对应该视频（剧集）的全部历史记录
        baseMapper.delete(wrapper);
    }

    @Override
    public void clearHistory() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        // 清空当前用户全部观看历史（逻辑删除）
        baseMapper.delete(new LambdaQueryWrapper<WatchHistory>()
                .eq(WatchHistory::getUserId, currentUserId));
    }

    @Override
    public List<DailyIncrementVo> getDailyPlayCount(LocalDate startDate, LocalDate endDate) {
        List<DailyIncrementVo> dbList = baseMapper.selectDailyPlayCount(startDate, endDate);
        Map<LocalDate, DailyIncrementVo> dateMap = dbList.stream()
                .collect(Collectors.toMap(DailyIncrementVo::getDate, v -> v));
        List<DailyIncrementVo> result = new ArrayList<>();
        for (LocalDate day = startDate; !day.isAfter(endDate); day = day.plusDays(1)) {
            DailyIncrementVo vo = dateMap.get(day);
            if (vo == null) {
                vo = new DailyIncrementVo();
                vo.setDate(day);
                vo.setPlayCount(0L);
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<DailyActiveVo> getDailyActiveUsers(LocalDate startDate, LocalDate endDate) {
        List<DailyActiveVo> dbList = baseMapper.selectDailyActiveUsers(startDate, endDate);
        Map<LocalDate, DailyActiveVo> dateMap = dbList.stream()
                .collect(Collectors.toMap(DailyActiveVo::getDate, v -> v));
        List<DailyActiveVo> result = new ArrayList<>();
        for (LocalDate day = startDate; !day.isAfter(endDate); day = day.plusDays(1)) {
            DailyActiveVo vo = dateMap.get(day);
            if (vo == null) {
                vo = new DailyActiveVo();
                vo.setDate(day);
                vo.setActiveCount(0L);
            }
            result.add(vo);
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
            return JWTUtil.getUserId(token);
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
