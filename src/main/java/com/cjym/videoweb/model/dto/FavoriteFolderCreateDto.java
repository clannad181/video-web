package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "创建收藏夹请求")
public class FavoriteFolderCreateDto {

    @Schema(description = "收藏夹名称（必填，最长20字）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "是否私密（可选，不传默认公开）")
    private Boolean isPrivate;

    @Schema(description = "收藏夹描述（可选，最长200字）")
    private String description;

    @Schema(description = "收藏夹图片（可选）")
    private String coverUrl;
}
