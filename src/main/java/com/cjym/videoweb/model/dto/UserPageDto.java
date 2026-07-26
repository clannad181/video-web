package com.cjym.videoweb.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户分页列表（含角色名称）")
public class UserPageDto {

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "用户唯一标识uid")
    private String uid;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "用户昵称")
    private String nikeName;

    @Schema(description = "用户邮箱")
    private String email;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "用户状态 0-正常，1-禁用")
    private Integer status;

    @Schema(description = "用户类型（0-普通用户，1-管理员用户）")
    private Integer type;

    @Schema(description = "是否为大会员")
    private Boolean isVip;

    @Schema(description = "用户角色Id")
    private Long roleId;

    @Schema(description = "用户角色等级")
    private Integer level;

    @Schema(description = "用户角色名称")
    private String roleName;

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
