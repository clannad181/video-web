package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CaptchaVo {
    @Schema(description = "验证码内容")
    private String captcha;

    @Schema(description = "验证码key")
    private String key;
}
