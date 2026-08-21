package com.cjym.videoweb.service;

import com.cjym.videoweb.model.dto.FavoriteFolderCreateDto;
import com.cjym.videoweb.model.dto.FavoriteFolderDeleteDto;
import com.cjym.videoweb.model.dto.FavoriteFolderUpdateDto;
import com.cjym.videoweb.model.dto.FavoritePublicFolderListDto;
import com.cjym.videoweb.model.entity.FavoriteFolder;
import com.cjym.videoweb.model.vo.FavoriteFolderVo;

import java.util.List;

public interface FavoriteFolderService {

    /**
     * 获取或创建指定用户的默认收藏夹（惰性创建）
     *
     * @param userId 用户ID
     * @return 默认收藏夹
     */
    FavoriteFolder getOrCreateDefault(Long userId);

    /**
     * 解析目标收藏夹：folderUid 为空时返回默认夹（不存在则创建），非空时按 avid 解析并校验归属
     *
     * @param userId    用户ID
     * @param folderUid 收藏夹前台标识（可为空）
     * @return 目标收藏夹
     */
    FavoriteFolder resolveTarget(Long userId, String folderUid);

    /**
     * 查询目标收藏夹：folderUid 为空时返回默认夹（不存在返回 null，不创建），非空时按 avid 解析并校验归属
     *
     * @param userId    用户ID
     * @param folderUid 收藏夹前台标识（可为空）
     * @return 目标收藏夹（默认夹不存在时为 null）
     */
    FavoriteFolder findTarget(Long userId, String folderUid);

    /**
     * 查询当前用户收藏夹列表（含各夹收藏数，默认夹排最前）
     *
     * @return 收藏夹列表
     */
    List<FavoriteFolderVo> list();

    /**
     * 查看指定用户的公开收藏夹列表（含各夹收藏数，默认夹排最前，私密夹不返回）
     *
     * @param dto 查询请求体，userUid 必填
     * @return 公开收藏夹列表
     */
    List<FavoriteFolderVo> listPublic(FavoritePublicFolderListDto dto);

    /**
     * 创建收藏夹
     *
     * @param dto 创建请求体，name 必填
     */
    void create(FavoriteFolderCreateDto dto);

    /**
     * 重命名收藏夹
     *
     * @param dto 更新请求体，folderUid、name 必填
     */
    void update(FavoriteFolderUpdateDto dto);

    /**
     * 删除收藏夹（默认夹不可删除；夹内收藏移回默认夹）
     *
     * @param dto 删除请求体，folderUid 必填
     */
    void delete(FavoriteFolderDeleteDto dto);
}
