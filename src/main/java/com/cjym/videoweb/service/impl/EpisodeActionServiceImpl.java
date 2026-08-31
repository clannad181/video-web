package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cjym.videoweb.constant.BaseConstant;
import com.cjym.videoweb.constant.CommentConstant;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.EpisodeCoinMapper;
import com.cjym.videoweb.mapper.EpisodeFavoriteMapper;
import com.cjym.videoweb.mapper.EpisodeLikeMapper;
import com.cjym.videoweb.mapper.FavoriteFolderMapper;
import com.cjym.videoweb.mapper.VideoEpisodeMapper;
import com.cjym.videoweb.model.entity.EpisodeCoin;
import com.cjym.videoweb.model.entity.EpisodeFavorite;
import com.cjym.videoweb.model.entity.EpisodeLike;
import com.cjym.videoweb.model.entity.FavoriteFolder;
import com.cjym.videoweb.model.entity.VideoEpisode;
import com.cjym.videoweb.model.vo.EpisodeActionStatusVo;
import com.cjym.videoweb.model.vo.EpisodeActionVo;
import com.cjym.videoweb.model.vo.EpisodeFavoriteStatusVo;
import com.cjym.videoweb.model.vo.RecentActionEpisodeVo;
import com.cjym.videoweb.service.EpisodeActionService;
import com.cjym.videoweb.utils.JWTUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class EpisodeActionServiceImpl implements EpisodeActionService {

    @Resource
    private EpisodeLikeMapper episodeLikeMapper;

    @Resource
    private EpisodeCoinMapper episodeCoinMapper;

    @Resource
    private VideoEpisodeMapper videoEpisodeMapper;

    @Resource
    private EpisodeFavoriteMapper episodeFavoriteMapper;

    @Resource
    private FavoriteFolderMapper favoriteFolderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EpisodeActionVo toggleLike(String episodeUid) {
        if (episodeUid == null || episodeUid.isBlank()) {
            throw new GlobeException("剧集avid不能为空");
        }
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        // 按 avid 查询剧集
        VideoEpisode episode = videoEpisodeMapper.selectOne(
                new LambdaQueryWrapper<VideoEpisode>().eq(VideoEpisode::getAvid, episodeUid));
        if (episode == null) {
            throw new GlobeException("剧集不存在");
        }

        Long episodeId = episode.getId();
        // 查询是否已点赞
        LambdaQueryWrapper<EpisodeLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EpisodeLike::getEpisodeId, episodeId)
                .eq(EpisodeLike::getUserId, currentUserId);
        EpisodeLike existingLike = episodeLikeMapper.selectOne(wrapper);

        boolean isLiked;
        long likeCount;

        if (existingLike != null) {
            // toggle：已点赞 → 取消，已取消 → 点赞
            if (existingLike.getStatus() == CommentConstant.LIKE_ACTIVE) {
                existingLike.setStatus(CommentConstant.LIKE_CANCELLED);
                episodeLikeMapper.updateById(existingLike);
                // 剧集点赞数 -1
                videoEpisodeMapper.update(null, new LambdaUpdateWrapper<VideoEpisode>()
                        .eq(VideoEpisode::getId, episodeId)
                        .setSql("like_count = like_count - 1"));
                isLiked = false;
            } else {
                existingLike.setStatus(CommentConstant.LIKE_ACTIVE);
                // 重新点赞时刷新操作时间，保证“最近点赞”按最新点赞时间排序
                existingLike.setCreateTime(LocalDateTime.now());
                episodeLikeMapper.updateById(existingLike);
                // 剧集点赞数 +1
                videoEpisodeMapper.update(null, new LambdaUpdateWrapper<VideoEpisode>()
                        .eq(VideoEpisode::getId, episodeId)
                        .setSql("like_count = like_count + 1"));
                isLiked = true;
            }
        } else {
            // 首次点赞
            EpisodeLike like = new EpisodeLike();
            like.setEpisodeId(episodeId);
            like.setUserId(currentUserId);
            like.setStatus(CommentConstant.LIKE_ACTIVE);
            episodeLikeMapper.insert(like);
            // 剧集点赞数 +1
            videoEpisodeMapper.update(null, new LambdaUpdateWrapper<VideoEpisode>()
                    .eq(VideoEpisode::getId, episodeId)
                    .setSql("like_count = like_count + 1"));
            isLiked = true;
        }

        // 查询最新点赞数
        VideoEpisode updatedEpisode = videoEpisodeMapper.selectById(episodeId);
        likeCount = updatedEpisode != null && updatedEpisode.getLikeCount() != null
                ? updatedEpisode.getLikeCount() : 0L;

        EpisodeActionVo vo = new EpisodeActionVo();
        vo.setCount(likeCount);
        vo.setIsActioned(isLiked);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EpisodeActionVo coin(String episodeUid) {
        if (episodeUid == null || episodeUid.isBlank()) {
            throw new GlobeException("剧集avid不能为空");
        }
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        // 按 avid 查询剧集
        VideoEpisode episode = videoEpisodeMapper.selectOne(
                new LambdaQueryWrapper<VideoEpisode>().eq(VideoEpisode::getAvid, episodeUid));
        if (episode == null) {
            throw new GlobeException("剧集不存在");
        }

        Long episodeId = episode.getId();
        // 查询是否已投币（每个用户每集只能投币一次，不可取消）
        LambdaQueryWrapper<EpisodeCoin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EpisodeCoin::getEpisodeId, episodeId)
                .eq(EpisodeCoin::getUserId, currentUserId);
        EpisodeCoin existingCoin = episodeCoinMapper.selectOne(wrapper);

        if (existingCoin != null) {
            throw new GlobeException("您已经投过币了，每集只能投币一次");
        }

        // 新增投币记录
        EpisodeCoin coin = new EpisodeCoin();
        coin.setEpisodeId(episodeId);
        coin.setUserId(currentUserId);
        coin.setCoinCount(1);
        episodeCoinMapper.insert(coin);

        // 剧集投币数 +1
        videoEpisodeMapper.update(null, new LambdaUpdateWrapper<VideoEpisode>()
                .eq(VideoEpisode::getId, episodeId)
                .setSql("coin_count = coin_count + 1"));

        // 查询最新投币数
        VideoEpisode updatedEpisode = videoEpisodeMapper.selectById(episodeId);
        long coinCount = updatedEpisode != null && updatedEpisode.getCoinCount() != null
                ? updatedEpisode.getCoinCount() : 0L;

        EpisodeActionVo vo = new EpisodeActionVo();
        vo.setCount(coinCount);
        vo.setIsActioned(true);
        return vo;
    }

    @Override
    public EpisodeActionStatusVo getActionStatus(String episodeUid) {
        if (episodeUid == null || episodeUid.isBlank()) {
            throw new GlobeException("剧集avid不能为空");
        }
        // 按 avid 查询剧集
        VideoEpisode episode = videoEpisodeMapper.selectOne(
                new LambdaQueryWrapper<VideoEpisode>().eq(VideoEpisode::getAvid, episodeUid));
        if (episode == null) {
            throw new GlobeException("剧集不存在");
        }

        Long episodeId = episode.getId();
        EpisodeActionStatusVo vo = new EpisodeActionStatusVo();
        vo.setIsLiked(isLiked(episodeId));
        vo.setIsCoined(isCoined(episodeId));
        return vo;
    }

    @Override
    public EpisodeFavoriteStatusVo getFavoriteStatus(String episodeUid) {
        if (episodeUid == null || episodeUid.isBlank()) {
            throw new GlobeException("剧集avid不能为空");
        }
        // 按 avid 查询剧集
        VideoEpisode episode = videoEpisodeMapper.selectOne(
                new LambdaQueryWrapper<VideoEpisode>().eq(VideoEpisode::getAvid, episodeUid));
        if (episode == null) {
            throw new GlobeException("剧集不存在");
        }

        EpisodeFavoriteStatusVo vo = new EpisodeFavoriteStatusVo();
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            vo.setIsFavorited(false);
            return vo;
        }
        // 查询该用户对该剧集的所有 active 收藏记录
        List<EpisodeFavorite> favorites = episodeFavoriteMapper.selectList(
                new LambdaQueryWrapper<EpisodeFavorite>()
                        .eq(EpisodeFavorite::getEpisodeId, episode.getId())
                        .eq(EpisodeFavorite::getUserId, currentUserId)
                        .eq(EpisodeFavorite::getStatus, CommentConstant.LIKE_ACTIVE));
        if (favorites.isEmpty()) {
            vo.setIsFavorited(false);
            return vo;
        }
        vo.setIsFavorited(true);
        // 批量查询收藏夹标识列表
        List<Long> folderIds = favorites.stream()
                .map(EpisodeFavorite::getFolderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!folderIds.isEmpty()) {
            List<String> folderUids = favoriteFolderMapper.selectBatchIds(folderIds).stream()
                    .map(FavoriteFolder::getAvid)
                    .collect(Collectors.toList());
            vo.setFolderUids(folderUids);
        }
        return vo;
    }

    @Override
    public List<RecentActionEpisodeVo> listRecentLikes() {
        Long currentUserId = requireLogin();
        List<RecentActionEpisodeVo> result = episodeLikeMapper.selectRecentLikeEpisodes(
                currentUserId, BaseConstant.RECENT_ACTION_LIMIT);
        fillPlayLink(result);
        return result;
    }

    @Override
    public List<RecentActionEpisodeVo> listRecentCoins() {
        Long currentUserId = requireLogin();
        List<RecentActionEpisodeVo> result = episodeCoinMapper.selectRecentCoinEpisodes(
                currentUserId, BaseConstant.RECENT_ACTION_LIMIT);
        fillPlayLink(result);
        return result;
    }

    /**
     * 为最近互动记录填充播放跳转链接：/client/video/{videoAvid}/episode/{episodeAvid}
     */
    private void fillPlayLink(List<RecentActionEpisodeVo> vos) {
        vos.forEach(vo -> {
            if (vo.getVideoAvid() != null && vo.getEpisodeAvid() != null) {
                vo.setPlayLink("/client/video/" + vo.getVideoAvid() + "/episode/" + vo.getEpisodeAvid());
            }
        });
    }

    /**
     * 获取当前登录用户ID，未登录抛异常
     */
    private Long requireLogin() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new GlobeException("请先登录");
        }
        return currentUserId;
    }

    @Override
    public boolean isLiked(Long episodeId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null || episodeId == null) {
            return false;
        }
        LambdaQueryWrapper<EpisodeLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EpisodeLike::getEpisodeId, episodeId)
                .eq(EpisodeLike::getUserId, currentUserId)
                .eq(EpisodeLike::getStatus, CommentConstant.LIKE_ACTIVE);
        return episodeLikeMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean isCoined(Long episodeId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null || episodeId == null) {
            return false;
        }
        LambdaQueryWrapper<EpisodeCoin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EpisodeCoin::getEpisodeId, episodeId)
                .eq(EpisodeCoin::getUserId, currentUserId);
        return episodeCoinMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean isFavorited(Long episodeId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null || episodeId == null) {
            return false;
        }
        LambdaQueryWrapper<EpisodeFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EpisodeFavorite::getEpisodeId, episodeId)
                .eq(EpisodeFavorite::getUserId, currentUserId)
                .eq(EpisodeFavorite::getStatus, CommentConstant.LIKE_ACTIVE);
        return episodeFavoriteMapper.selectCount(wrapper) > 0;
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
