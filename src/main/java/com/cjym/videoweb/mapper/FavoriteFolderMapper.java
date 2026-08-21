package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjym.videoweb.model.entity.FavoriteFolder;
import com.cjym.videoweb.model.vo.FavoriteFolderVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FavoriteFolderMapper extends BaseMapper<FavoriteFolder> {

    /**
     * 查询用户收藏夹列表（含各夹收藏数），默认夹排最前
     *
     * @param userId     用户ID
     * @param publicOnly 为 true 时仅返回公开收藏夹，false/null 返回全部
     * @return 收藏夹列表
     */
    List<FavoriteFolderVo> selectFolderListWithCount(@Param("userId") Long userId, @Param("publicOnly") Boolean publicOnly);
}
