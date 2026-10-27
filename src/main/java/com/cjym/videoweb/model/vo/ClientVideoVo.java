package com.cjym.videoweb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "前台视频列表记录")
public class ClientVideoVo {

    @Schema(description = "视频id")
    private Long id;

    @Schema(description = "视频avid")
    private String avid;

    @Schema(description = "最新更新剧集avid（审核通过剧集中集数序号最大的一集）")
    private String episodeAvid;

    @Schema(description = "是否需要大会员观看（取最新更新剧集的needVip）")
    private Boolean needVip;

    @Schema(description = "播放跳转链接（如 /avXxx/episode/avYyy）")
    private String playLink;

    @Schema(description = "视频标题")
    private String title;

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

    @Schema(description = "分类id")
    private Long categoryId;

    @Schema(description = "视频标签，多个以逗号分隔")
    private String tags;

    @Schema(description = "UP主昵称")
    private String nickName;

    @Schema(description = "UP主用户id")
    private Long userId;

    @Schema(description = "当前用户是否已关注该UP主")
    private Boolean isFollowed;

    @Schema(description = "UP主头像URL")
    private String avatarUrl;

    @Schema(description = "UP主uid")
    private String uid;

    @Schema(description = "是否连载中")
    private Boolean isOngoing;

    @Schema(description = "发布时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
