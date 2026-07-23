package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新用户请求")
public class UserUpdateVo {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "用户邮箱")
    private String email;

    @Schema(description = "用户昵称")
    private String nikeName;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "用户类型（0-普通用户，1-管理员用户）")
    private Integer type;

    @Schema(description = "用户角色Id")
    private Long roleId;

    @Schema(description = "是否为大会员")
    private Boolean isVip;

    @Schema(description = "用户状态 0-正常，1-禁用")
    private Integer status;

    @Schema(description = "性别")
    private Boolean sex;

    @Schema(description = "用户个性签名")
    private String descript;
}
