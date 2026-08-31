package com.cjym.videoweb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 关注列表记录（UP主公开信息 + 关注时间）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "关注列表记录")
public class FollowListVo extends UpUserVo {

    @Schema(description = "关注时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime followTime;
}
