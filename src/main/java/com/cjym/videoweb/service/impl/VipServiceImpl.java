package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.constant.BaseConstant;
import com.cjym.videoweb.constant.MessageConstant;
import com.cjym.videoweb.constant.OrderConstant;
import com.cjym.videoweb.constant.VipConstant;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.UserInfoMapper;
import com.cjym.videoweb.mapper.VipRecordMapper;
import com.cjym.videoweb.model.dto.VipRecordPageDto;
import com.cjym.videoweb.model.entity.MessageInfo;
import com.cjym.videoweb.model.entity.UserInfo;
import com.cjym.videoweb.model.entity.VipRecord;
import com.cjym.videoweb.model.event.VipOpenedEvent;
import com.cjym.videoweb.model.vo.VipOpenVo;
import com.cjym.videoweb.model.vo.VipPlanVo;
import com.cjym.videoweb.model.vo.VipPlansVo;
import com.cjym.videoweb.model.vo.VipRenewVo;
import com.cjym.videoweb.model.vo.VipStatusVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.EsSearchService;
import com.cjym.videoweb.service.MessageService;
import com.cjym.videoweb.service.VipService;
import com.cjym.videoweb.service.WebConfigService;
import com.cjym.videoweb.utils.JWTUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VipServiceImpl extends ServiceImpl<VipRecordMapper, VipRecord> implements VipService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private MessageService messageService;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Resource
    private WebConfigService webConfigService;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * ES 索引同步服务：仅 elasticsearch.enabled=true 时存在，未启用时为 null（埋点静默跳过）
     */
    @Resource
    private ObjectProvider<EsSearchService> esSearchServiceProvider;

    @Override
    public IPage<VipRecordPageDto> pageRecords(Integer page, Integer size, String userUid, String userName, Integer status) {
        Page<VipRecordPageDto> pageParam = new Page<>(page == null ? 1 : page, size == null ? 10 : size);
        return baseMapper.selectRecordPage(pageParam, userUid, userName, status);
    }

    @Override
    public VipRecordPageDto getDetail(Long id) {
        if (id == null) {
            throw new GlobeException("记录ID不能为空");
        }
        VipRecordPageDto detail = baseMapper.selectDetailById(id);
        if (detail == null) {
            throw new GlobeException("大会员记录不存在");
        }
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> openVip(VipOpenVo vo) {
        if (vo == null || StringUtils.isEmpty(vo.getUserUid())) {
            return R.error("用户uid不能为空");
        }
        // 时长与到期时间二选一必传
        if (vo.getDurationMonths() == null && vo.getExpireTime() == null) {
            return R.error("开通时长与到期时间至少传一个");
        }
        if (vo.getDurationMonths() != null
                && (vo.getDurationMonths() < 1 || vo.getDurationMonths() > VipConstant.MAX_DURATION_MONTHS)) {
            return R.error("开通时长必须在1~" + VipConstant.MAX_DURATION_MONTHS + "个月之间");
        }
        // 大会员仅对前台普通用户开通
        UserInfo user = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getUid, vo.getUserUid().trim())
                .eq(UserInfo::getType, BaseConstant.USER_TYPE_USER));
        if (user == null) {
            throw new GlobeException("前台用户不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        // 权益区间起点：已是大会员且未到期时从现有到期时间衔接，否则从当前时间起算
        LocalDateTime startTime = resolveStartTime(user.getVipExpireTime(), now);
        // 计算新到期时间：指定到期时间直接使用；按时长开通时从权益区间起点累加
        LocalDateTime newExpireTime = resolveExpireTime(vo.getExpireTime(), vo.getDurationMonths(), startTime, now);
        // 写入开通记录
        VipRecord record = new VipRecord();
        record.setUserId(user.getId());
        record.setDurationMonths(vo.getExpireTime() == null ? vo.getDurationMonths() : null);
        record.setStartTime(startTime);
        record.setExpireTime(newExpireTime);
        record.setStatus(VipConstant.STATUS_ACTIVE);
        record.setRemark(vo.getRemark());
        save(record);
        // 重新计算用户大会员状态：以所有生效记录的最大到期时间为准，避免覆盖其他更晚到期的记录
        recalibrateUserVip(user.getId());
        refreshUserEs(user.getId());
        return R.success("开通大会员成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> renewVip(VipRenewVo vo) {
        if (vo == null || vo.getId() == null) {
            return R.error("记录ID不能为空");
        }
        if (vo.getDurationMonths() == null && vo.getExpireTime() == null) {
            return R.error("续期时长与到期时间至少传一个");
        }
        if (vo.getDurationMonths() != null
                && (vo.getDurationMonths() < 1 || vo.getDurationMonths() > VipConstant.MAX_DURATION_MONTHS)) {
            return R.error("续期时长必须在1~" + VipConstant.MAX_DURATION_MONTHS + "个月之间");
        }
        VipRecord record = baseMapper.selectById(vo.getId());
        if (record == null) {
            return R.error("大会员记录不存在");
        }
        if (VipConstant.STATUS_EXPIRED == record.getStatus()) {
            return R.error("该记录已到期，请重新开通大会员");
        }
        if (VipConstant.STATUS_REVOKED == record.getStatus()) {
            return R.error("该记录已撤销，请重新开通大会员");
        }
        LocalDateTime now = LocalDateTime.now();
        // 续期基准：记录当前到期时间（已过期则从当前时间起算）
        LocalDateTime base = record.getExpireTime() != null && record.getExpireTime().isAfter(now)
                ? record.getExpireTime() : now;
        LocalDateTime newExpireTime = resolveExpireTime(vo.getExpireTime(), vo.getDurationMonths(), base, now);
        record.setExpireTime(newExpireTime);
        if (vo.getExpireTime() == null) {
            // 按时长续期：月数累加，保持记录展示的累计时长准确
            record.setDurationMonths(record.getDurationMonths() == null
                    ? vo.getDurationMonths()
                    : record.getDurationMonths() + vo.getDurationMonths());
        }
        if (vo.getRemark() != null) {
            record.setRemark(vo.getRemark());
        }
        updateById(record);
        // 重新计算用户大会员状态：以所有生效记录的最大到期时间为准，避免覆盖其他更晚到期的记录
        recalibrateUserVip(record.getUserId());
        refreshUserEs(record.getUserId());
        return R.success("续期成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> revokeVip(Long id) {
        if (id == null) {
            return R.error("记录ID不能为空");
        }
        VipRecord record = baseMapper.selectById(id);
        if (record == null) {
            return R.error("大会员记录不存在");
        }
        if (VipConstant.STATUS_REVOKED == record.getStatus()) {
            return R.error("该记录已撤销");
        }
        // 撤销：状态置为已撤销（记录保留便于管理端追溯，不物理删除）
        LambdaUpdateWrapper<VipRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(VipRecord::getId, id)
                .set(VipRecord::getStatus, VipConstant.STATUS_REVOKED);
        update(wrapper);
        // 联动调整下游生效记录：权益起点晚于被撤销记录到期时间的记录整体前移被撤销记录时长，
        // 避免撤销中间记录后用户权益未实际扣减
        int adjusted = shiftDownstreamRecords(record);
        // 重新计算该用户剩余生效中的记录，决定用户大会员状态
        recalibrateUserVip(record.getUserId());
        return adjusted > 0
                ? R.success("已撤销该大会员记录，并联动调整" + adjusted + "条后续开通记录")
                : R.success("已撤销该大会员记录");
    }

    @Override
    public VipStatusVo getCurrentUserVipStatus() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new GlobeException("请先登录");
        }
        UserInfo user = userInfoMapper.selectById(userId);
        if (user == null) {
            throw new GlobeException("用户不存在");
        }
        return buildVipStatus(user);
    }

    @Override
    public VipPlansVo getVipPlans() {
        VipPlansVo result = new VipPlansVo();
        String plansJson = webConfigService.getConfigValue(
                BaseConstant.WEB_CONFIG_CATEGORY_VIP, BaseConstant.WEB_CONFIG_KEY_VIP_PLANS);
        if (StringUtils.isEmpty(plansJson)) {
            result.setPlans(Collections.emptyList());
            result.setVipPricePerMonth(buildMonthlyPlan(getVipMonthlyPrice()));
            return result;
        }
        List<VipPlanVo> plans;
        try {
            plans = objectMapper.readValue(plansJson, new TypeReference<List<VipPlanVo>>() {
            });
        } catch (JsonProcessingException e) {
            log.warn("大会员套餐配置解析失败[value={}]，套餐列表置空", plansJson);
            result.setPlans(Collections.emptyList());
            result.setVipPricePerMonth(buildMonthlyPlan(getVipMonthlyPrice()));
            return result;
        }
        if (plans == null || plans.isEmpty()) {
            result.setPlans(Collections.emptyList());
            result.setVipPricePerMonth(buildMonthlyPlan(getVipMonthlyPrice()));
            return result;
        }
        // 缺少时长或现价的非法套餐项剔除，现价/原价统一保留 2 位小数
        List<VipPlanVo> validPlans = plans.stream()
                .filter(p -> p.getDurationMonths() != null && p.getPrice() != null)
                .peek(p -> {
                    p.setPrice(p.getPrice().setScale(2, RoundingMode.HALF_UP));
                    if (p.getOriginalPrice() != null) {
                        p.setOriginalPrice(p.getOriginalPrice().setScale(2, RoundingMode.HALF_UP));
                    }
                })
                .collect(Collectors.toList());
        // 单月套餐与套餐数据结构一致：优先取套餐列表中 1 个月套餐，无该套餐时按月单价配置合成
        VipPlanVo monthlyPlan = validPlans.stream()
                .filter(p -> Integer.valueOf(1).equals(p.getDurationMonths()))
                .findFirst()
                .orElseGet(() -> buildMonthlyPlan(getVipMonthlyPrice()));
        BigDecimal monthlyPrice = monthlyPlan.getPrice();
        // autoOrig=true 时原价按月单价自动计算
        validPlans.forEach(p -> {
            if (Boolean.TRUE.equals(p.getAutoOrig())) {
                p.setOriginalPrice(monthlyPrice.multiply(BigDecimal.valueOf(p.getDurationMonths()))
                        .setScale(2, RoundingMode.HALF_UP));
            }
        });
        result.setVipPricePerMonth(monthlyPlan);
        result.setPlans(validPlans);
        return result;
    }

    /**
     * 合成单月套餐（套餐配置中无 1 个月套餐时使用）：结构与套餐项一致，原价等于现价
     */
    private VipPlanVo buildMonthlyPlan(BigDecimal monthlyPrice) {
        VipPlanVo plan = new VipPlanVo();
        plan.setName("1月包");
        plan.setDurationMonths(1);
        BigDecimal price = monthlyPrice.setScale(2, RoundingMode.HALF_UP);
        plan.setPrice(price);
        plan.setOriginalPrice(price);
        return plan;
    }

    /**
     * 读取大会员月单价（web_config 配置，缺失或格式错误时兑底默认值）
     */
    private BigDecimal getVipMonthlyPrice() {
        String value = webConfigService.getConfigValue(
                BaseConstant.WEB_CONFIG_CATEGORY_VIP, BaseConstant.WEB_CONFIG_KEY_VIP_PRICE);
        if (StringUtils.isEmpty(value)) {
            return OrderConstant.DEFAULT_VIP_MONTHLY_PRICE;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            log.warn("大会员月单价配置格式错误[value={}]，使用默认值{}", value, OrderConstant.DEFAULT_VIP_MONTHLY_PRICE);
            return OrderConstant.DEFAULT_VIP_MONTHLY_PRICE;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void openVipForOrder(Long userId, Integer durationMonths, Long orderId) {
        if (userId == null || durationMonths == null || durationMonths < 1) {
            throw new GlobeException("开通参数不合法");
        }
        // 行锁读取：防止两次支付的异步通知并发处理时读到旧的到期时间，导致时长互相覆盖
        UserInfo user = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getId, userId)
                .last("FOR UPDATE"));
        if (user == null) {
            throw new GlobeException("用户不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        // 权益区间起点：已是大会员且未到期时从现有到期时间衔接，否则从当前时间起算
        LocalDateTime startTime = resolveStartTime(user.getVipExpireTime(), now);
        // 已是大会员的用户从现有到期时间累加，否则从当前时间起算
        LocalDateTime newExpireTime = resolveExpireTime(null, durationMonths, startTime, now);
        // 写入开通记录
        VipRecord record = new VipRecord();
        record.setUserId(userId);
        record.setOrderId(orderId);
        record.setDurationMonths(durationMonths);
        record.setStartTime(startTime);
        record.setExpireTime(newExpireTime);
        record.setStatus(VipConstant.STATUS_ACTIVE);
        record.setRemark("大会员充值" + durationMonths + "个月（订单支付）");
        save(record);
        // 更新用户大会员状态并同步 ES
        updateUserVip(userId, true, newExpireTime);
        refreshUserEs(userId);
        // 发布开通成功事件：站内消息通知等后续动作由监听器异步处理，与开通主流程解耦
        eventPublisher.publishEvent(new VipOpenedEvent(userId, durationMonths, newExpireTime));
    }

    @Override
    public int processExpiredVips() {
        LocalDateTime now = LocalDateTime.now();
        // 查询已过期但未失效的大会员用户
        List<UserInfo> expiredUsers = userInfoMapper.selectList(new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getIsVip, true)
                .le(UserInfo::getVipExpireTime, now));
        if (expiredUsers.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (UserInfo user : expiredUsers) {
            try {
                // 1. 用户大会员失效
                updateUserVip(user.getId(), false, null);
                // 2. 生效中的开通记录置为已到期
                LambdaUpdateWrapper<VipRecord> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(VipRecord::getUserId, user.getId())
                        .eq(VipRecord::getStatus, VipConstant.STATUS_ACTIVE)
                        .set(VipRecord::getStatus, VipConstant.STATUS_EXPIRED);
                update(wrapper);
                // 3. 站内消息通知
                MessageInfo message = new MessageInfo();
                message.setUserId(user.getId());
                message.setType(MessageConstant.TYPE_VIP_EXPIRE);
                message.setTitle(MessageConstant.TITLE_VIP_EXPIRE);
                message.setContent(MessageConstant.CONTENT_VIP_EXPIRE);
                messageService.create(message);
                // 4. 同步 ES
                refreshUserEs(user.getId());
                count++;
            } catch (Exception e) {
                log.error("大会员到期处理失败[userId={}]", user.getId(), e);
            }
        }
        log.info("大会员到期处理完成[到期用户数={}]", count);
        return count;
    }

    /**
     * 计算权益区间起点：已有未过期的到期时间时从现有到期时间衔接，否则从当前时间起算
     */
    private LocalDateTime resolveStartTime(LocalDateTime base, LocalDateTime now) {
        return base != null && base.isAfter(now) ? base : now;
    }

    /**
     * 计算新到期时间：指定到期时间直接使用（必须晚于当前时间且不得早于当前生效到期时间），
     * 否则按基准时间累加月数
     */
    private LocalDateTime resolveExpireTime(LocalDateTime expireTime, Integer durationMonths,
                                            LocalDateTime base, LocalDateTime now) {
        if (expireTime != null) {
            if (!expireTime.isAfter(now)) {
                throw new GlobeException("到期时间必须晚于当前时间");
            }
            // 防止到期时间被改早导致用户权益缩短：指定到期时间不得早于当前生效的到期时间
            if (base != null && base.isAfter(now) && expireTime.isBefore(base)) {
                throw new GlobeException("到期时间不能早于当前到期时间");
            }
            return expireTime;
        }
        LocalDateTime effectiveBase = base != null && base.isAfter(now) ? base : now;
        return effectiveBase.plusMonths(durationMonths);
    }

    /**
     * 撤销记录后联动调整其下游生效记录：将权益区间起点不早于被撤销记录到期时间的生效记录
     * 整体前移被撤销记录的权益时长（按天数精确平移，保持权益链条连续），
     * 前移后已过期的下游记录直接置为已到期；返回联动调整的记录条数
     */
    private int shiftDownstreamRecords(VipRecord revoked) {
        // 权益区间起点：历史记录未回填时退回创建时间近似
        LocalDateTime revokedStart = revoked.getStartTime() != null ? revoked.getStartTime() : revoked.getCreateTime();
        if (revokedStart == null || revoked.getExpireTime() == null) {
            return 0;
        }
        long durationDays = ChronoUnit.DAYS.between(revokedStart, revoked.getExpireTime());
        if (durationDays <= 0) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        // 下游记录：同一用户、生效中、权益区间起点不早于被撤销记录到期时间（依赖链）
        List<VipRecord> downstream = baseMapper.selectList(new LambdaQueryWrapper<VipRecord>()
                .eq(VipRecord::getUserId, revoked.getUserId())
                .eq(VipRecord::getStatus, VipConstant.STATUS_ACTIVE)
                .ge(VipRecord::getStartTime, revoked.getExpireTime()));
        int adjusted = 0;
        for (VipRecord item : downstream) {
            if (item.getStartTime() == null || item.getExpireTime() == null) {
                continue;
            }
            item.setStartTime(item.getStartTime().minusDays(durationDays));
            item.setExpireTime(item.getExpireTime().minusDays(durationDays));
            if (!item.getExpireTime().isAfter(now)) {
                // 前移后已过期的下游记录直接置为已到期，避免悬空的生效记录
                LambdaUpdateWrapper<VipRecord> expireWrapper = new LambdaUpdateWrapper<>();
                expireWrapper.eq(VipRecord::getId, item.getId())
                        .set(VipRecord::getStartTime, item.getStartTime())
                        .set(VipRecord::getExpireTime, item.getExpireTime())
                        .set(VipRecord::getStatus, VipConstant.STATUS_EXPIRED);
                update(expireWrapper);
            } else {
                updateById(item);
            }
            adjusted++;
        }
        return adjusted;
    }

    /**
     * 更新用户大会员状态（is_vip 与到期时间）
     */
    private void updateUserVip(Long userId, boolean isVip, LocalDateTime expireTime) {
        LambdaUpdateWrapper<UserInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserInfo::getId, userId)
                .set(UserInfo::getIsVip, isVip)
                .set(UserInfo::getVipExpireTime, expireTime);
        userInfoMapper.update(null, wrapper);
    }

    /**
     * 重新计算用户大会员状态：取该用户生效中且未到期的记录的最大到期时间，
     * 无有效记录时取消大会员
     */
    private void recalibrateUserVip(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<VipRecord> activeRecords = baseMapper.selectList(new LambdaQueryWrapper<VipRecord>()
                .eq(VipRecord::getUserId, userId)
                .eq(VipRecord::getStatus, VipConstant.STATUS_ACTIVE)
                .gt(VipRecord::getExpireTime, now));
        LocalDateTime maxExpireTime = activeRecords.stream()
                .map(VipRecord::getExpireTime)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        updateUserVip(userId, maxExpireTime != null, maxExpireTime);
    }

    /**
     * 组装前台大会员状态（剩余天数按到期时间向上取整）
     */
    private VipStatusVo buildVipStatus(UserInfo user) {
        VipStatusVo vo = new VipStatusVo();
        boolean validVip = Boolean.TRUE.equals(user.getIsVip())
                && user.getVipExpireTime() != null
                && user.getVipExpireTime().isAfter(LocalDateTime.now());
        vo.setIsVip(validVip);
        vo.setVipExpireTime(validVip ? user.getVipExpireTime() : null);
        long remainDays = 0;
        if (validVip) {
            Duration duration = Duration.between(LocalDateTime.now(), user.getVipExpireTime());
            remainDays = (duration.toMinutes() + 24L * 60 - 1) / (24L * 60);
        }
        vo.setRemainDays(remainDays);
        return vo;
    }

    /**
     * 从请求头解析当前登录用户id（未携带合法 token 时返回 null）
     */
    private Long getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            HttpServletRequest request = attributes.getRequest();
            String token = request.getHeader("authorization");
            if (StringUtils.isEmpty(token)) {
                return null;
            }
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            return JWTUtil.getUserId(token);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 用户大会员状态变更后同步 ES 索引（ES 未装配或同步失败时静默忽略，不影响主流程）
     */
    private void refreshUserEs(Long userId) {
        EsSearchService esSearchService = esSearchServiceProvider.getIfAvailable();
        if (esSearchService == null) {
            return;
        }
        try {
            esSearchService.refreshUser(userId);
        } catch (Exception e) {
            log.warn("同步用户到 ES 失败 userId={}: {}", userId, e.getMessage());
        }
    }
}
