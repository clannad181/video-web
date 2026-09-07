package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.constant.UserProfileConstant;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.CategoryMapper;
import com.cjym.videoweb.mapper.UserProfileMapper;
import com.cjym.videoweb.mapper.VideoMapper;
import com.cjym.videoweb.mapper.WatchHistoryMapper;
import com.cjym.videoweb.model.entity.Category;
import com.cjym.videoweb.model.entity.UserProfile;
import com.cjym.videoweb.model.entity.Video;
import com.cjym.videoweb.model.vo.WatchStatsVo;
import com.cjym.videoweb.service.UserProfileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户画像 Service 实现
 * <p>
 * 算法：取最近 {@link UserProfileConstant#PROFILE_WINDOW_DAYS} 天的观看历史，按视频聚合后，
 * 每个视频对标签/分类的兴趣贡献 = 观看时长占比 × 时间衰减（半衰期 {@link UserProfileConstant#HALF_LIFE_DAYS} 天），
 * 累加得到各标签/分类的兴趣得分，覆盖写入 user_profile 表。
 */
@Service
@Slf4j
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile> implements UserProfileService {

    @Resource
    private WatchHistoryMapper watchHistoryMapper;

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rebuildProfile(Long userId) {
        if (userId == null) {
            throw new GlobeException("用户ID不能为空");
        }

        LocalDateTime since = LocalDateTime.now().minusDays(UserProfileConstant.PROFILE_WINDOW_DAYS);
        List<WatchStatsVo> watchStats = watchHistoryMapper.selectProfileWatchStats(userId, since);

        // 1. 物理清除旧画像（画像为派生数据，全量重建覆盖）
        baseMapper.deleteByUserId(userId);
        if (watchStats == null || watchStats.isEmpty()) {
            log.info("用户画像重建完成[userId={},条数=0]（时间窗内无观看记录）", userId);
            return;
        }

        // 2. 批量加载视频信息与分类名称映射
        Set<Long> videoIds = watchStats.stream()
                .map(WatchStatsVo::getVideoId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Video> videoMap = videoIds.isEmpty() ? Collections.emptyMap()
                : videoMapper.selectBatchIds(videoIds).stream()
                        .collect(Collectors.toMap(Video::getId, v -> v));
        Map<Long, String> categoryNameMap = loadCategoryNames(videoMap.values());

        // 3. 聚合计算标签与分类的兴趣得分
        List<UserProfile> profiles = computeInterests(userId, watchStats, videoMap, categoryNameMap);

        // 4. 写入画像
        for (UserProfile profile : profiles) {
            baseMapper.insert(profile);
        }
        log.info("用户画像重建完成[userId={},条数={}]", userId, profiles.size());
    }

    @Override
    public List<UserProfile> listInterests(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return lambdaQuery().eq(UserProfile::getUserId, userId)
                .orderByDesc(UserProfile::getScore)
                .list();
    }

    /**
     * 聚合计算兴趣得分：按视频累加「时长占比×时间衰减」到标签与分类维度
     */
    private List<UserProfile> computeInterests(Long userId, List<WatchStatsVo> watchStats,
                                               Map<Long, Video> videoMap,
                                               Map<Long, String> categoryNameMap) {
        LocalDateTime now = LocalDateTime.now();
        Map<String, Accumulator> tagAccumulatorMap = new HashMap<>();
        Map<Long, Accumulator> categoryAccumulatorMap = new HashMap<>();
        for (WatchStatsVo stat : watchStats) {
            Video video = videoMap.get(stat.getVideoId());
            if (video == null) {
                continue;
            }
            double contribution = computeContribution(stat, video, now);
            if (contribution <= 0) {
                continue;
            }
            // 标签维度：视频标签为逗号分隔字符串
            for (String tag : splitTags(video.getTags())) {
                tagAccumulatorMap.computeIfAbsent(tag, k -> new Accumulator())
                        .accumulate(contribution, stat);
            }
            // 分类维度：仅统计名称可解析的分类
            if (video.getCategoryId() != null && categoryNameMap.containsKey(video.getCategoryId())) {
                categoryAccumulatorMap.computeIfAbsent(video.getCategoryId(), k -> new Accumulator())
                        .accumulate(contribution, stat);
            }
        }
        List<UserProfile> profiles = new ArrayList<>();
        tagAccumulatorMap.forEach((tag, acc) ->
                profiles.add(buildProfile(userId, UserProfileConstant.ITEM_TYPE_TAG, null, tag, acc)));
        categoryAccumulatorMap.forEach((categoryId, acc) ->
                profiles.add(buildProfile(userId, UserProfileConstant.ITEM_TYPE_CATEGORY, categoryId,
                        categoryNameMap.get(categoryId), acc)));
        return profiles;
    }

    /**
     * 计算单条观看统计对兴趣的贡献值：观看时长占比 × 时间衰减
     */
    private double computeContribution(WatchStatsVo stat, Video video, LocalDateTime now) {
        // 1. 观看时长占比（视频总时长缺失时按完整观看计）
        double ratio = 1.0;
        long watchedSeconds = stat.getTotalSeconds() == null ? 0L : stat.getTotalSeconds();
        if (video.getTotalDuration() != null && video.getTotalDuration() > 0) {
            ratio = Math.min(watchedSeconds, video.getTotalDuration()) / (double) video.getTotalDuration();
        }
        // 2. 时间衰减：每过半衰期权重减半，越久远的观看贡献越低
        long daysAgo = stat.getLastWatchTime() == null ? 0L
                : Duration.between(stat.getLastWatchTime(), now).toDays();
        double decay = Math.pow(0.5, daysAgo / (double) UserProfileConstant.HALF_LIFE_DAYS);
        return ratio * decay;
    }

    /**
     * 解析视频分类名称映射（分类ID → 分类名）
     */
    private Map<Long, String> loadCategoryNames(java.util.Collection<Video> videos) {
        Set<Long> categoryIds = videos.stream()
                .map(Video::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (categoryIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));
    }

    /**
     * 逗号分隔标签字符串 → 标签列表（去空白、去空项）
     */
    private List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 聚合结果 → 画像实体
     */
    private UserProfile buildProfile(Long userId, String itemType, Long itemId, String itemName,
                                     Accumulator acc) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setItemType(itemType);
        profile.setItemId(itemId);
        profile.setItemName(itemName);
        profile.setScore(BigDecimal.valueOf(acc.score).setScale(2, RoundingMode.HALF_UP));
        profile.setWatchCount(acc.watchCount);
        profile.setTotalWatchSeconds(acc.totalWatchSeconds);
        profile.setLastWatchTime(acc.lastWatchTime);
        return profile;
    }

    /**
     * 兴趣聚合累加器（标签/分类维度的中间统计）
     */
    private static class Accumulator {
        private double score;
        private int watchCount;
        private long totalWatchSeconds;
        private LocalDateTime lastWatchTime;

        void accumulate(double contribution, WatchStatsVo stat) {
            this.score += contribution;
            this.watchCount++;
            this.totalWatchSeconds += stat.getTotalSeconds() == null ? 0L : stat.getTotalSeconds();
            if (lastWatchTime == null
                    || (stat.getLastWatchTime() != null && stat.getLastWatchTime().isAfter(lastWatchTime))) {
                this.lastWatchTime = stat.getLastWatchTime();
            }
        }
    }
}
