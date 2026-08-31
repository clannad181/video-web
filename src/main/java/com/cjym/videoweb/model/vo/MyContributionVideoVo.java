package com.cjym.videoweb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "我的投稿视频记录")
public class MyContributionVideoVo {

    @Schema(description = "视频id")
    private Long id;

    @Schema(description = "视频avid")
    private String avid;

    @Schema(description = "视频标题")
    private String title;

    @Schema(description = "封面图URL")
    private String coverUrl;

    @Schema(description = "视频状态：0-草稿，1-待发布，2-已发布，3-已下架")
    private Integer videoStatus;

    @Schema(description = "总集数")
    private Integer episodeCount;

    @Schema(description = "播放数（多集累加）")
    private Long playCount;

    @Schema(description = "收藏数（多集累加）")
    private Long favoriteCount;

    @Schema(description = "最新更新剧集avid（审核通过剧集中集数序号最大的一集）")
    private String episodeAvid;

    @Schema(description = "播放跳转链接（如 /client/video/avXxx/episode/avYyy），无审核通过剧集时为空")
    private String playLink;

    @Schema(description = "投稿时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
