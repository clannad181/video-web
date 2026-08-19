package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "剧集互动状态（点赞/投币）")
public class EpisodeActionStatusVo {

    @Schema(description = "是否已点赞")
    private Boolean isLiked;

    @Schema(description = "是否已投币")
    private Boolean isCoined;
}
