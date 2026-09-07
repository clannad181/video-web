package com.cjym.videoweb.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "message_info", schema = "public")
@Schema(description = "消息通知")
public class MessageInfo {

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "消息唯一标识（av + Base62编码的雪花ID）")
    private String avid;

    @Schema(description = "接收者用户ID")
    private Long userId;

    @Schema(description = "消息类型：1-视频发布成功，2-稿件审核通知，3-举报下架通知，4-服务器状态通知")
    private Integer type;

    @Schema(description = "消息标题")
    private String title;

    @Schema(description = "消息正文")
    private String content;

    @Schema(description = "关联业务ID（如视频ID）")
    private Long bizId;

    @Schema(description = "关联业务avid（如视频avid）")
    private String bizAvid;

    @Schema(description = "播放跳转链接（如 /client/video/avXxx/episode/avYyy），服务器状态通知为 null")
    private String playLink;

    @Schema(description = "是否已读：0-未读，1-已读")
    private Integer isRead;

    @Schema(description = "阅读时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "是否删除")
    @TableField(value = "is_delete", fill = FieldFill.INSERT)
    @TableLogic(value = "0", delval = "1")
    private Integer isDelete;
}
