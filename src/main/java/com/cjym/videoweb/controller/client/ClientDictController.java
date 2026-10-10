package com.cjym.videoweb.controller.client;

import com.cjym.videoweb.model.vo.DictDataSimpleVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.DictDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 字典查询（客户端）
 */
@RestController
@RequestMapping("/client/dict")
@Tag(name = "字典查询", description = "前台字典数据查询")
public class ClientDictController {

    @Resource
    private DictDataService dictDataService;

    @GetMapping("/{typeCode}")
    @Operation(summary = "按类型编码获取字典数据列表", description = "获取启用中字典类型的启用数据，仅返回id/label/value/sort/isDefault，按排序升序排列，供前台下拉选择使用")
    public R<List<DictDataSimpleVo>> listByTypeCode(@PathVariable String typeCode) {
        return R.success(dictDataService.listEnabledByTypeCode(typeCode));
    }
}
