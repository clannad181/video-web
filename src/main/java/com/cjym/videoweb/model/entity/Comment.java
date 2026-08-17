package com.cjym.videoweb.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "comment", schema = "public")
@Schema(description = "评论")
public class Comment {

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "所属视频ID")
    private Long videoId;

    @Schema(description = "所属剧集ID（为空表示视频级历史评论）")
    private Long episodeId;

    @Schema(description = "评论者用户ID")
    private Long userId;

    @Schema(description = "父评论ID，一级评论为0")
    private Long parentId;

    @Schema(description = "根评论ID，一级评论root_id=id")
    private Long rootId;

    @Schema(description = "评论正文")
    private String content;

    @Schema(description = "点赞数冗余")
    private Integer likeCount;

    @Schema(description = "直接子评论数冗余")
    private Integer replyCount;

    @Schema(description = "0-正常，1-用户删除，2-运营屏蔽，3-待审核")
    private Integer status;

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
