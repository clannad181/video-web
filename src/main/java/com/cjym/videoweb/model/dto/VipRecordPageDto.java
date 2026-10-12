package com.cjym.videoweb.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "大会员开通记录分页列表项")
public class VipRecordPageDto {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户uid")
    private String userUid;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "用户昵称")
    private String nikeName;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "开通时长（月，指定到期时间开通时为 null）")
    private Integer durationMonths;

    @Schema(description = "大会员到期时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    @Schema(description = "记录状态：0-生效中，1-已到期")
    private Integer status;

    @Schema(description = "开通备注")
    private String remark;

    @Schema(description = "创建人（操作管理员ID）")
    private Long createUser;

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
