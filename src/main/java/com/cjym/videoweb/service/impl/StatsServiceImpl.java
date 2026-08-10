package com.cjym.videoweb.service.impl;

import com.cjym.videoweb.mapper.CommentMapper;
import com.cjym.videoweb.mapper.LoginLogMapper;
import com.cjym.videoweb.mapper.VideoMapper;
import com.cjym.videoweb.mapper.WatchHistoryMapper;
import com.cjym.videoweb.mapper.CategoryMapper;
import com.cjym.videoweb.model.entity.Category;
import com.cjym.videoweb.model.vo.CategoryDailyIncrementVo;
import com.cjym.videoweb.model.vo.DailyActiveVo;
import com.cjym.videoweb.model.vo.DailyIncrementVo;
import com.cjym.videoweb.model.vo.OverviewStatsVo;
import com.cjym.videoweb.service.CommentService;
import com.cjym.videoweb.service.StatsService;
import com.cjym.videoweb.service.VideoService;
import com.cjym.videoweb.service.WatchHistoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsServiceImpl implements StatsService {

    @Resource
    private VideoService videoService;

    @Resource
    private CommentService commentService;

    @Resource
    private WatchHistoryService watchHistoryService;

    @Resource
    private WatchHistoryMapper watchHistoryMapper;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private LoginLogMapper loginLogMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Override
    public OverviewStatsVo getOverviewStats() {
        OverviewStatsVo vo = new OverviewStatsVo();
        vo.setTotalVideoCount(videoService.count());
        vo.setTotalPlayCount(watchHistoryService.count());
        vo.setTodayActiveUsers(watchHistoryMapper.selectTodayActiveUserCount());
        vo.setTotalCommentCount(commentMapper.selectTotalCommentCount());
        vo.setTodayNewVideos(videoMapper.selectTodayVideoCount());
        vo.setTodayPlayCount(watchHistoryMapper.selectTodayPlayCount());
        vo.setTodayNewComments(commentMapper.selectTodayCommentCount());
        vo.setTodayLoginCount(loginLogMapper.selectTodayLoginCount());
        return vo;
    }

    @Override
    public List<DailyIncrementVo> getDailyIncrement(LocalDate startDate, LocalDate endDate) {
        List<DailyIncrementVo> videoList = videoService.getDailyIncrement(startDate, endDate);
        List<DailyIncrementVo> commentList = commentService.getDailyIncrement(startDate, endDate);
        List<DailyIncrementVo> playList = watchHistoryService.getDailyPlayCount(startDate, endDate);

        Map<LocalDate, Long> commentMap = commentList.stream()
                .collect(Collectors.toMap(DailyIncrementVo::getDate, DailyIncrementVo::getCommentCount));

        Map<LocalDate, Long> playMap = playList.stream()
                .collect(Collectors.toMap(DailyIncrementVo::getDate, DailyIncrementVo::getPlayCount));

        List<DailyIncrementVo> result = new ArrayList<>();
        for (DailyIncrementVo videoVo : videoList) {
            DailyIncrementVo vo = new DailyIncrementVo();
            vo.setDate(videoVo.getDate());
            vo.setVideoCount(videoVo.getVideoCount() != null ? videoVo.getVideoCount() : 0L);
            vo.setCommentCount(commentMap.getOrDefault(videoVo.getDate(), 0L));
            vo.setPlayCount(playMap.getOrDefault(videoVo.getDate(), 0L));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<DailyActiveVo> getDailyActiveUsers(LocalDate startDate, LocalDate endDate) {
        return watchHistoryService.getDailyActiveUsers(startDate, endDate);
    }

    @Override
    public List<CategoryDailyIncrementVo> getCategoryDailyIncrement(LocalDate startDate, LocalDate endDate) {
        // 查询数据库中实际有数据的分类×日期记录
        List<CategoryDailyIncrementVo> dbList = videoMapper.selectCategoryDailyIncrement(startDate, endDate);

        // 构建 (categoryId + date) -> count 的快速查找Map
        Map<String, Long> dataMap = new LinkedHashMap<>();
        for (CategoryDailyIncrementVo vo : dbList) {
            String key = vo.getCategoryId() + "_" + vo.getDate();
            dataMap.put(key, vo.getVideoCount());
        }

        // 查询所有启用的分类（用于零填充）
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getIsShow, true)
                        .eq(Category::getStatus, true)
        );

        // 遍历所有分类×所有日期，零填充
        List<CategoryDailyIncrementVo> result = new ArrayList<>();
        for (Category cat : categories) {
            for (LocalDate day = startDate; !day.isAfter(endDate); day = day.plusDays(1)) {
                CategoryDailyIncrementVo vo = new CategoryDailyIncrementVo();
                vo.setCategoryId(cat.getId());
                vo.setCategoryName(cat.getName());
                vo.setDate(day);
                String key = cat.getId() + "_" + day;
                vo.setVideoCount(dataMap.getOrDefault(key, 0L));
                result.add(vo);
            }
        }
        return result;
    }
}
