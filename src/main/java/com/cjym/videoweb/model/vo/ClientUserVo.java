package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 前台用户基础信息（客户端）
 */
@Data
@Schema(description = "前台用户基础信息")
public class ClientUserVo {

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "用户唯一标识uid（16位随机数字）")
    private String uid;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "用户邮箱")
    private String email;

    @Schema(description = "是否为大会员")
    private Boolean isVip;

    @Schema(description = "用户昵称")
    private String nikeName;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "用户类型（0-普通用户，1-管理员用户）")
    private Integer type;

    @Schema(description = "用户状态 0-正常，1-禁用")
    private Integer status;

    @Schema(description = "用户个性签名")
    private String descript;

    @Schema(description = "性别")
    private Boolean sex;
}
