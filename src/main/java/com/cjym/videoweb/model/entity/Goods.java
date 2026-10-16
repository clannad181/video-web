package com.cjym.videoweb.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName(value = "goods", schema = "public")
@Schema(description = "商品")
public class Goods {

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "商品分类（关联 dict_data.id，type_code=goods_category）")
    private Long categoryId;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "原价（元，保留2位小数）")
    private BigDecimal originalPrice;

    @Schema(description = "优惠价（元，保留2位小数，null 表示无优惠）")
    private BigDecimal discountPrice;

    @Schema(description = "商品主图URL")
    private String cover;

    @Schema(description = "商品详情图URL列表（逗号分隔）")
    private String images;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "商品状态：0-下架，1-上架")
    private Integer status;

    @Schema(description = "排序（越小越靠前）")
    private Integer sort;

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "创建人（操作管理员ID）")
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @Schema(description = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "更新人")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    @Schema(description = "是否删除（0-未删除，1-已删除）")
    @TableField(value = "is_delete", fill = FieldFill.INSERT)
    @TableLogic(value = "0", delval = "1")
    private Integer isDelete;
}
