package com.cjym.videoweb.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "搜索观看历史请求")
public class WatchHistorySearchDto {

    @Schema(description = "关键字（可选，聚合模糊匹配视频标题/剧集标题和UP主昵称，不传表示查询全部）")
    private String keyword;

    @Schema(description = "开始日期（可选，格式yyyy-MM-dd，含当天；不传默认2年前，最早只能到2年前）")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "结束日期（可选，格式yyyy-MM-dd，含当天；不传默认今天，不能晚于今天）")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "页码，从1开始（默认1，按月份分组分页）")
    private Integer page;

    @Schema(description = "每页月份组数量（默认10）")
    private Integer size;
}
