package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "访问IP排行")
public class IpRankVo {

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "访问次数")
    private Long count;
}
