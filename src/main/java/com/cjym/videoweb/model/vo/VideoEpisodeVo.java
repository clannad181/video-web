package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "剧集信息")
public class VideoEpisodeVo {

    @Schema(description = "剧集id")
    private Long id;

    @Schema(description = "集数序号")
    private Integer episodeNumber;

    @Schema(description = "剧集标题")
    private String title;

    @Schema(description = "剧集时长（秒）")
    private Long duration;

    @Schema(description = "播放地址")
    private String playUrl;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "审核状态：0-待审核，1-审核通过，2-审核拒绝")
    private Integer reviewStatus;

    @Schema(description = "播放数")
    private Long playCount;

    @Schema(description = "收藏数")
    private Long favoriteCount;

    @Schema(description = "点赞数")
    private Long likeCount;

    @Schema(description = "投币数")
    private Long coinCount;

    @Schema(description = "观看人数")
    private Long viewerCount;
}
