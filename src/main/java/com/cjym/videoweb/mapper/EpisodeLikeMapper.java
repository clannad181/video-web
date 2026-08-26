package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjym.videoweb.model.entity.EpisodeLike;
import com.cjym.videoweb.model.vo.RecentActionEpisodeVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EpisodeLikeMapper extends BaseMapper<EpisodeLike> {

    /**
     * 查询用户最近点赞的剧集列表（每个视频仅保留最近点赞的一集，按点赞时间倒序）
     */
    List<RecentActionEpisodeVo> selectRecentLikeEpisodes(@Param("userId") Long userId,
                                                          @Param("limit") int limit);
}
