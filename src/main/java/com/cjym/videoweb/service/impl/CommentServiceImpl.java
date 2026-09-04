package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.constant.BaseConstant;
import com.cjym.videoweb.constant.CommentConstant;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.CommentLikeMapper;
import com.cjym.videoweb.mapper.CommentMapper;
import com.cjym.videoweb.mapper.UserInfoMapper;
import com.cjym.videoweb.mapper.VideoEpisodeMapper;
import com.cjym.videoweb.mapper.VideoMapper;
import com.cjym.videoweb.model.dto.CommentClientSaveDto;
import com.cjym.videoweb.model.dto.CommentSaveDto;
import com.cjym.videoweb.model.entity.Comment;
import com.cjym.videoweb.model.entity.CommentLike;
import com.cjym.videoweb.model.entity.UserInfo;
import com.cjym.videoweb.model.entity.Video;
import com.cjym.videoweb.model.entity.VideoEpisode;
import com.cjym.videoweb.model.vo.DailyIncrementVo;
import com.cjym.videoweb.model.vo.CommentStatusStatsVo;
import com.cjym.videoweb.model.vo.CommentVo;
import com.cjym.videoweb.model.vo.EpisodeCommentCountVo;
import com.cjym.videoweb.service.CommentService;
import com.cjym.videoweb.service.WebConfigService;
import com.cjym.videoweb.utils.JWTUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Resource
    private CommentLikeMapper commentLikeMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private WebConfigService webConfigService;

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private VideoEpisodeMapper videoEpisodeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVo create(CommentSaveDto dto) {
        if (dto.getVideoId() == null) {
            throw new GlobeException("视频ID不能为空");
        }
        if (dto.getContent() == null || dto.getContent().isBlank()) {
            throw new GlobeException("评论内容不能为空");
        }
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }

        Comment comment = new Comment();
        comment.setVideoId(dto.getVideoId());
        comment.setEpisodeId(dto.getEpisodeId());
        comment.setUserId(currentUserId);
        comment.setContent(dto.getContent().trim());

        // 确定 parentId 和 rootId
        Long parentId = dto.getParentId() != null && dto.getParentId() != 0 ? dto.getParentId() : 0L;
        comment.setParentId(parentId);

        // 根据 comment_review 配置决定评论状态：开启审核则进入待审核，否则直接正常发布
        comment.setStatus(isCommentAutoReviewEnabled() ? CommentConstant.STATUS_PENDING_REVIEW : CommentConstant.STATUS_NORMAL );

        if (parentId == 0L) {
            // 一级评论：root_id 先占位，insert 后回写
            baseMapper.insert(comment);
            comment.setRootId(comment.getId());
            baseMapper.updateById(comment);
        } else {
            // 回复评论：查找父评论获取 root_id
            Comment parentComment = baseMapper.selectById(parentId);
            if (parentComment == null) {
                throw new GlobeException("父评论不存在");
            }
            // 回复继承父评论的剧集归属
            comment.setEpisodeId(parentComment.getEpisodeId());
            comment.setRootId(parentComment.getRootId() != 0 ? parentComment.getRootId() : parentComment.getId());
            baseMapper.insert(comment);

            // 更新父评论的 replyCount
            parentComment.setReplyCount(parentComment.getReplyCount() + 1);
            baseMapper.updateById(parentComment);
        }

        return toCommentVo(comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVo createClient(CommentClientSaveDto dto) {
        if (dto.getVideoUid() == null || dto.getVideoUid().isBlank()) {
            throw new GlobeException("视频avid不能为空");
        }
        if (dto.getEpisodeUid() == null || dto.getEpisodeUid().isBlank()) {
            throw new GlobeException("剧集avid不能为空");
        }
        if (dto.getContent() == null || dto.getContent().isBlank()) {
            throw new GlobeException("评论内容不能为空");
        }
        // 按 avid 解析视频与剧集
        Video video = videoMapper.selectOne(new LambdaQueryWrapper<Video>().eq(Video::getAvid, dto.getVideoUid()));
        if (video == null) {
            throw new GlobeException("视频不存在");
        }
        VideoEpisode episode = videoEpisodeMapper.selectOne(new LambdaQueryWrapper<VideoEpisode>().eq(VideoEpisode::getAvid, dto.getEpisodeUid()));
        if (episode == null || !episode.getVideoId().equals(video.getId())) {
            throw new GlobeException("剧集不存在或不属于该视频");
        }
        // 回复校验：父评论必须存在且属于该视频，防止跨视频伪造回复
        if (dto.getParentId() != null && dto.getParentId() != 0L) {
            Comment parentComment = baseMapper.selectById(dto.getParentId());
            if (parentComment == null) {
                throw new GlobeException("父评论不存在");
            }
            if (!parentComment.getVideoId().equals(video.getId())) {
                throw new GlobeException("父评论不属于该视频");
            }
        }
        // 组装内部 DTO 复用现有发表逻辑
        CommentSaveDto saveDto = new CommentSaveDto();
        saveDto.setVideoId(video.getId());
        saveDto.setEpisodeId(episode.getId());
        saveDto.setParentId(dto.getParentId());
        saveDto.setContent(dto.getContent());
        CommentVo vo = create(saveDto);
        // 填充评论者昵称与头像，便于前端发布后直接上屏展示
        UserInfo currentUser = userInfoMapper.selectById(vo.getUserId());
        if (currentUser != null) {
            vo.setNickName(currentUser.getNikeName());
            vo.setAvatar(currentUser.getAvatar());
        }
        return vo;
    }

    @Override
    public void deleteByUser(Long commentId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        Comment comment = baseMapper.selectById(commentId);
        if (comment == null) {
            throw new GlobeException("评论不存在");
        }
        if (!comment.getUserId().equals(currentUserId)) {
            throw new GlobeException("只能删除自己的评论");
        }
        if (comment.getStatus() != CommentConstant.STATUS_NORMAL) {
            throw new GlobeException("该评论已被删除");
        }
        comment.setStatus(CommentConstant.STATUS_USER_DELETED);
        baseMapper.updateById(comment);
    }

    @Override
    public IPage<CommentVo> pageRootComments(Integer page, Integer size, Long videoId, Long episodeId, String sortBy) {
        if (videoId == null) {
            throw new GlobeException("视频ID不能为空");
        }
        Page<CommentVo> pageParam = new Page<>(page, size);
        Long currentUserId = getCurrentUserId();
        String safeSortBy = CommentConstant.SORT_BY_LIKE.equals(sortBy) ? CommentConstant.SORT_BY_LIKE : CommentConstant.SORT_BY_TIME;
        return baseMapper.selectRootComments(pageParam, videoId, episodeId, safeSortBy, currentUserId);
    }

    @Override
    public IPage<CommentVo> pageCommentTree(Integer page, Integer size, String videoUid, String episodeUid, String sortBy) {
        if (videoUid == null || videoUid.isBlank()) {
            throw new GlobeException("视频avid不能为空");
        }
        if (episodeUid == null || episodeUid.isBlank()) {
            throw new GlobeException("剧集avid不能为空");
        }
        // 按 avid 解析视频与剧集
        Video video = videoMapper.selectOne(new LambdaQueryWrapper<Video>().eq(Video::getAvid, videoUid));
        if (video == null) {
            throw new GlobeException("视频不存在");
        }
        VideoEpisode episode = videoEpisodeMapper.selectOne(new LambdaQueryWrapper<VideoEpisode>().eq(VideoEpisode::getAvid, episodeUid));
        if (episode == null || !episode.getVideoId().equals(video.getId())) {
            throw new GlobeException("剧集不存在或不属于该视频");
        }
        // 一级评论分页（排除运营屏蔽）
        Page<CommentVo> pageParam = new Page<>(page, size);
        Long currentUserId = getCurrentUserId();
        String safeSortBy = CommentConstant.SORT_BY_LIKE.equals(sortBy) ? CommentConstant.SORT_BY_LIKE : CommentConstant.SORT_BY_TIME;
        IPage<CommentVo> result = baseMapper.selectRootComments(pageParam, video.getId(), episode.getId(), safeSortBy, currentUserId);
        // 批量加载该页一级评论的全部子回复并组装树
        List<CommentVo> records = result.getRecords();
        if (records.isEmpty()) {
            return result;
        }
        List<Long> rootIds = records.stream().map(CommentVo::getId).collect(Collectors.toList());
        List<CommentVo> allReplies = baseMapper.selectAllRepliesByRootIds(rootIds, currentUserId);
        Map<Long, List<CommentVo>> childrenMap = allReplies.stream()
                .collect(Collectors.groupingBy(CommentVo::getParentId, LinkedHashMap::new, Collectors.toList()));
        records.forEach(root -> attachReplyTree(root, childrenMap));
        return result;
    }

    /**
     * 递归将子回复挂载到评论树上（childrenMap 按 parentId 分组，从根评论出发逐级挂载）
     */
    private void attachReplyTree(CommentVo node, Map<Long, List<CommentVo>> childrenMap) {
        List<CommentVo> children = childrenMap.get(node.getId());
        if (children == null) {
            return;
        }
        node.setReplies(children);
        children.forEach(child -> attachReplyTree(child, childrenMap));
    }

    @Override
    public List<CommentVo> getReplies(Long rootId) {
        if (rootId == null) {
            throw new GlobeException("根评论ID不能为空");
        }
        Long currentUserId = getCurrentUserId();
        return baseMapper.selectReplies(rootId, currentUserId);
    }

    @Override
    public Map<Long, Long> getEpisodeCommentCounts(List<Long> episodeIds) {
        if (episodeIds == null || episodeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return baseMapper.selectEpisodeCommentCounts(episodeIds).stream()
                .collect(Collectors.toMap(EpisodeCommentCountVo::getEpisodeId, EpisodeCommentCountVo::getCommentCount));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentLike toggleLike(Long commentId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        Comment comment = baseMapper.selectById(commentId);
        if (comment == null) {
            throw new GlobeException("评论不存在");
        }
        if (comment.getStatus() == CommentConstant.STATUS_ADMIN_BLOCKED) {
            throw new GlobeException("该评论已被屏蔽");
        }

        // 查询是否已点赞
        LambdaQueryWrapper<CommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentLike::getCommentId, commentId)
                .eq(CommentLike::getUserId, currentUserId);
        CommentLike existingLike = commentLikeMapper.selectOne(wrapper);

        if (existingLike != null) {
            // toggle：已点赞 → 取消，已取消 → 点赞
            if (existingLike.getStatus() == CommentConstant.LIKE_ACTIVE) {
                existingLike.setStatus(CommentConstant.LIKE_CANCELLED);
                commentLikeMapper.updateById(existingLike);
                comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
                baseMapper.updateById(comment);
                return existingLike;
            } else {
                existingLike.setStatus(CommentConstant.LIKE_ACTIVE);
                commentLikeMapper.updateById(existingLike);
                comment.setLikeCount(comment.getLikeCount() + 1);
                baseMapper.updateById(comment);
                return existingLike;
            }
        } else {
            // 首次点赞
            CommentLike like = new CommentLike();
            like.setCommentId(commentId);
            like.setUserId(currentUserId);
            like.setStatus(CommentConstant.LIKE_ACTIVE);
            commentLikeMapper.insert(like);
            comment.setLikeCount(comment.getLikeCount() + 1);
            baseMapper.updateById(comment);
            return like;
        }
    }

    @Override
    public void deleteByAdmin(Long commentId) {
        Comment comment = baseMapper.selectById(commentId);
        if (comment == null) {
            throw new GlobeException("评论不存在");
        }
        comment.setStatus(comment.getStatus()==CommentConstant.STATUS_NORMAL?CommentConstant.STATUS_ADMIN_BLOCKED:CommentConstant.STATUS_NORMAL);
        baseMapper.updateById(comment);
    }

    @Override
    public IPage<CommentVo> pageAdminList(Integer page, Integer size,
                                           Long videoId, Integer status,
                                           String keyword, String nickName,
                                           String startTime, String endTime) {
        Page<CommentVo> pageParam = new Page<>(page, size);
        return baseMapper.selectAdminList(pageParam, videoId, status, keyword, nickName, startTime, endTime);
    }

    @Override
    public CommentStatusStatsVo getStatusStats() {
        return baseMapper.selectStatusStats();
    }

    @Override
    public IPage<CommentVo> pageReviewList(Integer page, Integer size,
                                            String content, String nickName, String videoTitle) {
        Page<CommentVo> pageParam = new Page<>(page, size);
        return baseMapper.selectReviewList(pageParam, content, nickName, videoTitle);
    }

    @Override
    public void review(Long commentId, Integer reviewStatus) {
        if (commentId == null) {
            throw new GlobeException("评论ID不能为空");
        }
        if (reviewStatus == null || (reviewStatus != CommentConstant.STATUS_NORMAL
                && reviewStatus != CommentConstant.STATUS_ADMIN_BLOCKED)) {
            throw new GlobeException("审核状态无效，仅允许 0(通过) 或 2(拒绝)");
        }
        Comment comment = baseMapper.selectById(commentId);
        if (comment == null) {
            throw new GlobeException("评论不存在");
        }
        if (!comment.getStatus().equals(CommentConstant.STATUS_PENDING_REVIEW)) {
            throw new GlobeException("仅待审核状态的评论才能进行审核操作");
        }
        comment.setStatus(reviewStatus);
        baseMapper.updateById(comment);
    }

    @Override
    public List<DailyIncrementVo> getDailyIncrement(LocalDate startDate, LocalDate endDate) {
        List<DailyIncrementVo> dbList = baseMapper.selectDailyIncrement(startDate, endDate);
        Map<LocalDate, DailyIncrementVo> dateMap = dbList.stream()
                .collect(Collectors.toMap(DailyIncrementVo::getDate, v -> v));
        List<DailyIncrementVo> result = new ArrayList<>();
        for (LocalDate day = startDate; !day.isAfter(endDate); day = day.plusDays(1)) {
            DailyIncrementVo vo = dateMap.get(day);
            if (vo == null) {
                vo = new DailyIncrementVo();
                vo.setDate(day);
                vo.setCommentCount(0L);
            }
            result.add(vo);
        }
        return result;
    }

    // ==================== 私有方法 ====================

    /**
     * 查询 review 分类下的 comment_review 配置，判断评论是否开启审核
     *
     * @return true 表示开启审核（评论发布后进入待审核状态），false 表示直接正常发布
     */
    private boolean isCommentAutoReviewEnabled() {
        return webConfigService.getConfigBoolean(
                BaseConstant.WEB_CONFIG_CATEGORY_REVIEW, BaseConstant.WEB_CONFIG_KEY_COMMENT_REVIEW);
    }

    /**
     * 将 Comment 实体转为 CommentVo（不包含用户信息，需调用方自行填充）
     */
    private CommentVo toCommentVo(Comment comment) {
        CommentVo vo = new CommentVo();
        vo.setId(comment.getId());
        vo.setVideoId(comment.getVideoId());
        vo.setEpisodeId(comment.getEpisodeId());
        vo.setUserId(comment.getUserId());
        vo.setParentId(comment.getParentId());
        vo.setRootId(comment.getRootId());
        vo.setContent(comment.getContent());
        vo.setLikeCount(comment.getLikeCount());
        vo.setReplyCount(comment.getReplyCount());
        vo.setStatus(comment.getStatus());
        vo.setLiked(false);
        vo.setCreateTime(comment.getCreateTime());
        return vo;
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
