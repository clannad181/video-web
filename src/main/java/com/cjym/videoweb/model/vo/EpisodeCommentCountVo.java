package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 剧集评论数统计
 */
@Data
@Schema(description = "剧集评论数统计")
public class EpisodeCommentCountVo {

    @Schema(description = "剧集ID")
    private Long episodeId;

    @Schema(description = "评论总数（前台可见，排除运营屏蔽）")
    private Long commentCount;
}
