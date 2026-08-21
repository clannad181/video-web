package com.cjym.videoweb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "收藏夹信息")
public class FavoriteFolderVo {

    @Schema(description = "收藏夹前台标识")
    private String folderUid;

    @Schema(description = "收藏夹名称")
    private String name;

    @Schema(description = "是否默认收藏夹")
    private Boolean isDefault;

    @Schema(description = "是否私密（仅本人可见）")
    private Boolean isPrivate;

    @Schema(description = "收藏数量")
    private Long count;

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
