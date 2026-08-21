package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "删除收藏夹请求")
public class FavoriteFolderDeleteDto {

    @Schema(description = "收藏夹前台标识（必填）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String folderUid;
}
