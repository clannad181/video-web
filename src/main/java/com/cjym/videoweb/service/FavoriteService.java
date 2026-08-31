package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.FavoriteAddDto;
import com.cjym.videoweb.model.dto.FavoriteFolderDetailDto;
import com.cjym.videoweb.model.dto.FavoriteListDto;
import com.cjym.videoweb.model.dto.FavoritePublicListDto;
import com.cjym.videoweb.model.dto.FavoriteRemoveDto;
import com.cjym.videoweb.model.dto.FavoriteSearchDto;
import com.cjym.videoweb.model.vo.FavoriteEpisodeVo;
import com.cjym.videoweb.model.vo.FavoriteFolderDetailVo;
import com.cjym.videoweb.model.vo.FavoriteSearchVo;

public interface FavoriteService {

    /**
     * 收藏剧集到指定文件夹（folderUid 为空时收藏到默认夹）
     *
     * @param dto 收藏请求体，episodeUid 必填
     */
    void add(FavoriteAddDto dto);

    /**
     * 从指定文件夹移除收藏（folderUid 为空时从默认夹移除）
     *
     * @param dto 移除请求体，episodeUid 必填
     */
    void remove(FavoriteRemoveDto dto);

    /**
     * 分页查询文件夹内的收藏列表（folderUid 为空时查询默认夹）
     *
     * @param dto 查询请求体，page/size 缺省为 1/10
     * @return 收藏剧集分页列表
     */
    IPage<FavoriteEpisodeVo> pageList(FavoriteListDto dto);

    /**
     * 分页搜索当前用户的收藏（按剧集去重，取最新收藏记录）
     *
     * <p>folderUid 为空时在全部收藏夹中搜索，否则仅搜索指定收藏夹；title 为空时返回全部，否则模糊匹配剧集标题、视频标题、UP主昵称与UP主uid；返回结果含UP主完整公开信息</p>
     *
     * @param dto 搜索请求体，page/size 缺省为 1/10
     * @return 收藏剧集分页列表（按收藏时间倒序）
     */
    IPage<FavoriteSearchVo> pageSearch(FavoriteSearchDto dto);

    /**
     * 分页查询他人公开收藏夹内的收藏列表（私密夹不可见）
     *
     * @param dto 查询请求体，userUid、folderUid 必填，page/size 缺省为 1/10
     * @return 收藏剧集分页列表
     */
    IPage<FavoriteEpisodeVo> pagePublicList(FavoritePublicListDto dto);

    /**
     * 按收藏夹标识查看收藏夹详情（含夹基本信息和收藏分页列表）
     *
     * <p>本人可查看自己的私密夹；他人私密夹统一返回「收藏夹不存在」；游客可访问公开夹</p>
     *
     * @param dto 查询请求体，folderUid 必填，page/size 缺省为 1/10
     * @return 收藏夹详情
     */
    FavoriteFolderDetailVo pageDetail(FavoriteFolderDetailDto dto);
}
