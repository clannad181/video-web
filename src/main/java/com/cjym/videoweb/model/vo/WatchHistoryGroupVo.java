package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "观看历史按日期分组视图")
public class WatchHistoryGroupVo {

    @Schema(description = "观看日期")
    private LocalDate date;

    @Schema(description = "当天观看记录（按观看时间倒序，同一视频当天仅保留最新一条）")
    private List<WatchHistoryVo> items;
}
