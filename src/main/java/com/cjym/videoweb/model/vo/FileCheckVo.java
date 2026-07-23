package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "文件预检查请求")
public class FileCheckVo {

    @Schema(description = "文件MD5", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileMd5;

    @Schema(description = "文件名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileName;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "总分片数")
    private Integer totalChunks;

    @Schema(description = "分片大小（字节）")
    private Long chunkSize;

    @Schema(description = "文件分类（如 avatar/video/image/document）")
    private String category;
}
