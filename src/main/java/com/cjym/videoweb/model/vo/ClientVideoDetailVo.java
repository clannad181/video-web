package com.cjym.videoweb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 前台视频播放详情（客户端）
 */
@Data
@Schema(description = "前台视频播放详情")
public class ClientVideoDetailVo {

    @Schema(description = "视频id")
    private Long id;

    @Schema(description = "视频avid")
    private String avid;

    @Schema(description = "视频标题")
    private String title;

    @Schema(description = "视频简介")
    private String description;

    @Schema(description = "封面图URL")
    private String coverUrl;

    @Schema(description = "总时长（秒）")
    private Long totalDuration;

    @Schema(description = "总集数")
    private Integer episodeCount;

    @Schema(description = "播放数（多集累加）")
    private Long playCount;

    @Schema(description = "收藏数（多集累加）")
    private Long favoriteCount;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "是否连载中")
    private Boolean isOngoing;

    @Schema(description = "发布时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "当前播放剧集")
    private VideoEpisodeVo currentEpisode;

    @Schema(description = "剧集列表（仅审核通过）")
    private List<VideoEpisodeVo> episodes;

    // ========== 以下为UP主信息（实时从user_info获取，用于播放页UP主卡片展示） ==========

    @Schema(description = "UP主用户id")
    private Long userId;

    @Schema(description = "UP主昵称")
    private String nickName;

    @Schema(description = "UP主头像URL")
    private String avatarUrl;

    @Schema(description = "UP主uid")
    private String uid;

    @Schema(description = "UP主个性签名")
    private String descript;

    @Schema(description = "当前用户是否已关注该UP主")
    private Boolean isFollowed;
}
