package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.model.entity.Report;
import com.cjym.videoweb.model.vo.ReportVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReportMapper extends BaseMapper<Report> {

    /**
     * 管理端分页查询举报列表（JOIN 视频/剧集/用户信息），支持按状态、举报类型筛选
     */
    IPage<ReportVo> selectReportPage(Page<ReportVo> pageParam,
                                     @Param("status") Integer status,
                                     @Param("reportType") Integer reportType,
                                     @Param("videoTitle") String videoTitle,
                                     @Param("reporterNickName") String reporterNickName);
}
