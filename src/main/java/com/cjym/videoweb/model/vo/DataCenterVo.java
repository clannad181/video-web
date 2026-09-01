package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "前台个人数据中心统计")
public class DataCenterVo {

    @Schema(description = "总观看数（自己所有投稿剧集播放数之和）")
    private Long totalPlayCount;

    @Schema(description = "总收藏数（自己所有投稿剧集收藏数之和）")
    private Long totalFavoriteCount;

    @Schema(description = "总投币数（自己所有投稿剧集投币数之和）")
    private Long totalCoinCount;

    @Schema(description = "总点赞数（自己所有投稿剧集点赞数之和）")
    private Long totalLikeCount;

    @Schema(description = "最热视频TOP5（观看数最多）")
    private List<ClientVideoVo> hotVideos;

    @Schema(description = "最受欢迎视频TOP5（收藏数最多）")
    private List<ClientVideoVo> favoriteVideos;
}
