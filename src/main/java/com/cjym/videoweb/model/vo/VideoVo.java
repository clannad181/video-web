package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "视频详情")
public class VideoVo {

    @Schema(description = "视频id")
    private Long id;

    @Schema(description = "视频标题")
    private String title;

    @Schema(description = "视频简介")
    private String description;

    @Schema(description = "分类id")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "封面图URL")
    private String coverUrl;

    @Schema(description = "总时长（秒）")
    private Long totalDuration;

    @Schema(description = "总集数")
    private Integer episodeCount;

    @Schema(description = "播放数")
    private Long playCount;

    @Schema(description = "收藏数")
    private Long favoriteCount;

    @Schema(description = "点赞数")
    private Long likeCount;

    @Schema(description = "投币数")
    private Long coinCount;

    @Schema(description = "观看人数")
    private Long viewerCount;

    @Schema(description = "UP主用户id")
    private Long userId;

    @Schema(description = "UP主用户名")
    private String userName;

    @Schema(description = "UP主昵称")
    private String nickName;

    @Schema(description = "审核状态：0-待审核，1-审核通过，2-审核拒绝")
    private Integer reviewStatus;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "剧集列表")
    private List<VideoEpisodeVo> episodes;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
