package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.dto.VipRecordPageDto;
import com.cjym.videoweb.model.entity.VipRecord;
import com.cjym.videoweb.model.vo.VipOpenVo;
import com.cjym.videoweb.model.vo.VipPlansVo;
import com.cjym.videoweb.model.vo.VipRenewVo;
import com.cjym.videoweb.model.vo.VipStatusVo;
import com.cjym.videoweb.result.R;

public interface VipService extends IService<VipRecord> {

    /**
     * 查询大会员充值套餐与单月套餐（解析 web_config 套餐配置，autoOrig=true 时原价按月单价自动计算；
     * 单月套餐优先取套餐列表中 1 个月套餐，无该套餐时按月单价配置合成，结构与套餐项一致）
     */
    VipPlansVo getVipPlans();

    /**
     * 大会员开通记录分页查询（管理端）
     */
    IPage<VipRecordPageDto> pageRecords(Integer page, Integer size, String userUid, String userName, Integer status);

    /**
     * 按记录ID查询详情（管理端）
     */
    VipRecordPageDto getDetail(Long id);

    /**
     * 开通大会员（管理端，创建记录并更新用户大会员状态）
     */
    R<String> openVip(VipOpenVo vo);

    /**
     * 续期/修改到期时间（管理端）
     */
    R<String> renewVip(VipRenewVo vo);

    /**
     * 撤销大会员（管理端，逻辑删除记录并重新计算用户大会员状态）
     */
    R<String> revokeVip(Long id);

    /**
     * 查询当前登录前台用户的大会员状态
     */
    VipStatusVo getCurrentUserVipStatus();

    /**
     * 支付成功后开通大会员（供支付回调调用）：写入开通记录并更新用户大会员状态，
     * 用户已是大会员时在现有到期时间上累加
     */
    void openVipForOrder(Long userId, Integer durationMonths, Long orderId);

    /**
     * 处理已到期的大会员（定时任务调用，返回处理用户数）
     */
    int processExpiredVips();
}
