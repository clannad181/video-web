package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "总览统计")
public class OverviewStatsVo {

    @Schema(description = "视频总数")
    private Long totalVideoCount;

    @Schema(description = "总播放量")
    private Long totalPlayCount;

    @Schema(description = "今日活跃用户数")
    private Long todayActiveUsers;

    @Schema(description = "评论总数")
    private Long totalCommentCount;

    @Schema(description = "今日上传视频数")
    private Long todayNewVideos;

    @Schema(description = "今日总播放量")
    private Long todayPlayCount;

    @Schema(description = "今日新增评论数")
    private Long todayNewComments;

    @Schema(description = "今日登录人数")
    private Long todayLoginCount;
}
