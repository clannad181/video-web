package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分页查询收藏列表请求")
public class FavoriteListDto {

    @Schema(description = "收藏夹前台标识（可选，不传表示默认收藏夹）")
    private String folderUid;

    @Schema(description = "页码，从1开始（默认1）")
    private Integer page;

    @Schema(description = "每页数量（默认10）")
    private Integer size;
}
