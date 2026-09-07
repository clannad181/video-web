package com.cjym.videoweb.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户画像（基于观看历史计算的兴趣标签与分类权重，派生数据可全量重建）
 */
@Data
@TableName(value = "user_profile", schema = "public")
@Schema(description = "用户画像")
public class UserProfile {

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "兴趣维度（tag-标签 / category-分类）")
    private String itemType;

    @Schema(description = "分类ID（分类维度使用，标签维度为空）")
    private Long itemId;

    @Schema(description = "标签名或分类名")
    private String itemName;

    @Schema(description = "兴趣得分（观看时长占比×时间衰减累加）")
    private BigDecimal score;

    @Schema(description = "观看视频数（去重后）")
    private Integer watchCount;

    @Schema(description = "累计观看秒数")
    private Long totalWatchSeconds;

    @Schema(description = "该维度最近观看时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastWatchTime;

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
