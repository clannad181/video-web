package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.constant.OrderConstant;
import com.cjym.videoweb.constant.VipConstant;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.OrderInfoMapper;
import com.cjym.videoweb.model.dto.OrderPageDto;
import com.cjym.videoweb.model.entity.OrderInfo;
import com.cjym.videoweb.model.vo.OrderCancelVo;
import com.cjym.videoweb.model.vo.OrderCreateResultVo;
import com.cjym.videoweb.model.vo.OrderCreateVo;
import com.cjym.videoweb.model.vo.VipPlanVo;
import com.cjym.videoweb.model.vo.VipPlansVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.OrderService;
import com.cjym.videoweb.service.VipService;
import com.cjym.videoweb.task.OrderTimeoutQueue;
import com.cjym.videoweb.utils.Base62Converter;
import com.cjym.videoweb.utils.JWTUtil;
import com.cjym.videoweb.utils.SnowflakeIdGenerator;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {

    @Resource
    private VipService vipService;

    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Resource
    private OrderTimeoutQueue orderTimeoutQueue;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<OrderCreateResultVo> createVipOrder(OrderCreateVo vo) {
        Long userId = requireLoginUserId();
        if (vo == null || vo.getDurationMonths() == null) {
            return R.error("充值月数不能为空");
        }
        if (vo.getDurationMonths() < 1 || vo.getDurationMonths() > VipConstant.MAX_DURATION_MONTHS) {
            return R.error("充值月数必须在1~" + VipConstant.MAX_DURATION_MONTHS + "个月之间");
        }
        // 金额按套餐配置计算：充值月数必须命中已配置的充值套餐或单月套餐（套餐配置见 web_config 的 vip/vip_pricing_plans）
        VipPlansVo vipPlans = vipService.getVipPlans();
        List<VipPlanVo> availablePlans = new ArrayList<>(vipPlans.getPlans());
        availablePlans.add(vipPlans.getVipPricePerMonth());
        VipPlanVo plan = availablePlans.stream()
                .filter(p -> Objects.equals(p.getDurationMonths(), vo.getDurationMonths()))
                .findFirst()
                .orElse(null);
        if (plan == null) {
            return R.error("充值月数未匹配到已配置的充值套餐");
        }
        BigDecimal amount = plan.getPrice().setScale(2, RoundingMode.HALF_UP);
        LocalDateTime now = LocalDateTime.now();
        OrderInfo order = new OrderInfo();
        order.setOrderNo(OrderConstant.ORDER_NO_PREFIX + Base62Converter.encode(snowflakeIdGenerator.nextId()));
        order.setUserId(userId);
        order.setBizType(OrderConstant.BIZ_TYPE_VIP_RECHARGE);
        order.setAmount(amount);
        order.setDurationMonths(vo.getDurationMonths());
        order.setStatus(OrderConstant.STATUS_PENDING_PAY);
        order.setExpireTime(now.plusMinutes(OrderConstant.ORDER_TIMEOUT_MINUTES));
        order.setRemark("大会员充值" + (StringUtils.isNotEmpty(plan.getName()) ? plan.getName() : vo.getDurationMonths() + "个月"));
        save(order);
        // 加入 Redis 延迟队列：到期后由消费线程关闭订单（Redis 异常静默降级，DB 定时扫描兜底）
        orderTimeoutQueue.offer(order.getOrderNo(), order.getExpireTime());
        // 组装返回：订单号/金额/支付截止时间
        OrderCreateResultVo result = new OrderCreateResultVo();
        result.setOrderNo(order.getOrderNo());
        result.setAmount(amount);
        result.setExpireTime(order.getExpireTime());
        return R.success("下单成功", result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> cancelOrder(OrderCancelVo vo) {
        Long userId = requireLoginUserId();
        if (vo == null || StringUtils.isEmpty(vo.getOrderNo())) {
            return R.error("订单号不能为空");
        }
        OrderInfo order = baseMapper.selectOne(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getOrderNo, vo.getOrderNo().trim()));
        if (order == null) {
            return R.error("订单不存在");
        }
        if (!userId.equals(order.getUserId())) {
            return R.error("只能操作自己的订单");
        }
        if (!Integer.valueOf(OrderConstant.STATUS_PENDING_PAY).equals(order.getStatus())) {
            return R.error("当前订单状态不可取消");
        }
        if (order.getExpireTime() != null && !order.getExpireTime().isAfter(LocalDateTime.now())) {
            return R.error("订单已超时，无法取消");
        }
        order.setStatus(OrderConstant.STATUS_CANCELLED);
        updateById(order);
        // 移除延迟队列中的超时任务，避免到期后无效消费
        orderTimeoutQueue.remove(order.getOrderNo());
        return R.success("订单已取消");
    }

    @Override
    public IPage<OrderPageDto> pageMyOrders(Integer page, Integer size, Integer status) {
        Long userId = requireLoginUserId();
        Page<OrderPageDto> pageParam = new Page<>(page == null ? 1 : page, size == null ? 10 : size);
        return baseMapper.selectOrderPage(pageParam, userId, null, null, null, status);
    }

    @Override
    public OrderPageDto getMyOrderDetail(String orderNo) {
        Long userId = requireLoginUserId();
        if (StringUtils.isEmpty(orderNo)) {
            throw new GlobeException("订单号不能为空");
        }
        OrderPageDto detail = baseMapper.selectDetailByOrderNo(orderNo.trim());
        if (detail == null) {
            throw new GlobeException("订单不存在");
        }
        if (!userId.equals(detail.getUserId())) {
            throw new GlobeException("只能查看自己的订单");
        }
        return detail;
    }

    @Override
    public IPage<OrderPageDto> pageOrders(Integer page, Integer size, String userUid, String orderNo,
                                          Integer bizType, Integer status) {
        Page<OrderPageDto> pageParam = new Page<>(page == null ? 1 : page, size == null ? 10 : size);
        return baseMapper.selectOrderPage(pageParam, null, userUid, orderNo, bizType, status);
    }

    @Override
    public OrderPageDto getOrderDetail(String orderNo) {
        if (StringUtils.isEmpty(orderNo)) {
            throw new GlobeException("订单号不能为空");
        }
        OrderPageDto detail = baseMapper.selectDetailByOrderNo(orderNo.trim());
        if (detail == null) {
            throw new GlobeException("订单不存在");
        }
        return detail;
    }

    @Override
    public int closeTimeoutOrders() {
        LocalDateTime now = LocalDateTime.now();
        // 查询超时未支付的待支付订单
        List<OrderInfo> timeoutOrders = baseMapper.selectList(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getStatus, OrderConstant.STATUS_PENDING_PAY)
                .le(OrderInfo::getExpireTime, now));
        if (timeoutOrders.isEmpty()) {
            return 0;
        }
        LambdaUpdateWrapper<OrderInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OrderInfo::getStatus, OrderConstant.STATUS_PENDING_PAY)
                .le(OrderInfo::getExpireTime, now)
                .set(OrderInfo::getStatus, OrderConstant.STATUS_CLOSED);
        boolean updated = update(wrapper);
        return updated ? timeoutOrders.size() : 0;
    }

    @Override
    public void closeTimeoutOrder(String orderNo) {
        if (StringUtils.isEmpty(orderNo)) {
            return;
        }
        OrderInfo order = baseMapper.selectOne(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getOrderNo, orderNo.trim()));
        if (order == null) {
            log.warn("超时关闭订单不存在[orderNo={}]", orderNo);
            return;
        }
        // 幂等校验：仅待支付且已过支付截止时间的订单才会被关闭（已支付/已取消直接忽略）
        if (!Integer.valueOf(OrderConstant.STATUS_PENDING_PAY).equals(order.getStatus())) {
            return;
        }
        if (order.getExpireTime() != null && order.getExpireTime().isAfter(LocalDateTime.now())) {
            log.warn("订单支付截止时间未到[orderNo={}]，跳过关闭", orderNo);
            return;
        }
        order.setStatus(OrderConstant.STATUS_CLOSED);
        updateById(order);
        log.info("订单超时关闭[orderNo={}]", orderNo);
    }

    /**
     * 解析当前登录用户id（未登录抛异常）
     */
    private Long requireLoginUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                throw new GlobeException("请先登录");
            }
            HttpServletRequest request = attributes.getRequest();
            String token = request.getHeader("authorization");
            if (StringUtils.isEmpty(token)) {
                throw new GlobeException("请先登录");
            }
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = JWTUtil.getUserId(token);
            if (userId == null) {
                throw new GlobeException("请先登录");
            }
            return userId;
        } catch (GlobeException e) {
            throw e;
        } catch (Exception e) {
            throw new GlobeException("请先登录");
        }
    }
}
