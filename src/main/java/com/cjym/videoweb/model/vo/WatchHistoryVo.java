package com.cjym.videoweb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "观看历史记录视图")
public class WatchHistoryVo {

    @Schema(description = "记录id")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "视频ID")
    private Long videoId;

    @Schema(description = "视频avid")
    private String videoAvid;

    @Schema(description = "视频标题")
    private String videoTitle;

    @Schema(description = "视频封面URL")
    private String videoCover;

    @Schema(description = "剧集ID")
    private Long episodeId;

    @Schema(description = "剧集avid")
    private String episodeAvid;

    @Schema(description = "剧集标题")
    private String episodeTitle;

    @Schema(description = "集数序号")
    private Integer episodeNumber;

    @Schema(description = "已观看秒数")
    private Long watchedSeconds;

    @Schema(description = "已观看时长（格式化，如 12:35）")
    private String watchedTime;

    @Schema(description = "播放跳转链接（如 /admin/video/avXxx/episode/avYyy）")
    private String playLink;

    @Schema(description = "最近观看时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime watchTime;
}
