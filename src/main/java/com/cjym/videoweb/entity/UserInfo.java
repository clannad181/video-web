package com.cjym.videoweb.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@TableName(schema = "public")
@Data
public class UserInfo {
    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "用户密码")
    private String password;

    @Schema(description = "用户邮箱")
    private String email;

    @Schema(description = "是否为大会员")
    private Boolean isVip;

    @Schema(description = "用户昵称")
    private String nikeName;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "用户类型（0-普通用户，1-管理员用户）")
    private Boolean type;

    @Schema(description = "用户角色Id")
    private String roleId;

    @Schema(description = "用户状态 0-正常，1-禁用")
    private Integer status;

    @Schema(description = "用户个性签名")
    private String descript;

    @Schema(description = "性别")
    private Boolean sex;

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "创建人")
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @Schema(description = "更新人")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    @Schema(description = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableField(value = "is_delete",fill = FieldFill.INSERT)
    @TableLogic
    private Boolean isDelete;

    @Version
    private Integer version;
}
