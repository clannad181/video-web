package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 粉丝列表记录（粉丝公开信息 + 关注时间 + 是否已回关）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "粉丝列表记录")
public class FansListVo extends FollowListVo {

    @Schema(description = "是否已回关（当前用户是否已关注该粉丝）")
    private Boolean followedBack;
}
