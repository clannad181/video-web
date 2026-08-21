package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "从文件夹移除收藏请求")
public class FavoriteRemoveDto {

    @Schema(description = "被取消收藏剧集avid（必填）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String episodeUid;

    @Schema(description = "收藏夹前台标识（可选；不传表示取消该剧集在所有收藏夹中的收藏，传了则仅从该夹移除）")
    private String folderUid;
}
