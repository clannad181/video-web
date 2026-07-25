package com.cjym.videoweb.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "video_info", schema = "public")
@Schema(description = "视频信息")
public class Video {

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "视频标题")
    private String title;

    @Schema(description = "视频简介")
    private String description;

    @Schema(description = "分类id")
    private Long categoryId;

    @Schema(description = "封面图URL")
    private String coverUrl;

    @Schema(description = "总时长（秒，多集累加）")
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

    @Schema(description = "标签，多个以逗号分隔")
    private String tags;

    @Schema(description = "审核状态：0-待审核，1-审核通过，2-审核拒绝")
    private Integer reviewStatus;

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableField(value = "is_delete", fill = FieldFill.INSERT)
    @TableLogic(value = "0", delval = "1")
    private Integer isDelete;
}
