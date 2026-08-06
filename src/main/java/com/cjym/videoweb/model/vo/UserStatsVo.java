package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户统计总览")
public class UserStatsVo {

    @Schema(description = "用户总数")
    private Long totalCount;

    @Schema(description = "大会员数")
    private Long vipCount;

    @Schema(description = "管理员数")
    private Long adminCount;

    @Schema(description = "今日新增用户数")
    private Long todayNewCount;

    @Schema(description = "较昨日新增变化（正为升，负为降）")
    private Long todayNewDiff;

    @Schema(description = "今日活跃用户数（当日至少调用一次接口的去重用户）")
    private Long todayActiveCount;

    @Schema(description = "较昨日活跃变化（正为升，负为降）")
    private Long todayActiveDiff;

    // ===== 以下为内部计算用字段，不暴露给前端 =====

    @Schema(hidden = true)
    private Long yesterdayNewCount;

    @Schema(hidden = true)
    private Long yesterdayActiveCount;
}
