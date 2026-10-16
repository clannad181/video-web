package com.cjym.videoweb.controller.admin;

import com.cjym.videoweb.model.vo.WebConfigVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.WebConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/config")
@Tag(name = "网站配置管理", description = "网站配置管理")
public class WebConfigController {

    @Resource
    private WebConfigService webConfigService;

    @GetMapping("/list")
    @Operation(summary = "查询配置列表", description = "查询配置列表，可按分类筛选")
    public R<List<WebConfigVo>> list(@RequestParam(required = false) String category) {
        return webConfigService.list(category);
    }

    @PostMapping("/update")
    @Operation(summary = "更新配置", description = "批量更新配置，支持一次传递多条，id为空视为新增（分类与键必填且不能重复）")
    public R<String> update(@RequestBody List<WebConfigVo> configList) {
        return webConfigService.update(configList);
    }
}
