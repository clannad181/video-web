package com.cjym.videoweb.controller.admin;

import com.cjym.videoweb.model.vo.SystemInfoVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.SystemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/system")
@Tag(name = "系统监控", description = "系统资源信息监控")
public class SystemController {

    @Resource
    private SystemService systemService;

    @GetMapping("/info")
    @Operation(summary = "获取系统信息", description = "获取CPU、内存、磁盘、JVM等系统资源使用情况")
    public R<SystemInfoVo> getSystemInfo() {
        return R.success(systemService.getSystemInfo());
    }
}
