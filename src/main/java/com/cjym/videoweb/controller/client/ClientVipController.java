package com.cjym.videoweb.controller.client;

import com.cjym.videoweb.model.vo.VipPlansVo;
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
 * 提供大会员状态查询与充值套餐查询；购买大会员通过订单接口下单支付
 */
@RestController
@RequestMapping("/client/vip")
@Tag(name = "大会员", description = "前台大会员状态与充值套餐查询")
public class ClientVipController {

    @Resource
    private VipService vipService;

    @GetMapping("/status")
    @Operation(summary = "查询当前用户大会员状态", description = "获取当前登录前台用户的大会员状态（是否大会员、到期时间、剩余天数），未登录报错")
    public R<VipStatusVo> status() {
        return R.success(vipService.getCurrentUserVipStatus());
    }

    @GetMapping("/plans")
    @Operation(summary = "查询大会员充值套餐与单月套餐", description = "返回单月套餐（vip_price_per_month，结构与套餐项一致，优先取套餐列表中 1 个月套餐，无该套餐时按月单价配置合成）与充值套餐列表（套餐名/时长/现价/原价），原价在配置 autoOrig=true 时按月单价自动计算")
    public R<VipPlansVo> plans() {
        return R.success(vipService.getVipPlans());
    }
}
