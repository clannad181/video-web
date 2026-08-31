package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.constant.ReportConstant;
import com.cjym.videoweb.constant.VideoConstant;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.ReportMapper;
import com.cjym.videoweb.mapper.VideoEpisodeMapper;
import com.cjym.videoweb.mapper.VideoMapper;
import com.cjym.videoweb.model.dto.ReportCreateDto;
import com.cjym.videoweb.model.dto.ReportHandleDto;
import com.cjym.videoweb.model.entity.Report;
import com.cjym.videoweb.model.entity.Video;
import com.cjym.videoweb.model.entity.VideoEpisode;
import com.cjym.videoweb.model.vo.ReportVo;
import com.cjym.videoweb.service.ReportService;
import com.cjym.videoweb.utils.JWTUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private VideoEpisodeMapper videoEpisodeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createClient(ReportCreateDto dto) {
        if (dto.getVideoUid() == null || dto.getVideoUid().isBlank()) {
            throw new GlobeException("视频avid不能为空");
        }
        if (dto.getReportType() == null || dto.getReportType() < ReportConstant.TYPE_MIN
                || dto.getReportType() > ReportConstant.TYPE_MAX) {
            throw new GlobeException("举报类型无效");
        }
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        // 按 avid 解析视频
        Video video = videoMapper.selectOne(new LambdaQueryWrapper<Video>().eq(Video::getAvid, dto.getVideoUid()));
        if (video == null) {
            throw new GlobeException("视频不存在");
        }
        // 解析剧集（可选）：传入时校验剧集存在且属于该视频
        Long episodeId = null;
        if (dto.getEpisodeUid() != null && !dto.getEpisodeUid().isBlank()) {
            VideoEpisode episode = videoEpisodeMapper.selectOne(
                    new LambdaQueryWrapper<VideoEpisode>().eq(VideoEpisode::getAvid, dto.getEpisodeUid()));
            if (episode == null || !episode.getVideoId().equals(video.getId())) {
                throw new GlobeException("剧集不存在或不属于该视频");
            }
            episodeId = episode.getId();
        }
        // 校验重复举报：同一用户对同一目标仅允许一条待处理举报
        LambdaQueryWrapper<Report> dupWrapper = new LambdaQueryWrapper<>();
        dupWrapper.eq(Report::getVideoId, video.getId())
                .eq(Report::getEpisodeId, episodeId)
                .eq(Report::getReporterId, currentUserId)
                .eq(Report::getStatus, ReportConstant.STATUS_PENDING);
        if (baseMapper.selectCount(dupWrapper) > 0) {
            throw new GlobeException("您已举报过该稿件，请等待处理结果");
        }
        Report report = new Report();
        report.setVideoId(video.getId());
        report.setEpisodeId(episodeId);
        report.setReporterId(currentUserId);
        report.setReportType(dto.getReportType());
        report.setReason(dto.getReason());
        report.setStatus(ReportConstant.STATUS_PENDING);
        baseMapper.insert(report);
    }

    @Override
    public IPage<ReportVo> pageReportList(Integer page, Integer size, Integer status,
                                          Integer reportType, String videoTitle, String reporterNickName) {
        Page<ReportVo> pageParam = new Page<>(page, size);
        return baseMapper.selectReportPage(pageParam, status, reportType, videoTitle, reporterNickName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(ReportHandleDto dto) {
        if (dto.getReportId() == null) {
            throw new GlobeException("举报ID不能为空");
        }
        if (dto.getHandleResult() == null
                || (dto.getHandleResult() != ReportConstant.STATUS_ACCEPTED
                && dto.getHandleResult() != ReportConstant.STATUS_REJECTED)) {
            throw new GlobeException("处理结果无效，仅允许 1-已受理 或 2-已驳回");
        }
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        Report report = baseMapper.selectById(dto.getReportId());
        if (report == null) {
            throw new GlobeException("举报记录不存在");
        }
        if (report.getStatus() != ReportConstant.STATUS_PENDING) {
            throw new GlobeException("该举报已处理，请勿重复操作");
        }
        // 更新举报处理信息
        report.setStatus(dto.getHandleResult());
        report.setHandlerId(currentUserId);
        report.setHandlerNote(dto.getHandlerNote());
        report.setHandleTime(LocalDateTime.now());
        baseMapper.updateById(report);
        // 确认违规（已受理）→ 已发布的视频下架（video_status=3）
        if (dto.getHandleResult() == ReportConstant.STATUS_ACCEPTED) {
            Video video = videoMapper.selectById(report.getVideoId());
            if (video != null && video.getVideoStatus() != null
                    && video.getVideoStatus() == VideoConstant.VIDEO_STATUS_PUBLISHED) {
                video.setVideoStatus(3);  // 已下架
                videoMapper.updateById(video);
            }
        }
    }

    /**
     * 从请求 JWT token 中获取当前用户ID
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
}
