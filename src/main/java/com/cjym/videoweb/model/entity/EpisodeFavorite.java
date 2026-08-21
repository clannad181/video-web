package com.cjym.videoweb.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "episode_favorite", schema = "public")
@Schema(description = "剧集收藏")
public class EpisodeFavorite {

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "关联剧集ID")
    private Long episodeId;

    @Schema(description = "所属收藏文件夹ID")
    private Long folderId;

    @Schema(description = "收藏用户ID")
    private Long userId;

    @Schema(description = "1-收藏，0-取消")
    private Integer status;

    @Schema(description = "操作时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
