package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.model.entity.EpisodeFavorite;
import com.cjym.videoweb.model.vo.FavoriteEpisodeVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EpisodeFavoriteMapper extends BaseMapper<EpisodeFavorite> {

    /**
     * 分页搜索用户收藏（按剧集去重，取最新收藏时间），标题关键字为空时查询全部
     *
     * @param page     分页参数
     * @param userId   用户ID
     * @param folderId 收藏夹ID（可空，为空时搜索全部收藏夹）
     * @param keyword  标题模糊关键词（可空）
     * @return 收藏剧集分页列表（按收藏时间倒序）
     */
    IPage<FavoriteEpisodeVo> selectFavoriteSearchPage(Page<FavoriteEpisodeVo> page,
                                                       @Param("userId") Long userId,
                                                       @Param("folderId") Long folderId,
                                                       @Param("keyword") String keyword);
}
