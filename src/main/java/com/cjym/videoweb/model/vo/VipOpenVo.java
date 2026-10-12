package com.cjym.videoweb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理端开通大会员请求")
public class VipOpenVo {

    @Schema(description = "用户uid（16位随机数字）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userUid;

    @Schema(description = "开通时长（月，1~120；与 expireTime 二选一必传，用户已是大会员时在现有到期时间上累加）")
    private Integer durationMonths;

    @Schema(description = "到期时间（直接指定；与 durationMonths 二选一必传，必须晚于当前时间）")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    @Schema(description = "开通备注")
    private String remark;
}
