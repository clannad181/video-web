package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录账号排行")
public class LoginAccountRankVo {

    @Schema(description = "登录账号")
    private String account;

    @Schema(description = "登录次数")
    private Long count;
}
