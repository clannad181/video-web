package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "客户端剧集分页查询请求")
public class EpisodePageDto {

    @Schema(description = "视频avid")
    private String videoUid;

    @Schema(description = "页码，默认1")
    private Integer page;

    @Schema(description = "每页条数，默认10")
    private Integer size;

    @Schema(description = "剧集标题模糊搜索")
    private String title;

    @Schema(description = "审核状态筛选：0-待审核，1-审核通过，2-审核拒绝")
    private Integer reviewStatus;
}
