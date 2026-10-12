package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.model.dto.VipRecordPageDto;
import com.cjym.videoweb.model.entity.VipRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VipRecordMapper extends BaseMapper<VipRecord> {

    /**
     * 大会员开通记录分页查询（关联用户信息，支持按用户uid/用户名模糊、状态筛选）
     */
    IPage<VipRecordPageDto> selectRecordPage(Page<VipRecordPageDto> page,
                                             @Param("userUid") String userUid,
                                             @Param("userName") String userName,
                                             @Param("status") Integer status);

    /**
     * 按记录ID查询详情（关联用户信息）
     */
    VipRecordPageDto selectDetailById(@Param("id") Long id);
}
