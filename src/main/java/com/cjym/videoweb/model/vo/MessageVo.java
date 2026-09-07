package com.cjym.videoweb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "消息通知记录")
public class MessageVo {

    @Schema(description = "消息id")
    private Long id;

    @Schema(description = "消息唯一标识avid")
    private String avid;

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

    @Schema(description = "消息创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
