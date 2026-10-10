package com.cjym.videoweb.controller.admin;

import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.model.vo.GlobalSearchVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.CommentService;
import com.cjym.videoweb.service.UserInfoService;
import com.cjym.videoweb.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端全局搜索（视频/用户/评论）
 */
@RestController
@RequestMapping("/admin/search")
@Tag(name = "全局搜索（管理端）", description = "管理端视频、用户、评论全局模糊搜索")
public class AdminSearchController {

    @Resource
    private VideoService videoService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private CommentService commentService;

    @GetMapping
    @Operation(summary = "全局搜索", description = "输入关键词同时模糊搜索视频（标题或avid）、用户（昵称/用户名/邮箱/uid）、评论（内容），每类最多返回 limit 条（默认5，最大20），均按创建时间倒序")
    public R<GlobalSearchVo> search(
            @Parameter(description = "模糊关键词（必填）") @RequestParam String keyword,
            @Parameter(description = "每类返回条数，默认5，最大20") @RequestParam(required = false) Integer limit) {
        if (keyword == null || keyword.isBlank()) {
            throw new GlobeException("搜索关键词不能为空");
        }
        int safeLimit = limit == null || limit <= 0 ? 5 : Math.min(limit, 20);
        String kw = keyword.trim();
        GlobalSearchVo vo = new GlobalSearchVo();
        vo.setVideos(videoService.globalSearch(kw, safeLimit));
        vo.setUsers(userInfoService.globalSearch(kw, safeLimit));
        vo.setComments(commentService.globalSearch(kw, safeLimit));
        return R.success(vo);
    }
}
