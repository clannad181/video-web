package com.cjym.videoweb.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
@TableName(value = "video_episode", schema = "public")
@Schema(description = "视频剧集")
public class VideoEpisode {

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "视频id")
    private Long videoId;

    @Schema(description = "剧集avid（av + Base62编码的雪花ID，用于指定集数跳转）")
    private String avid;

    @Schema(description = "集数序号（如第1集）")
    private Integer episodeNumber;

    @Schema(description = "剧集标题")
    private String title;

    @Schema(description = "剧集时长（秒）")
    private Long duration;

    @Schema(description = "播放地址")
    private String playUrl;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否需要大会员观看")
    private Boolean needVip;

    @Schema(description = "审核状态：0-待审核，1-审核通过，2-审核拒绝")
    private Integer reviewStatus;

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

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "是否删除")
    @TableField(value = "is_delete", fill = FieldFill.INSERT)
    @TableLogic(value = "0", delval = "1")
    private Integer isDelete;

    public boolean equalsObj(VideoEpisode ve) {
        return ve.getTitle().equals(this.getTitle())&&ve.getPlayUrl().equals(this.getPlayUrl());
    }

    public boolean equalsObj(String title, String playUrl) {
        if (StringUtils.isEmpty(title)) {
            return Objects.equals(playUrl, this.getPlayUrl());
        }
        return title.equals(this.getTitle()) && Objects.equals(playUrl, this.getPlayUrl());
    }
}
