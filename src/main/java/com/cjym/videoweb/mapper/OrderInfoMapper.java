package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.model.dto.OrderPageDto;
import com.cjym.videoweb.model.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    /**
     * 订单分页查询（关联用户信息，支持按用户ID/用户uid/订单号/业务类型/状态筛选）
     */
    IPage<OrderPageDto> selectOrderPage(Page<OrderPageDto> page,
                                        @Param("userId") Long userId,
                                        @Param("userUid") String userUid,
                                        @Param("orderNo") String orderNo,
                                        @Param("bizType") Integer bizType,
                                        @Param("status") Integer status);

    /**
     * 按订单号查询详情（关联用户信息）
     */
    OrderPageDto selectDetailByOrderNo(@Param("orderNo") String orderNo);
}
