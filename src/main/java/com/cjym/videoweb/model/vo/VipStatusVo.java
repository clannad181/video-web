package com.cjym.videoweb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "前台用户大会员状态")
public class VipStatusVo {

    @Schema(description = "是否为大会员")
    private Boolean isVip;

    @Schema(description = "大会员到期时间（非大会员为 null）")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime vipExpireTime;

    @Schema(description = "剩余天数（非大会员为 0）")
    private Long remainDays;
}
