package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "文件合并请求")
public class FileMergeVo {

    @Schema(description = "文件MD5", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileMd5;
}
