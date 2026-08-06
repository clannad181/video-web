package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "每小时活跃用户数")
public class HourlyActiveVo {

    @Schema(description = "小时（格式 HH:00）")
    private String hour;

    @Schema(description = "活跃用户数（去重）")
    private Long activeCount;
}
