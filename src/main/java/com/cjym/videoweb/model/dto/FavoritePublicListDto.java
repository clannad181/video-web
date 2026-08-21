package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分页查询他人公开收藏夹内容请求")
public class FavoritePublicListDto {

    @Schema(description = "目标用户uid（必填）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userUid;

    @Schema(description = "收藏夹前台标识（必填，仅限该用户的公开收藏夹）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String folderUid;

    @Schema(description = "页码，从1开始（默认1）")
    private Integer page;

    @Schema(description = "每页数量（默认10）")
    private Integer size;
}
