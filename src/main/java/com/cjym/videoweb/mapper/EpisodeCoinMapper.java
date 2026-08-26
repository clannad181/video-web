package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjym.videoweb.model.entity.EpisodeCoin;
import com.cjym.videoweb.model.vo.RecentActionEpisodeVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EpisodeCoinMapper extends BaseMapper<EpisodeCoin> {

    /**
     * 查询用户最近投币的剧集列表（每个视频仅保留最近投币的一集，按投币时间倒序）
     */
    List<RecentActionEpisodeVo> selectRecentCoinEpisodes(@Param("userId") Long userId,
                                                          @Param("limit") int limit);
}
