package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "剧集收藏状态")
public class EpisodeFavoriteStatusVo {

    @Schema(description = "是否已收藏")
    private Boolean isFavorited;
}
