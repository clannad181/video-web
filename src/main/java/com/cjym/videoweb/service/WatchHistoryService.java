package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.dto.WatchHistorySaveDto;
import com.cjym.videoweb.model.entity.WatchHistory;
import com.cjym.videoweb.model.vo.WatchHistoryVo;

/**
 * 观看历史记录 Service
 */
public interface WatchHistoryService extends IService<WatchHistory> {

    /**
     * 记录或更新观看历史（同一用户+视频+剧集存在则更新进度）
     */
    void record(WatchHistorySaveDto dto);

    /**
     * 分页查询当前用户的观看历史
     */
    IPage<WatchHistoryVo> pageHistory(Integer page, Integer size);
}
