package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.model.entity.EpisodeFavorite;
import com.cjym.videoweb.model.vo.FavoriteSearchVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EpisodeFavoriteMapper extends BaseMapper<EpisodeFavorite> {

    /**
     * 分页搜索用户收藏（按剧集去重，取最新收藏时间），关键字为空时查询全部
     *
     * <p>关键字同时模糊匹配剧集标题、视频标题、UP主昵称与UP主uid，返回结果含UP主完整公开信息</p>
     *
     * @param page     分页参数
     * @param userId   用户ID
     * @param folderId 收藏夹ID（可空，为空时搜索全部收藏夹）
     * @param keyword  模糊关键词（可空）
     * @return 收藏剧集分页列表（按收藏时间倒序）
     */
    IPage<FavoriteSearchVo> selectFavoriteSearchPage(Page<FavoriteSearchVo> page,
                                                      @Param("userId") Long userId,
                                                      @Param("folderId") Long folderId,
                                                      @Param("keyword") String keyword);
}
