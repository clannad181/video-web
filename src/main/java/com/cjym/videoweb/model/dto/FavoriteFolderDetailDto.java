package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "查看收藏夹详情请求")
public class FavoriteFolderDetailDto {

    @Schema(description = "收藏夹前台标识（必填）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String folderUid;

    @Schema(description = "页码，从1开始（默认1）")
    private Integer page;

    @Schema(description = "每页数量（默认10）")
    private Integer size;
}
