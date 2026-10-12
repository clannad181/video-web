package com.cjym.videoweb.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.OrderPageDto;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单管理（管理端）
 */
@RestController
@RequestMapping("/admin/order")
@Tag(name = "订单管理", description = "订单查询（大会员充值订单）")
public class OrderController {

    @Resource
    private OrderService orderService;

    @GetMapping("/list")
    @Operation(summary = "订单分页查询", description = "支持按用户uid/订单号模糊搜索、业务类型/状态筛选，按创建时间倒序")
    public R<IPage<OrderPageDto>> list(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) String userUid,
                                       @RequestParam(required = false) String orderNo,
                                       @RequestParam(required = false) Integer bizType,
                                       @RequestParam(required = false) Integer status) {
        return R.success(orderService.pageOrders(page, size, userUid, orderNo, bizType, status));
    }

    @GetMapping("/detail/{orderNo}")
    @Operation(summary = "订单详情", description = "按订单号查询订单详情（含用户信息）")
    public R<OrderPageDto> detail(@PathVariable String orderNo) {
        return R.success(orderService.getOrderDetail(orderNo));
    }
}
