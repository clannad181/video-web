package com.cjym.videoweb.controller.client;

import com.cjym.videoweb.model.vo.CategorySimpleVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 视频分类（客户端）
 */
@RestController
@RequestMapping("/client/category")
@Tag(name = "视频分类", description = "前台视频分类查询")
public class ClientCategoryController {

    @Resource
    private CategoryService categoryService;

    @GetMapping("/list")
    @Operation(summary = "获取前台展示的分类列表", description = "获取所有启用且可见的分类，仅返回id/name/sort，按排序升序排列，供前台页面分类导航使用")
    public R<List<CategorySimpleVo>> list() {
        return R.success(categoryService.listEnabled());
    }
}
