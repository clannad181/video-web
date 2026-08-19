package com.cjym.videoweb.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "episode_coin", schema = "public")
@Schema(description = "剧集投币")
public class EpisodeCoin {

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "关联剧集ID")
    private Long episodeId;

    @Schema(description = "投币用户ID")
    private Long userId;

    @Schema(description = "投币数量（默认1个）")
    private Integer coinCount;

    @Schema(description = "操作时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
