package com.cjym.videoweb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "评论信息")
public class CommentVo {

    @Schema(description = "评论id")
    private Long id;

    @Schema(description = "视频ID")
    private Long videoId;

    @Schema(description = "视频标题")
    private String videoTitle;

    @Schema(description = "视频封面URL")
    private String videoCover;

    @Schema(description = "评论者用户ID")
    private Long userId;

    @Schema(description = "评论者昵称")
    private String nickName;

    @Schema(description = "评论者头像")
    private String avatar;

    @Schema(description = "父评论ID，一级评论为0")
    private Long parentId;

    @Schema(description = "根评论ID")
    private Long rootId;

    @Schema(description = "评论正文")
    private String content;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "直接子评论数")
    private Integer replyCount;

    @Schema(description = "0-正常，1-用户删除，2-运营屏蔽")
    private Integer status;

    @Schema(description = "当前用户是否已点赞")
    private Boolean liked;

    @Schema(description = "子回复列表（懒加载，仅一级评论返回前几条）")
    private List<CommentVo> replies;

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
