package com.cjym.videoweb.controller.client;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.OrderPageDto;
import com.cjym.videoweb.model.vo.OrderCancelVo;
import com.cjym.videoweb.model.vo.OrderCreateResultVo;
import com.cjym.videoweb.model.vo.OrderCreateVo;
import com.cjym.videoweb.model.vo.PayOrderVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.OrderService;
import com.cjym.videoweb.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单（客户端）
 * <p>
 * 用户充值大会员时创建订单；发起支付生成支付渠道页面，支付成功后由支付渠道异步通知开通大会员
 */
@RestController
@RequestMapping("/client/order")
@Tag(name = "订单", description = "前台订单创建、支付、取消与查询（大会员充值下单）")
public class ClientOrderController {

    @Resource
    private OrderService orderService;

    @Resource
    private PaymentService paymentService;

    @PostMapping("/create")
    @Operation(summary = "创建大会员充值订单", description = "按充值月数下单，月数必须命中 web_config 配置的充值套餐（vip/vip_pricing_plans），金额按套餐现价计算，返回订单号/金额/支付截止时间，需登录")
    public R<OrderCreateResultVo> create(@RequestBody OrderCreateVo vo) {
        return orderService.createVipOrder(vo);
    }

    @PostMapping("/pay")
    @Operation(summary = "发起支付", description = "按订单号和支付方式（1-支付宝）生成支付页面 HTML，data 为自动跳转收银台的 HTML 表单，前端需将其插入页面执行，需登录")
    public R<String> pay(@RequestBody PayOrderVo vo, HttpServletRequest request) {
        return R.success(paymentService.createPaymentPage(vo.getOrderNo(), vo.getPayMethod(), request));
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消订单", description = "取消本人待支付且未超时的订单，需登录")
    public R<String> cancel(@RequestBody OrderCancelVo vo) {
        return orderService.cancelOrder(vo);
    }

    @GetMapping("/list")
    @Operation(summary = "我的订单分页查询", description = "查询当前登录用户的订单，支持按状态筛选，按创建时间倒序，需登录")
    public R<IPage<OrderPageDto>> list(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) Integer status) {
        return R.success(orderService.pageMyOrders(page, size, status));
    }

    @GetMapping("/detail/{orderNo}")
    @Operation(summary = "我的订单详情", description = "按订单号查询本人订单详情，需登录")
    public R<OrderPageDto> detail(@PathVariable String orderNo) {
        return R.success(orderService.getMyOrderDetail(orderNo));
    }
}
