package com.cjym.videoweb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @Schema(description = "播放数（多集累加）")
    private Long playCount;

    @Schema(description = "收藏数（多集累加）")
    private Long favoriteCount;

    @Schema(description = "点赞数（多集累加）")
    private Long likeCount;

    @Schema(description = "投币数（多集累加）")
    private Long coinCount;

    @Schema(description = "观看人数（多集累加）")
    private Long viewerCount;

    @Schema(description = "UP主用户id")
    private Long userId;

    @Schema(description = "UP主用户名")
    private String userName;

    @Schema(description = "UP主昵称")
    private String nickName;

    @Schema(description = "UP主头像URL")
    private String avatarUrl;

    @Schema(description = "UP主uid")
    private String uid;

    @Schema(description = "是否连载中")
    private Boolean isOngoing;

    @Schema(description = "视频状态：0-草稿，1-待发布，2-已发布，3-已下架")
    private Integer videoStatus;

    @Schema(description = "视频审核情况：0-未审核，1-部分审核，2-审核完成")
    private Integer reviewStatus;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "剧集列表")
    private List<VideoEpisodeVo> episodes;

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
