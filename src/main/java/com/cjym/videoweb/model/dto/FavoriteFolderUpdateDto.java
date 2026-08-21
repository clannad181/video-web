package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "重命名收藏夹请求")
public class FavoriteFolderUpdateDto {

    @Schema(description = "收藏夹前台标识（必填）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String folderUid;

    @Schema(description = "新名称（可选，最长20字；不传则不修改名称）")
    private String name;

    @Schema(description = "是否私密（可选；不传则不修改可见性）")
    private Boolean isPrivate;
}
