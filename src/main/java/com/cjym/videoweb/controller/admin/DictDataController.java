package com.cjym.videoweb.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.DictDataPageDto;
import com.cjym.videoweb.model.entity.DictData;
import com.cjym.videoweb.model.vo.DictDataSimpleVo;
import com.cjym.videoweb.model.vo.DictDataVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.DictDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dict/data")
@Tag(name = "字典数据管理", description = "字典数据管理")
public class DictDataController {

    @Resource
    private DictDataService dictDataService;

    @GetMapping("/list")
    @Operation(summary = "分页查询字典数据列表", description = "按类型编码精确筛选（必填），支持标签模糊搜索、状态筛选、动态排序")
    public R<IPage<DictDataPageDto>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam String typeCode,
            @RequestParam(required = false) String label,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) String field,
            @RequestParam(required = false) String descOrAsc) {
        return R.success(dictDataService.pageDictData(page, size, typeCode, label, status, field, descOrAsc));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询字典数据详情", description = "根据ID查询字典数据详情")
    public R<DictDataVo> getDetail(@PathVariable Long id) {
        return R.success(dictDataService.getDetailById(id));
    }

    @PostMapping("/add")
    @Operation(summary = "新增字典数据", description = "新增字典数据，同类型下标签与字典值均唯一")
    public R<String> add(@RequestBody DictData dictData) {
        dictDataService.add(dictData);
        return R.success("新增成功");
    }

    @PostMapping("/update")
    @Operation(summary = "修改字典数据", description = "修改字典数据，变更类型编码时需目标类型存在")
    public R<String> update(@RequestBody DictData dictData) {
        dictDataService.update(dictData);
        return R.success("修改成功");
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除字典数据", description = "根据ID逻辑删除字典数据")
    public R<String> delete(@PathVariable Long id) {
        dictDataService.delete(id);
        return R.success("删除成功");
    }

    @GetMapping("/enabled/{typeCode}")
    @Operation(summary = "按类型编码获取启用中的字典数据", description = "根据字典类型编码查询启用中的所有字典数据，仅返回id/label/value/sort/isDefault，按排序升序排列，供下拉选择使用")
    public R<List<DictDataSimpleVo>> listEnabled(@PathVariable String typeCode) {
        return R.success(dictDataService.listEnabledByTypeCode(typeCode));
    }
}
