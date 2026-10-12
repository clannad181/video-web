package com.cjym.videoweb.controller.client;

import com.cjym.videoweb.model.vo.VipStatusVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.VipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 大会员（客户端）
 * <p>
 * 开通大会员功能暂定，当前仅提供状态查询
 */
@RestController
@RequestMapping("/client/vip")
@Tag(name = "大会员", description = "前台大会员状态查询（开通功能暂定）")
public class ClientVipController {

    @Resource
    private VipService vipService;

    @GetMapping("/status")
    @Operation(summary = "查询当前用户大会员状态", description = "获取当前登录前台用户的大会员状态（是否大会员、到期时间、剩余天数），未登录报错")
    public R<VipStatusVo> status() {
        return R.success(vipService.getCurrentUserVipStatus());
    }
}
