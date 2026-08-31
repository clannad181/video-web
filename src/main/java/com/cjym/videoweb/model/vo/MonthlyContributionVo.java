package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "按月份分组的投稿记录")
public class MonthlyContributionVo {

    @Schema(description = "月份（yyyy-MM）")
    private String month;

    @Schema(description = "该月投稿数量")
    private Integer count;

    @Schema(description = "该月投稿视频列表（按投稿时间倒序）")
    private List<MyContributionVideoVo> videos;
}
