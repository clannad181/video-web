package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cjym.videoweb.constant.BaseConstant;
import com.cjym.videoweb.constant.CommentConstant;
import com.cjym.videoweb.constant.MessageConstant;
import com.cjym.videoweb.constant.ReportConstant;
import com.cjym.videoweb.constant.VideoConstant;
import com.cjym.videoweb.mapper.CommentMapper;
import com.cjym.videoweb.mapper.ReportMapper;
import com.cjym.videoweb.mapper.VideoEpisodeMapper;
import com.cjym.videoweb.mapper.VideoMapper;
import com.cjym.videoweb.model.dto.ReportHandleDto;
import com.cjym.videoweb.model.entity.Comment;
import com.cjym.videoweb.model.entity.MessageInfo;
import com.cjym.videoweb.model.entity.Report;
import com.cjym.videoweb.model.entity.Video;
import com.cjym.videoweb.model.entity.VideoEpisode;
import com.cjym.videoweb.model.vo.CommentVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 消息通知组装器
 * <p>
 * 将 @NotifyMessage 切面传入的业务对象按消息类型组装为消息实体：
 * - 视频发布成功 / 举报下架：target 为视频实体，直接提取 UP 主、标题、业务标识；
 * - 剧集审核：target 为所属视频实体，剧集ID与审核状态从方法参数中按类型提取；
 * - 评论通知：target 为评论 VO（发表场景）或从参数提取评论ID与审核状态（审核通过场景），通知视频 UP 主；
 * - 举报处理结果：从参数提取举报ID，通知举报人。
 * <p>
 * 组装失败（对象为空、接收者缺失、状态不满足、通知开关关闭）返回 null，切面将跳过发送。
 * 消息模板统一取自 MessageConstant，业务侧无需关心消息组装细节。
 */
@Component
@Slf4j
public class NotifyMessageAssembler {

    @Resource
    private VideoEpisodeMapper videoEpisodeMapper;

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private ReportMapper reportMapper;

    @Resource
    private WebConfigService webConfigService;
    /**
     * 按消息类型组装消息实体，无法组装时返回 null
     *
     * @param type   消息类型（MessageConstant.TYPE_*）
     * @param target 消息数据来源对象（注解 target SpEL 的解析结果，通常为方法返回值）
     * @param args   方法参数（参数名 → 参数值）
     */
    public MessageInfo assemble(int type, Object target, Map<String, Object> args) {
        return switch (type) {
            case MessageConstant.TYPE_VIDEO_PUBLISHED -> buildVideoPublished(target);
            case MessageConstant.TYPE_VIDEO_REVIEW -> buildVideoReview(target, args);
            case MessageConstant.TYPE_VIDEO_TAKEDOWN -> buildVideoTakedown(target);
            case MessageConstant.TYPE_COMMENT -> buildCommentNotify(target, args);
            case MessageConstant.TYPE_REPORT -> buildReportNotify(args);
            default -> {
                log.warn("未知消息类型，跳过发送[type={}]", type);
                yield null;
            }
        };
    }

    /**
     * 视频发布成功：target 为视频实体，仅已发布（免审直接发布）且存在 UP 主时发送；
     * playLink 指向该视频最新审核通过剧集（无审核通过剧集时回退最新剧集）
     */
    private MessageInfo buildVideoPublished(Object target) {
        if (!(target instanceof Video video)|| video.getId()==null) {
            return null;
        }
        video = videoMapper.selectById(video.getId());
        if (video == null || video.getUserId() == null
                || !java.util.Objects.equals(video.getVideoStatus(), VideoConstant.VIDEO_STATUS_PUBLISHED)) {
            return null;
        }

        MessageInfo message = build(video.getUserId(), MessageConstant.TYPE_VIDEO_PUBLISHED,
                MessageConstant.TITLE_VIDEO_PUBLISHED,
                String.format(MessageConstant.CONTENT_VIDEO_PUBLISHED, video.getTitle()),
                video.getId(), video.getAvid());
        message.setPlayLink(buildPlayLink(video, findLatestEpisode(video.getId())));
        return message;
    }

    /**
     * 剧集审核结果：target 为所属视频实体（方法仅在审核通过/拒绝时返回），
     * 剧集ID与审核状态从方法参数中按类型提取（Long=剧集ID，Integer=审核状态）；
     * 网站配置 notification/videoReviewNotify 关闭时静默跳过（不发送通知）；
     * playLink 指向该剧集
     */
    private MessageInfo buildVideoReview(Object target, Map<String, Object> args) {
        // 审核通知开关关闭时不发送通知
        if (!webConfigService.getConfigBoolean(BaseConstant.WEB_CONFIG_CATEGORY_NOTIFICATION,
                BaseConstant.WEB_CONFIG_KEY_VIDEO_REVIEW_NOTIFY)) {
            return null;
        }
        if (!(target instanceof Video video) || video.getUserId() == null) {
            return null;
        }
        Integer reviewStatus = findArg(args, Integer.class);
        if (reviewStatus == null || (reviewStatus != VideoConstant.REVIEW_APPROVED
                && reviewStatus != VideoConstant.REVIEW_REJECTED)) {
            return null;
        }
        Long episodeId = findArg(args, Long.class);
        VideoEpisode episode = episodeId != null ? videoEpisodeMapper.selectById(episodeId) : null;
        if (episode == null) {
            return null;
        }
        int episodeNumber = episode.getEpisodeNumber() != null ? episode.getEpisodeNumber() : 0;
        String content = reviewStatus == VideoConstant.REVIEW_APPROVED
                ? String.format(MessageConstant.CONTENT_VIDEO_REVIEW_APPROVED, video.getTitle(), episodeNumber)
                : String.format(MessageConstant.CONTENT_VIDEO_REVIEW_REJECTED, video.getTitle(), episodeNumber);
        MessageInfo message = build(video.getUserId(), MessageConstant.TYPE_VIDEO_REVIEW,
                MessageConstant.TITLE_VIDEO_REVIEW, content, episode.getId(), episode.getAvid());
        message.setPlayLink(buildPlayLink(video, episode));
        return message;
    }

    /**
     * 稿件举报下架：target 为已下架的视频实体（方法仅在受理下架时返回）；
     * playLink 指向该视频最新审核通过剧集（无审核通过剧集时回退最新剧集）
     */
    private MessageInfo buildVideoTakedown(Object target) {
        if (!(target instanceof Video video) || video.getUserId() == null) {
            return null;
        }
        MessageInfo message = build(video.getUserId(), MessageConstant.TYPE_VIDEO_TAKEDOWN,
                MessageConstant.TITLE_VIDEO_TAKEDOWN,
                String.format(MessageConstant.CONTENT_VIDEO_TAKEDOWN, video.getTitle()),
                video.getId(), video.getAvid());
        message.setPlayLink(buildPlayLink(video, findLatestEpisode(video.getId())));
        return message;
    }

    /**
     * 评论通知：通知视频 UP 主收到新评论。
     * - target 为评论 VO（前台发表评论）：待审核状态不通知（审核通过时由审核场景通知）；
     * - target 为空（评论审核通过场景）：从参数提取审核状态（0-通过）与评论ID，仅通过时通知。
     * 评论者评论自己的视频不通知；playLink 指向评论所属剧集（视频级历史评论回退最新剧集）。
     */
    private MessageInfo buildCommentNotify(Object target, Map<String, Object> args) {
        if (!webConfigService.getConfigBoolean(BaseConstant.WEB_CONFIG_CATEGORY_NOTIFICATION,
                BaseConstant.WEB_CONFIG_KEY_COMMENT_NOTIFY)) {
            return null;
        }
        Long videoId;
        Long commentUserId;
        Long episodeId;
        Long commentId;
        if (target instanceof CommentVo vo) {
            // 发表场景：评论进入待审核时不通知，审核通过时由 review 场景触发通知
            if (vo.getStatus() != null && vo.getStatus() == CommentConstant.STATUS_PENDING_REVIEW) {
                return null;
            }
            videoId = vo.getVideoId();
            commentUserId = vo.getUserId();
            episodeId = vo.getEpisodeId();
            commentId = vo.getId();
        } else {
            // 审核场景：仅审核通过（reviewStatus=0）时通知
            Integer reviewStatus = findArg(args, Integer.class);
            if (reviewStatus == null || reviewStatus != CommentConstant.STATUS_NORMAL) {
                return null;
            }
            Long id = findArg(args, Long.class);
            Comment comment = id != null ? commentMapper.selectById(id) : null;
            if (comment == null) {
                return null;
            }
            videoId = comment.getVideoId();
            commentUserId = comment.getUserId();
            episodeId = comment.getEpisodeId();
            commentId = comment.getId();
        }
        Video video = videoId != null ? videoMapper.selectById(videoId) : null;
        if (video == null || video.getUserId() == null) {
            return null;
        }
        // 评论自己的视频不通知
        if (video.getUserId().equals(commentUserId)) {
            return null;
        }
        // 视频级历史评论（无剧集归属）回退到最新剧集
        VideoEpisode episode = episodeId != null ? videoEpisodeMapper.selectById(episodeId) : null;
        if (episode == null) {
            episode = findLatestEpisode(video.getId());
        }
        MessageInfo message = build(video.getUserId(), MessageConstant.TYPE_COMMENT,
                MessageConstant.TITLE_COMMENT_NOTIFY,
                String.format(MessageConstant.CONTENT_COMMENT_NOTIFY, video.getTitle()),
                commentId, video.getAvid());
        message.setPlayLink(buildPlayLink(video, episode));
        return message;
    }

    /**
     * 举报处理结果通知：通知举报人（受理/驳回均通知），从方法参数提取举报ID后查库获取举报人与处理结果；
     * playLink 指向该视频最新审核通过剧集（无审核通过剧集时回退最新剧集）
     */
    private MessageInfo buildReportNotify(Map<String, Object> args) {
        if (!webConfigService.getConfigBoolean(BaseConstant.WEB_CONFIG_CATEGORY_NOTIFICATION,
                BaseConstant.WEB_CONFIG_KEY_REPORT_NOTIFY)) {
            return null;
        }
        ReportHandleDto dto = findArg(args, ReportHandleDto.class);
        Report report = dto != null && dto.getReportId() != null ? reportMapper.selectById(dto.getReportId()) : null;
        if (report == null || report.getReporterId() == null) {
            return null;
        }
        Video video = videoMapper.selectById(report.getVideoId());
        if (video == null) {
            return null;
        }
        boolean accepted = report.getStatus() != null && report.getStatus() == ReportConstant.STATUS_ACCEPTED;
        String content = String.format(accepted ? MessageConstant.CONTENT_REPORT_ACCEPTED
                : MessageConstant.CONTENT_REPORT_REJECTED, video.getTitle());
        MessageInfo message = build(report.getReporterId(), MessageConstant.TYPE_REPORT,
                MessageConstant.TITLE_REPORT_NOTIFY, content, report.getId(), video.getAvid());
        message.setPlayLink(buildPlayLink(video, findLatestEpisode(video.getId())));
        return message;
    }

    /**
     * 查询视频最新审核通过剧集（无审核通过剧集时回退到最新剧集），无任何剧集时返回 null
     */
    private VideoEpisode findLatestEpisode(Long videoId) {
        List<VideoEpisode> episodes = videoEpisodeMapper.selectList(new LambdaQueryWrapper<VideoEpisode>()
                .eq(VideoEpisode::getVideoId, videoId)
                .orderByDesc(VideoEpisode::getEpisodeNumber));
        if (episodes.isEmpty()) {
            return null;
        }
        return episodes.stream()
                .filter(e -> java.util.Objects.equals(e.getReviewStatus(), VideoConstant.REVIEW_APPROVED))
                .findFirst()
                .orElse(episodes.get(0));
    }

    /**
     * 拼接播放跳转链接（视频或剧集缺失时返回 null）：/client/video/{videoAvid}/episode/{episodeAvid}
     */
    private String buildPlayLink(Video video, VideoEpisode episode) {
        if (video == null || episode == null || video.getAvid() == null || episode.getAvid() == null) {
            return null;
        }
        return "/client/video/" + video.getAvid() + "/episode/" + episode.getAvid();
    }

    /**
     * 构建基础消息实体（avid 与已读状态由 MessageService 落库时统一补齐）
     */
    private MessageInfo build(Long userId, int type, String title, String content, Long bizId, String bizAvid) {
        MessageInfo message = new MessageInfo();
        message.setUserId(userId);
        message.setType(type);
        message.setTitle(title);
        message.setContent(content);
        message.setBizId(bizId);
        message.setBizAvid(bizAvid);
        return message;
    }

    /**
     * 从方法参数中按类型查找第一个匹配值（不依赖参数名，规避 -parameters 编译参数影响）
     */
    private <T> T findArg(Map<String, Object> args, Class<T> clazz) {
        for (Object value : args.values()) {
            if (clazz.isInstance(value)) {
                return clazz.cast(value);
            }
        }
        return null;
    }
}
