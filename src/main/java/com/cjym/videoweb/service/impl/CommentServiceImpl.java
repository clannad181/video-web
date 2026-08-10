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
import com.cjym.videoweb.mapper.WebConfigMapper;
import com.cjym.videoweb.model.dto.CommentSaveDto;
import com.cjym.videoweb.model.entity.Comment;
import com.cjym.videoweb.model.entity.CommentLike;
import com.cjym.videoweb.model.entity.UserInfo;
import com.cjym.videoweb.model.entity.WebConfig;
import com.cjym.videoweb.model.vo.DailyIncrementVo;
import com.cjym.videoweb.model.vo.CommentStatusStatsVo;
import com.cjym.videoweb.model.vo.CommentVo;
import com.cjym.videoweb.model.vo.WebConfigVo;
import com.cjym.videoweb.service.CommentService;
import com.cjym.videoweb.utils.JWTUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
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
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private WebConfigMapper webConfigMapper;

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
        comment.setUserId(currentUserId);
        comment.setContent(dto.getContent().trim());

        // 确定 parentId 和 rootId
        Long parentId = dto.getParentId() != null && dto.getParentId() != 0 ? dto.getParentId() : 0L;
        comment.setParentId(parentId);

        // 根据 comment_review 配置决定评论状态：免审则直接正常，否则进入待审核
        comment.setStatus(isCommentAutoReviewEnabled() ? CommentConstant.STATUS_NORMAL : CommentConstant.STATUS_PENDING_REVIEW);

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
            comment.setRootId(parentComment.getRootId() != 0 ? parentComment.getRootId() : parentComment.getId());
            baseMapper.insert(comment);

            // 更新父评论的 replyCount
            parentComment.setReplyCount(parentComment.getReplyCount() + 1);
            baseMapper.updateById(parentComment);
        }

        return toCommentVo(comment);
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
    public IPage<CommentVo> pageRootComments(Integer page, Integer size, Long videoId, String sortBy) {
        if (videoId == null) {
            throw new GlobeException("视频ID不能为空");
        }
        Page<CommentVo> pageParam = new Page<>(page, size);
        Long currentUserId = getCurrentUserId();
        String safeSortBy = CommentConstant.SORT_BY_LIKE.equals(sortBy) ? CommentConstant.SORT_BY_LIKE : CommentConstant.SORT_BY_TIME;
        return baseMapper.selectRootComments(pageParam, videoId, safeSortBy, currentUserId);
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
     * 查询 Redis 中 review 分类下的 comment_review 配置，判断评论是否免审核
     *
     * @return true 表示开启免审（评论直接发布，状态为正常）
     */
    private boolean isCommentAutoReviewEnabled() {
        String redisKey = BaseConstant.WEB_CONFIG_REDIS_KEY_PREFIX + BaseConstant.WEB_CONFIG_CATEGORY_REVIEW;
        // 1. 尝试从 Redis 缓存读取
        String cached = stringRedisTemplate.opsForValue().get(redisKey);
        if (StringUtils.isNotEmpty(cached)) {
            try {
                List<WebConfigVo> list = objectMapper.readValue(cached,
                        new TypeReference<List<WebConfigVo>>() {});
                for (WebConfigVo vo : list) {
                    if (BaseConstant.WEB_CONFIG_KEY_COMMENT_REVIEW.equals(vo.getKey())) {
                        return BaseConstant.WEB_CONFIG_VALUE_TRUE.equalsIgnoreCase(vo.getValue());
                    }
                }
                return false;
            } catch (JsonProcessingException ignored) {
                // 缓存数据异常，回源数据库
            }
        }
        // 2. Redis 未命中或数据异常，查数据库
        LambdaQueryWrapper<WebConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WebConfig::getCategory, BaseConstant.WEB_CONFIG_CATEGORY_REVIEW)
                .eq(WebConfig::getKey, BaseConstant.WEB_CONFIG_KEY_COMMENT_REVIEW);
        WebConfig config = webConfigMapper.selectOne(wrapper);
        return config != null && BaseConstant.WEB_CONFIG_VALUE_TRUE.equalsIgnoreCase(config.getValue());
    }

    /**
     * 将 Comment 实体转为 CommentVo（不包含用户信息，需调用方自行填充）
     */
    private CommentVo toCommentVo(Comment comment) {
        CommentVo vo = new CommentVo();
        vo.setId(comment.getId());
        vo.setVideoId(comment.getVideoId());
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
            String username = JWTUtil.getUsername(token);
            UserInfo userInfo = userInfoMapper.selectByUserName(username);
            return userInfo != null ? userInfo.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
