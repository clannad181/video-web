package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "剧集操作结果（点赞/投币）")
public class EpisodeActionVo {

    @Schema(description = "操作后最新数量（点赞数/投币数）")
    private Long count;

    @Schema(description = "当前用户是否已执行该操作（true-已点赞/已投币，false-未点赞/未投币）")
    private Boolean isActioned;
}
