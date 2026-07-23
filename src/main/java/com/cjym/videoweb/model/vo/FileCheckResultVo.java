package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件预检查结果")
public class FileCheckResultVo {

    @Schema(description = "是否已存在（秒传）")
    private Boolean exist;

    @Schema(description = "已上传的分片索引列表")
    private List<Integer> uploadedChunks;

    @Schema(description = "文件信息（秒传时返回）")
    private FileResultVo file;

    @Schema(description = "服务端配置的单分片最大大小（字节）")
    private Long chunkSize;
}
