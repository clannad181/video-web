package com.cjym.videoweb.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(schema = "public")
@Schema(description = "文件信息")
public class FileInfo {

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件MD5")
    private String fileMd5;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件类型（扩展名）")
    private String fileType;

    @Schema(description = "文件存放路径")
    private String filePath;

    @Schema(description = "文件分类目录")
    private String category;

    @Schema(description = "总分片数")
    private Integer totalChunks;

    @Schema(description = "分片大小（字节）")
    private Long chunkSize;

    @Schema(description = "状态：0-上传中，1-已完成")
    private Integer status;

    @Schema(description = "文件访问URL")
    private String url;

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "创建人")
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @Schema(description = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableField(value = "is_delete", fill = FieldFill.INSERT)
    @TableLogic(value = "0", delval = "1")
    private Integer isDelete;
}
