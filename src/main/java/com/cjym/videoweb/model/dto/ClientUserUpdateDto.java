package com.cjym.videoweb.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "前台用户信息更新请求")
public class ClientUserUpdateDto {

    @Schema(description = "用户昵称（可选，不传则不修改）")
    private String nikeName;

    @Schema(description = "用户头像（可选，不传则不修改）")
    private String avatar;

    @Schema(description = "性别（可选，不传则不修改）")
    private Boolean sex;

    @Schema(description = "生日（可选，格式yyyy-MM-dd，不传则不修改）")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @Schema(description = "用户个性签名（可选，不传则不修改）")
    private String descript;
}
