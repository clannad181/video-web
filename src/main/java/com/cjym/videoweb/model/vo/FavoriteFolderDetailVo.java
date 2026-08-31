package com.cjym.videoweb.model.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "收藏夹详情（收藏夹基本信息与收藏分页列表）")
public class FavoriteFolderDetailVo {

    @Schema(description = "收藏夹前台标识")
    private String folderUid;

    @Schema(description = "收藏夹名称")
    private String name;

    @Schema(description = "是否默认收藏夹")
    private Boolean isDefault;

    @Schema(description = "是否私密（仅本人可见）")
    private Boolean isPrivate;

    @Schema(description = "封面URL（自定义封面优先，未设置时为夹内最新收藏视频封面）")
    private String coverUrl;

    @Schema(description = "收藏夹描述")
    private String description;

    @Schema(description = "所属用户uid")
    private String userUid;

    @Schema(description = "所属用户昵称")
    private String nickName;

    @Schema(description = "所属用户头像URL")
    private String avatarUrl;

    @Schema(description = "收藏列表（分页，按收藏时间倒序）")
    private IPage<FavoriteEpisodeVo> list;
}
