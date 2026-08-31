package com.cjym.videoweb.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@TableName(value = "favorite_folder", schema = "public")
@Data
@Schema(description = "收藏文件夹")
public class FavoriteFolder {

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "文件夹前台标识（fd前缀Base62编码）")
    private String avid;

    @Schema(description = "所属用户ID")
    private Long userId;

    @Schema(description = "文件夹名称")
    private String name;

    @Schema(description = "1-默认文件夹，0-自定义文件夹")
    private Integer isDefault;

    @Schema(description = "1-私密（仅本人可见），0-公开")
    private Integer isPrivate;

    @Schema(description = "自定义封面URL（null表示未设置，使用最新收藏视频封面）")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String coverUrl;

    @Schema(description = "收藏夹描述（最长200字，null表示未设置）")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String description;

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableField(value = "is_delete", fill = FieldFill.INSERT)
    @TableLogic(value = "0", delval = "1")
    private Integer isDelete;
}
