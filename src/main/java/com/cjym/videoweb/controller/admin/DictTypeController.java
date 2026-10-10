package com.cjym.videoweb.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.DictTypePageDto;
import com.cjym.videoweb.model.entity.DictType;
import com.cjym.videoweb.model.vo.DictTypeVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.DictTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/dict/type")
@Tag(name = "字典类型管理", description = "字典类型管理")
public class DictTypeController {

    @Resource
    private DictTypeService dictTypeService;

    @GetMapping("/list")
    @Operation(summary = "分页查询字典类型列表", description = "支持按编码/名称模糊搜索、状态筛选、动态排序，含该类型下字典数据条数统计")
    public R<IPage<DictTypePageDto>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String typeCode,
            @RequestParam(required = false) String typeName,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) String field,
            @RequestParam(required = false) String descOrAsc) {
        return R.success(dictTypeService.pageDictType(page, size, typeCode, typeName, status, field, descOrAsc));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询字典类型详情", description = "根据ID查询字典类型详情")
    public R<DictTypeVo> getDetail(@PathVariable Long id) {
        return R.success(dictTypeService.getDetailById(id));
    }

    @PostMapping("/add")
    @Operation(summary = "新增字典类型", description = "新增字典类型，编码与名称均唯一")
    public R<String> add(@RequestBody DictType dictType) {
        dictTypeService.add(dictType);
        return R.success("新增成功");
    }

    @PostMapping("/update")
    @Operation(summary = "修改字典类型", description = "修改字典类型，系统内置字典不可修改编码，编码变更时同步更新其下字典数据")
    public R<String> update(@RequestBody DictType dictType) {
        dictTypeService.update(dictType);
        return R.success("修改成功");
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除字典类型", description = "根据ID逻辑删除字典类型，系统内置字典不可删除，删除时级联逻辑删除其下字典数据")
    public R<String> delete(@PathVariable Long id) {
        dictTypeService.delete(id);
        return R.success("删除成功");
    }
}
