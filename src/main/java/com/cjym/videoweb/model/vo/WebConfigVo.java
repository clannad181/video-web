package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "网站配置")
public class WebConfigVo {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "键")
    private String key;

    @Schema(description = "值")
    private String value;
}
