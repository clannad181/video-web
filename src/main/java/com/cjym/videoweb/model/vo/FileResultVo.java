package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件上传结果")
public class FileResultVo {

    @Schema(description = "文件ID")
    private Long fileId;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件MD5")
    private String fileMd5;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件访问URL")
    private String url;

    @Schema(description = "是否秒传")
    private Boolean instantUpload;
}
