package com.cjym.videoweb.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "角色信息（含创建人昵称）")
public class RoleDto {

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "角色状态")
    private Boolean status;

    @Schema(description = "创建人账号")
    private String createUserName;

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "系统保留")
    private Boolean sysDef;

    @Schema(description = "是否删除")
    private Integer isDelete;

    @Schema(description = "创建者昵称")
    private String createUserNickname;
}
