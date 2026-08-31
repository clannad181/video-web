package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "观看历史按月份分组视图")
public class WatchHistoryMonthVo {

    @Schema(description = "月份（yyyy-MM）")
    private String month;

    @Schema(description = "当月观看记录（按日期倒序分组，每个日期组内记录完整）")
    private List<WatchHistoryGroupVo> days;
}
