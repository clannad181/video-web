package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收藏搜索结果记录（含UP主完整公开信息）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "收藏搜索结果记录")
public class FavoriteSearchVo extends FavoriteEpisodeVo {

    @Schema(description = "UP主完整公开信息（含关注数/粉丝数/累计获赞/累计播放）")
    private UpUserVo upUser;
}
