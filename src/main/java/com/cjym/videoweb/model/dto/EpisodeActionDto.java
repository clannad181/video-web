package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "剧集操作请求（点赞/投币）")
public class EpisodeActionDto {

    @Schema(description = "剧集avid（如 avXyz789）")
    private String episodeUid;
}
