package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 首页视频推荐
 */
@Data
@Schema(description = "首页视频推荐（banner + 卡片两部分）")
public class RecommendVo {

    @Schema(description = "Banner展示视频（6条，已登录且有画像时按画像兴趣推荐，无画像或未登录时返回最多收藏的视频）")
    private List<ClientVideoVo> banner;

    @Schema(description = "卡片展示视频（6条，已登录返回关注UP主最新视频，未登录返回最新发布视频，不足时用最多收藏的视频补齐）")
    private List<ClientVideoVo> cards;
}
