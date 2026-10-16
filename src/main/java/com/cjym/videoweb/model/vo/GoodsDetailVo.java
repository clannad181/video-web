package com.cjym.videoweb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "商品详情")
public class GoodsDetailVo {

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "商品分类ID（关联 dict_data.id）")
    private Long categoryId;

    @Schema(description = "商品分类名称（分类被删除时为 null）")
    private String categoryLabel;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "原价（元，保留2位小数）")
    private BigDecimal originalPrice;

    @Schema(description = "优惠价（元，保留2位小数，null 表示无优惠）")
    private BigDecimal discountPrice;

    @Schema(description = "商品主图URL")
    private String cover;

    @Schema(description = "商品详情图URL列表")
    private List<String> images;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "商品状态：0-下架，1-上架")
    private Integer status;

    @Schema(description = "排序（越小越靠前）")
    private Integer sort;

    @Schema(description = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
