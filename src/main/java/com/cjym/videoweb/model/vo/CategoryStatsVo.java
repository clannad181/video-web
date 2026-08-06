package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "视频分类统计")
public class CategoryStatsVo {

    @Schema(description = "分类名称（中文）")
    private String categoryName;

    @Schema(description = "视频数量")
    private Long videoCount;
}
