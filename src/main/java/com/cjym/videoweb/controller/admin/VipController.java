package com.cjym.videoweb.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.VipRecordPageDto;
import com.cjym.videoweb.model.vo.VipOpenVo;
import com.cjym.videoweb.model.vo.VipRenewVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.VipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 大会员管理（管理端）
 */
@RestController
@RequestMapping("/admin/vip")
@Tag(name = "大会员管理", description = "大会员开通记录管理与开通/续期/撤销操作")
public class VipController {

    @Resource
    private VipService vipService;

    @GetMapping("/list")
    @Operation(summary = "分页查询大会员开通记录", description = "支持按用户uid/用户名模糊搜索、记录状态筛选，按创建时间倒序")
    public R<IPage<VipRecordPageDto>> list(@RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer size,
                                           @RequestParam(required = false) String userUid,
                                           @RequestParam(required = false) String userName,
                                           @RequestParam(required = false) Integer status) {
        return R.success(vipService.pageRecords(page, size, userUid, userName, status));
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "查询大会员记录详情", description = "按记录ID查询开通记录详情（含用户信息）")
    public R<VipRecordPageDto> detail(@PathVariable Long id) {
        return R.success(vipService.getDetail(id));
    }

    @PostMapping("/open")
    @Operation(summary = "开通大会员", description = "为前台普通用户开通大会员（按用户uid），开通时长与到期时间二选一必传，已是大会员时按时长开通会在现有到期时间上累加")
    public R<String> open(@RequestBody VipOpenVo vo) {
        return vipService.openVip(vo);
    }

    @PostMapping("/renew")
    @Operation(summary = "续期/修改到期时间", description = "对生效中的开通记录续期，续期时长与到期时间二选一必传")
    public R<String> renew(@RequestBody VipRenewVo vo) {
        return vipService.renewVip(vo);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "撤销大会员记录", description = "按记录ID撤销开通记录（逻辑删除），并重新计算用户大会员状态")
    public R<String> delete(@PathVariable Long id) {
        return vipService.revokeVip(id);
    }
}
