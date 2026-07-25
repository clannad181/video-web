package com.cjym.videoweb.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "视频分页查询结果")
public class VideoPageDto {

    @Schema(description = "视频id")
    private Long id;

    @Schema(description = "视频标题")
    private String title;

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

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "UP主昵称")
    private String nickName;

    @Schema(description = "审核状态：0-待审核，1-审核通过，2-审核拒绝")
    private Integer reviewStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
