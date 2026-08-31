package com.cjym.videoweb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "最近互动（点赞/投币）剧集记录")
public class RecentActionEpisodeVo {

    @Schema(description = "视频avid")
    private String videoAvid;

    @Schema(description = "视频标题")
    private String videoTitle;

    @Schema(description = "视频封面URL")
    private String videoCover;

    @Schema(description = "剧集avid")
    private String episodeAvid;

    @Schema(description = "剧集标题")
    private String episodeTitle;

    @Schema(description = "集数序号")
    private Integer episodeNumber;

    @Schema(description = "剧集点赞数")
    private Long likeCount;

    @Schema(description = "剧集收藏数")
    private Long favoriteCount;

    @Schema(description = "剧集播放数")
    private Long playCount;

    @Schema(description = "UP主昵称")
    private String nickName;

    @Schema(description = "UP主头像URL")
    private String avatarUrl;

    @Schema(description = "播放跳转链接（如 /client/video/avXxx/episode/avYyy）")
    private String playLink;

    @Schema(description = "操作时间（点赞/投币时间）")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actionTime;
}
