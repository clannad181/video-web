package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "剧集收藏状态")
public class EpisodeFavoriteStatusVo {

    @Schema(description = "是否已收藏")
    private Boolean isFavorited;

    @Schema(description = "当前用户已收藏该剧集的收藏夹前台标识列表（未收藏时为空）")
    private List<String> folderUids;
}
