package com.cjym.videoweb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "每日增量（视频+评论+播放量）")
public class DailyIncrementVo {

    @Schema(description = "日期")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;

    @Schema(description = "当日新增视频数")
    private Long videoCount;

    @Schema(description = "当日新增评论数")
    private Long commentCount;

    @Schema(description = "当日总播放量")
    private Long playCount;
}
