package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.constant.VideoConstant;
import com.cjym.videoweb.mapper.UserInfoMapper;
import com.cjym.videoweb.mapper.VideoMapper;
import com.cjym.videoweb.model.vo.ClientVideoVo;
import com.cjym.videoweb.model.vo.UpUserVo;
import com.cjym.videoweb.service.SearchAdapter;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 数据库搜索适配器：聚合搜索的数据库实现（始终注册），
 * ES 未启用或不可用时由 SearchServiceFacade 降级到此实现
 */
@Service
public class DatabaseSearchAdapter implements SearchAdapter {

    /**
     * 视频搜索排序类型 → ORDER BY 列 映射（latest:按发布时间 hot:按播放量 favorite:按收藏数）
     */
    private static final Map<String, String> SEARCH_SORT_COLUMN_MAP = Map.of(
            VideoConstant.SORT_LATEST, "v.create_time",
            VideoConstant.SORT_HOT, "play_count",
            VideoConstant.SORT_FAVORITE, "favorite_count"
    );

    /**
     * UP主搜索排序类型 → ORDER BY 列 映射（fans:按粉丝数 like:按累计获赞 play:按累计播放）
     */
    private static final Map<String, String> UP_SEARCH_SORT_COLUMN_MAP = Map.of(
            "fans", "fans_count",
            "like", "total_like_count",
            "play", "total_play_count"
    );

    /**
     * 前台视频列表排序类型 → ORDER BY 列 映射（latest:按发布时间 hot:按播放量）
     */
    private static final Map<String, String> CLIENT_SORT_COLUMN_MAP = Map.of(
            VideoConstant.SORT_LATEST, "v.create_time",
            VideoConstant.SORT_HOT, "play_count"
    );

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public IPage<UpUserVo> searchUp(String keyword, Integer page, Integer size, String sortType) {
        int pageNum = page != null ? page : 1;
        int pageSize = size != null ? size : 10;
        // 排序白名单解析：非法值返回 null（走默认粉丝数降序）
        String orderColumn = UP_SEARCH_SORT_COLUMN_MAP.get(
                sortType == null || sortType.isBlank() ? "fans" : sortType.trim().toLowerCase());
        String trimKeyword = StringUtils.isNotEmpty(keyword) ? keyword.trim() : null;
        return userInfoMapper.selectSearchUpPage(new Page<>(pageNum, pageSize), trimKeyword, orderColumn);
    }

    @Override
    public IPage<ClientVideoVo> searchVideo(String keyword, Integer page, Integer size, String sortType) {
        int pageNum = page != null ? page : 1;
        int pageSize = size != null ? size : 10;
        // 排序白名单解析：非法值降级为最新排序
        String key = sortType == null || sortType.isBlank()
                ? VideoConstant.SORT_LATEST
                : sortType.trim().toLowerCase();
        String orderColumn = SEARCH_SORT_COLUMN_MAP.getOrDefault(key, SEARCH_SORT_COLUMN_MAP.get(VideoConstant.SORT_LATEST));
        String trimKeyword = StringUtils.isNotEmpty(keyword) ? keyword.trim() : null;
        return videoMapper.selectSearchVideoPage(new Page<>(pageNum, pageSize), trimKeyword, orderColumn);
    }

    @Override
    public IPage<ClientVideoVo> listVideo(Integer page, Integer size, String sortType, Long categoryId) {
        int pageNum = page != null ? page : 1;
        int pageSize = size != null ? size : 10;
        // 排序白名单解析：非法值降级为最新排序
        String key = sortType == null || sortType.isBlank()
                ? VideoConstant.SORT_LATEST
                : sortType.trim().toLowerCase();
        String orderColumn = CLIENT_SORT_COLUMN_MAP.getOrDefault(key, CLIENT_SORT_COLUMN_MAP.get(VideoConstant.SORT_LATEST));
        return videoMapper.selectClientVideoPage(new Page<>(pageNum, pageSize), orderColumn, categoryId, null);
    }

    @Override
    public IPage<ClientVideoVo> listUserVideos(Long userId, Integer page, Integer size, String sortType) {
        int pageNum = page != null ? page : 1;
        int pageSize = size != null ? size : 10;
        // 排序白名单解析：非法值降级为最新排序
        String key = sortType == null || sortType.isBlank()
                ? VideoConstant.SORT_LATEST
                : sortType.trim().toLowerCase();
        String orderColumn = CLIENT_SORT_COLUMN_MAP.getOrDefault(key, CLIENT_SORT_COLUMN_MAP.get(VideoConstant.SORT_LATEST));
        return videoMapper.selectClientVideoPage(new Page<>(pageNum, pageSize), orderColumn, null, userId);
    }

    @Override
    public List<ClientVideoVo> recommendByInterests(List<Long> categoryIds, List<String> tags,
                                                    List<Long> excludeIds, int limit) {
        return videoMapper.selectRecommendByInterests(categoryIds, tags, excludeIds, limit);
    }
}
