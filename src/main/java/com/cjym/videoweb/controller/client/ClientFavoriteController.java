package com.cjym.videoweb.controller.client;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.FavoriteAddDto;
import com.cjym.videoweb.model.dto.FavoriteFolderCreateDto;
import com.cjym.videoweb.model.dto.FavoriteFolderDeleteDto;
import com.cjym.videoweb.model.dto.FavoriteFolderDetailDto;
import com.cjym.videoweb.model.dto.FavoriteFolderUpdateDto;
import com.cjym.videoweb.model.dto.FavoriteListDto;
import com.cjym.videoweb.model.dto.FavoritePublicFolderListDto;
import com.cjym.videoweb.model.dto.FavoritePublicListDto;
import com.cjym.videoweb.model.dto.FavoriteRemoveDto;
import com.cjym.videoweb.model.vo.FavoriteEpisodeVo;
import com.cjym.videoweb.model.vo.FavoriteFolderDetailVo;
import com.cjym.videoweb.model.vo.FavoriteFolderVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.FavoriteFolderService;
import com.cjym.videoweb.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 收藏与收藏夹（前台）
 */
@RestController
@RequestMapping("/client/favorite")
@Tag(name = "收藏与收藏夹（前台）", description = "前台剧集收藏与收藏夹管理接口")
public class ClientFavoriteController {

    @Resource
    private FavoriteService favoriteService;

    @Resource
    private FavoriteFolderService favoriteFolderService;

    /**
     * 收藏剧集到文件夹
     *
     * @param dto 收藏请求体，episodeUid 必填，folderUid 可选（缺省为默认收藏夹）
     */
    @PostMapping("/add")
    @Operation(summary = "收藏到文件夹", description = "收藏剧集到指定收藏夹：folderUid不传时收藏到默认收藏夹；同一剧集可收藏到多个不同收藏夹")
    public R<String> add(@RequestBody FavoriteAddDto dto) {
        favoriteService.add(dto);
        return R.success("收藏成功");
    }

    /**
     * 取消收藏（不传 folderUid 取消所有收藏夹中的收藏）
     *
     * @param dto 移除请求体，episodeUid 必填，folderUid 可选（不传取消所有夹）
     */
    @PostMapping("/remove")
    @Operation(summary = "取消收藏", description = "取消剧集收藏：folderUid不传时取消该剧集在所有收藏夹中的收藏；传folderUid时仅从指定收藏夹移除（其他夹的收藏不受影响）")
    public R<String> remove(@RequestBody FavoriteRemoveDto dto) {
        favoriteService.remove(dto);
        return R.success("已取消收藏");
    }

    /**
     * 查看收藏夹详情
     *
     * @param dto 查询请求体，folderUid 必填，page/size 缺省为 1/10
     */
    @PostMapping("/detail")
    @Operation(summary = "查看收藏夹详情", description = "按收藏夹标识分页查看收藏内容并返回收藏夹基本信息：本人可查看自己的私密夹；他人私密夹统一返回「收藏夹不存在」；游客可访问公开夹")
    public R<FavoriteFolderDetailVo> detail(@RequestBody FavoriteFolderDetailDto dto) {
        return R.success(favoriteService.pageDetail(dto));
    }

    /**
     * 分页查询收藏列表
     *
     * @param dto 查询请求体，folderUid 可选（缺省为默认收藏夹），page/size 缺省为 1/10
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询收藏列表", description = "分页查询指定收藏夹内的收藏剧集列表（含视频与UP主信息），按收藏时间倒序")
    public R<IPage<FavoriteEpisodeVo>> list(@RequestBody FavoriteListDto dto) {
        return R.success(favoriteService.pageList(dto));
    }

    /**
     * 分页查看他人公开收藏夹内容
     *
     * @param dto 查询请求体，userUid、folderUid 必填，page/size 缺省为 1/10
     */
    @PostMapping("/public-list")
    @Operation(summary = "分页查看他人公开收藏夹内容", description = "分页查看指定用户公开收藏夹内的收藏剧集列表（私密夹不可见），游客可访问")
    public R<IPage<FavoriteEpisodeVo>> publicList(@RequestBody FavoritePublicListDto dto) {
        return R.success(favoriteService.pagePublicList(dto));
    }

    /**
     * 查询收藏夹列表
     */
    @GetMapping("/folder/list")
    @Operation(summary = "查询收藏夹列表", description = "查询当前用户全部收藏夹（含各夹收藏数量与私密标记），默认收藏夹排最前")
    public R<List<FavoriteFolderVo>> folderList() {
        return R.success(favoriteFolderService.list());
    }

    /**
     * 查看他人公开收藏夹列表
     *
     * @param dto 查询请求体，userUid 必填
     */
    @PostMapping("/folder/public-list")
    @Operation(summary = "查看他人公开收藏夹列表", description = "查看指定用户的公开收藏夹列表（含各夹收藏数量，私密夹不返回），游客可访问")
    public R<List<FavoriteFolderVo>> publicFolderList(@RequestBody FavoritePublicFolderListDto dto) {
        return R.success(favoriteFolderService.listPublic(dto));
    }

    /**
     * 创建收藏夹
     *
     * @param dto 创建请求体，name 必填（最长20字，同用户下不可重名）
     */
    @PostMapping("/folder/create")
    @Operation(summary = "创建收藏夹", description = "创建自定义收藏夹：名称最长20字且同用户下不可重名，每用户最多50个；isPrivate不传默认公开")
    public R<String> createFolder(@RequestBody FavoriteFolderCreateDto dto) {
        favoriteFolderService.create(dto);
        return R.success("创建成功");
    }

    /**
     * 重命名收藏夹
     *
     * @param dto 更新请求体，folderUid、name 必填
     */
    @PostMapping("/folder/update")
    @Operation(summary = "重命名/设置私密收藏夹", description = "修改收藏夹名称或可见性（至少传一个）：名称最长20字且同用户下不可重名；默认收藏夹同样支持设置私密")
    public R<String> updateFolder(@RequestBody FavoriteFolderUpdateDto dto) {
        favoriteFolderService.update(dto);
        return R.success("修改成功");
    }

    /**
     * 删除收藏夹
     *
     * @param dto 删除请求体，folderUid 必填
     */
    @PostMapping("/folder/delete")
    @Operation(summary = "删除收藏夹", description = "删除自定义收藏夹：夹内收藏自动移回默认收藏夹（默认夹已有同剧集收藏时合并）；默认收藏夹不可删除")
    public R<String> deleteFolder(@RequestBody FavoriteFolderDeleteDto dto) {
        favoriteFolderService.delete(dto);
        return R.success("删除成功");
    }
}
