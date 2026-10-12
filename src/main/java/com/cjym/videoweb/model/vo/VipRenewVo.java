package com.cjym.videoweb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理端续期/修改大会员请求")
public class VipRenewVo {

    @Schema(description = "大会员开通记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "续期时长（月，1~120；与 expireTime 二选一必传，在记录当前到期时间上累加）")
    private Integer durationMonths;

    @Schema(description = "到期时间（直接指定；与 durationMonths 二选一必传，必须晚于当前时间）")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    @Schema(description = "备注（覆盖原备注）")
    private String remark;
}
