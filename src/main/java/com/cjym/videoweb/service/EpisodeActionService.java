package com.cjym.videoweb.service;

import com.cjym.videoweb.model.vo.EpisodeActionStatusVo;
import com.cjym.videoweb.model.vo.EpisodeActionVo;
import com.cjym.videoweb.model.vo.EpisodeFavoriteStatusVo;

public interface EpisodeActionService {

    /**
     * 点赞/取消点赞剧集（toggle逻辑）
     *
     * @param episodeUid 剧集avid
     * @return 操作结果（最新点赞数和当前用户点赞状态）
     */
    EpisodeActionVo toggleLike(String episodeUid);

    /**
     * 投币（每个用户每集只能投币一次，不可取消）
     *
     * @param episodeUid 剧集avid
     * @return 操作结果（最新投币数和当前用户投币状态）
     */
    EpisodeActionVo coin(String episodeUid);

    /**
     * 收藏/取消收藏剧集（toggle逻辑）
     *
     * @param episodeUid 剧集avid
     * @return 操作结果（最新收藏数和当前用户收藏状态）
     */
    EpisodeActionVo toggleFavorite(String episodeUid);

    /**
     * 查询当前用户对指定剧集的点赞和投币状态
     *
     * @param episodeUid 剧集avid
     * @return 点赞/投币状态
     */
    EpisodeActionStatusVo getActionStatus(String episodeUid);

    /**
     * 查询当前用户对指定剧集的收藏状态
     *
     * @param episodeUid 剧集avid
     * @return 收藏状态
     */
    EpisodeFavoriteStatusVo getFavoriteStatus(String episodeUid);

    /**
     * 查询当前用户对指定剧集的点赞状态
     *
     * @param episodeId 剧集主键ID
     * @return true-已点赞，false-未点赞
     */
    boolean isLiked(Long episodeId);

    /**
     * 查询当前用户对指定剧集的投币状态
     *
     * @param episodeId 剧集主键ID
     * @return true-已投币，false-未投币
     */
    boolean isCoined(Long episodeId);

    /**
     * 查询当前用户对指定剧集的收藏状态
     *
     * @param episodeId 剧集主键ID
     * @return true-已收藏，false-未收藏
     */
    boolean isFavorited(Long episodeId);
}
