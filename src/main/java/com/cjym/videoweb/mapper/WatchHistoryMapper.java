package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.model.entity.WatchHistory;
import com.cjym.videoweb.model.vo.WatchHistoryVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 观看历史记录 Mapper
 */
@Mapper
public interface WatchHistoryMapper extends BaseMapper<WatchHistory> {

    /**
     * 分页查询用户的观看历史（关联视频和剧集信息）
     */
    IPage<WatchHistoryVo> selectWatchHistoryPage(Page<WatchHistoryVo> page,
                                                  @Param("userId") Long userId);
}
