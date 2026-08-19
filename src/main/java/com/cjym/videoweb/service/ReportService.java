package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.dto.ReportCreateDto;
import com.cjym.videoweb.model.dto.ReportHandleDto;
import com.cjym.videoweb.model.entity.Report;
import com.cjym.videoweb.model.vo.ReportVo;

public interface ReportService extends IService<Report> {

    /**
     * 客户端举报稿件（基于视频avid与剧集avid标识）
     */
    void createClient(ReportCreateDto dto);

    /**
     * 管理端分页查询举报列表，支持按状态、举报类型、视频标题、举报人昵称筛选
     */
    IPage<ReportVo> pageReportList(Integer page, Integer size, Integer status,
                                   Integer reportType, String videoTitle, String reporterNickName);

    /**
     * 管理端处理举报（复核）：
     * 已受理（确认违规）→ 举报状态=1 且视频下架（video_status=3）；
     * 已驳回（未违规）→ 举报状态=2，视频不变
     */
    void handle(ReportHandleDto dto);
}
