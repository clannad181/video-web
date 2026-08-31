package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "搜索收藏内容请求")
public class FavoriteSearchDto {

    @Schema(description = "收藏夹前台标识（可选，不传表示在所有收藏夹中搜索）")
    private String folderUid;

    @Schema(description = "模糊关键词（可选，匹配剧集标题/视频标题/UP主昵称/UP主uid，不传表示查询全部收藏）")
    private String title;

    @Schema(description = "页码，从1开始（默认1）")
    private Integer page;

    @Schema(description = "每页数量（默认10）")
    private Integer size;
}
