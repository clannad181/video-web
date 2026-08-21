package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "收藏剧集到文件夹请求")
public class FavoriteAddDto {

    @Schema(description = "被收藏剧集avid（必填）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String episodeUid;

    @Schema(description = "目标收藏夹前台标识（可选，不传表示默认收藏夹）")
    private String folderUid;
}
