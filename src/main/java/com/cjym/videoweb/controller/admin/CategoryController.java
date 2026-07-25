package com.cjym.videoweb.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.CategoryPageDto;
import com.cjym.videoweb.model.entity.Category;
import com.cjym.videoweb.model.vo.CategoryVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 视频分类管理
 */
@RestController
@RequestMapping("/admin/category")
@Tag(name = "视频分类管理", description = "视频分类管理")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    @GetMapping("/list")
    @Operation(summary = "分页查询分类列表", description = "支持按名称模糊搜索、状态、是否显示筛选、动态排序")
    public R<IPage<CategoryPageDto>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) Boolean isShow,
            @RequestParam(required = false) String field,
            @RequestParam(required = false) String descOrAsc) {
        return R.success(categoryService.pageCategory(page, size, name, status, isShow, field, descOrAsc));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询分类详情", description = "根据ID查询分类详情")
    public R<CategoryVo> getDetail(@PathVariable Long id) {
        return R.success(categoryService.getDetailById(id));
    }

    @PostMapping("/add")
    @Operation(summary = "新增分类", description = "新增视频分类")
    public R<String> add(@RequestBody Category category) {
        categoryService.add(category);
        return R.success("新增成功");
    }

    @PostMapping("/update")
    @Operation(summary = "修改分类", description = "修改视频分类")
    public R<String> update(@RequestBody Category category) {
        categoryService.update(category);
        return R.success("修改成功");
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除分类", description = "根据ID逻辑删除分类，系统保留分类不可删除")
    public R<String> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return R.success("删除成功");
    }

    @GetMapping("/enabled")
    @Operation(summary = "获取显示中的分类列表", description = "获取所有 isShow=true 的分类，按排序升序排列，供下拉选择使用")
    public R<List<CategoryVo>> listEnabled() {
        return R.success(categoryService.listEnabled());
    }
}
