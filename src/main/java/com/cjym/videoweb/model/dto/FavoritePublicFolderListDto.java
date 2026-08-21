package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "查看他人公开收藏夹列表请求")
public class FavoritePublicFolderListDto {

    @Schema(description = "目标用户uid（必填）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userUid;
}
