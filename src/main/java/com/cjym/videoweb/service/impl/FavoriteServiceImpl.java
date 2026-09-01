package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.constant.FavoriteConstant;
import com.cjym.videoweb.constant.VideoConstant;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.EpisodeFavoriteMapper;
import com.cjym.videoweb.mapper.FavoriteFolderMapper;
import com.cjym.videoweb.mapper.UserInfoMapper;
import com.cjym.videoweb.mapper.VideoEpisodeMapper;
import com.cjym.videoweb.mapper.VideoMapper;
import com.cjym.videoweb.model.dto.FavoriteAddDto;
import com.cjym.videoweb.model.dto.FavoriteFolderDetailDto;
import com.cjym.videoweb.model.dto.FavoriteListDto;
import com.cjym.videoweb.model.dto.FavoritePublicListDto;
import com.cjym.videoweb.model.dto.FavoriteRemoveDto;
import com.cjym.videoweb.model.dto.FavoriteSearchDto;
import com.cjym.videoweb.model.entity.EpisodeFavorite;
import com.cjym.videoweb.model.entity.FavoriteFolder;
import com.cjym.videoweb.model.entity.UserInfo;
import com.cjym.videoweb.model.entity.Video;
import com.cjym.videoweb.model.entity.VideoEpisode;
import com.cjym.videoweb.model.vo.FavoriteEpisodeVo;
import com.cjym.videoweb.model.vo.FavoriteFolderDetailVo;
import com.cjym.videoweb.model.vo.FavoriteSearchVo;
import com.cjym.videoweb.service.FavoriteFolderService;
import com.cjym.videoweb.service.FavoriteService;
import com.cjym.videoweb.utils.JWTUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 剧集收藏 Service 实现
 */
@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Resource
    private EpisodeFavoriteMapper episodeFavoriteMapper;

    @Resource
    private FavoriteFolderMapper favoriteFolderMapper;

    @Resource
    private VideoEpisodeMapper videoEpisodeMapper;

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private FavoriteFolderService favoriteFolderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(FavoriteAddDto dto) {
        if (dto.getEpisodeUid() == null || dto.getEpisodeUid().isBlank()) {
            throw new GlobeException("剧集avid不能为空");
        }
        Long currentUserId = requireLogin();
        // 按 avid 解析剧集
        VideoEpisode episode = videoEpisodeMapper.selectOne(
                new LambdaQueryWrapper<VideoEpisode>().eq(VideoEpisode::getAvid, dto.getEpisodeUid()));
        if (episode == null) {
            throw new GlobeException("剧集不存在");
        }
        // 剧集未审核通过时不允许收藏
        if (episode.getReviewStatus() == null || episode.getReviewStatus() != VideoConstant.REVIEW_APPROVED) {
            throw new GlobeException("剧集未审核通过，无法收藏");
        }
        // 解析目标收藏夹
        FavoriteFolder folder = favoriteFolderService.resolveTarget(currentUserId, dto.getFolderUid());
        // 查询该夹内是否已收藏
        EpisodeFavorite existing = selectByFolder(episode.getId(), currentUserId, folder.getId());
        if (existing != null && existing.getStatus() == FavoriteConstant.FAVORITE_ACTIVE) {
            throw new GlobeException("该剧集已在此收藏夹中");
        }
        // 激活前判断该用户对该剧集是否已有其他夹的 active 收藏
        Long activeCount = episodeFavoriteMapper.selectCount(new LambdaQueryWrapper<EpisodeFavorite>()
                .eq(EpisodeFavorite::getEpisodeId, episode.getId())
                .eq(EpisodeFavorite::getUserId, currentUserId)
                .eq(EpisodeFavorite::getStatus, FavoriteConstant.FAVORITE_ACTIVE));
        if (existing != null) {
            existing.setStatus(FavoriteConstant.FAVORITE_ACTIVE);
            episodeFavoriteMapper.updateById(existing);
        } else {
            EpisodeFavorite favorite = new EpisodeFavorite();
            favorite.setEpisodeId(episode.getId());
            favorite.setUserId(currentUserId);
            favorite.setFolderId(folder.getId());
            favorite.setStatus(FavoriteConstant.FAVORITE_ACTIVE);
            episodeFavoriteMapper.insert(favorite);
        }
        // 该用户此前对该剧集无 active 收藏时，剧集收藏数 +1（SQL原子自增）
        if (activeCount == null || activeCount == 0) {
            videoEpisodeMapper.update(null, new LambdaUpdateWrapper<VideoEpisode>()
                    .eq(VideoEpisode::getId, episode.getId())
                    .setSql("favorite_count = favorite_count + 1"));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(FavoriteRemoveDto dto) {
        if (dto.getEpisodeUid() == null || dto.getEpisodeUid().isBlank()) {
            throw new GlobeException("剧集avid不能为空");
        }
        Long currentUserId = requireLogin();
        // 按 avid 解析剧集
        VideoEpisode episode = videoEpisodeMapper.selectOne(
                new LambdaQueryWrapper<VideoEpisode>().eq(VideoEpisode::getAvid, dto.getEpisodeUid()));
        if (episode == null) {
            throw new GlobeException("剧集不存在");
        }
        if (StringUtils.isEmpty(dto.getFolderUid())) {
            // 未指定收藏夹：取消该剧集在所有收藏夹中的收藏
            List<EpisodeFavorite> activeFavorites = episodeFavoriteMapper.selectList(
                    new LambdaQueryWrapper<EpisodeFavorite>()
                            .eq(EpisodeFavorite::getEpisodeId, episode.getId())
                            .eq(EpisodeFavorite::getUserId, currentUserId)
                            .eq(EpisodeFavorite::getStatus, FavoriteConstant.FAVORITE_ACTIVE));
            if (activeFavorites.isEmpty()) {
                throw new GlobeException("您尚未收藏该剧集");
            }
            for (EpisodeFavorite favorite : activeFavorites) {
                favorite.setStatus(FavoriteConstant.FAVORITE_CANCELLED);
                episodeFavoriteMapper.updateById(favorite);
            }
            // 全部夹取消后，剧集收藏数 -1（SQL原子自减，防负数）
            videoEpisodeMapper.update(null, new LambdaUpdateWrapper<VideoEpisode>()
                    .eq(VideoEpisode::getId, episode.getId())
                    .gt(VideoEpisode::getFavoriteCount, 0)
                    .setSql("favorite_count = favorite_count - 1"));
            return;
        }
        // 指定收藏夹：仅移除该夹内的收藏
        FavoriteFolder folder = favoriteFolderService.findTarget(currentUserId, dto.getFolderUid());
        if (folder == null) {
            throw new GlobeException("收藏夹不存在");
        }
        EpisodeFavorite existing = selectByFolder(episode.getId(), currentUserId, folder.getId());
        if (existing == null || existing.getStatus() != FavoriteConstant.FAVORITE_ACTIVE) {
            throw new GlobeException("该剧集未在此收藏夹中");
        }
        existing.setStatus(FavoriteConstant.FAVORITE_CANCELLED);
        episodeFavoriteMapper.updateById(existing);
        // 该用户对该剧集已无任何 active 收藏时，剧集收藏数 -1（SQL原子自减，防负数）
        Long activeCount = episodeFavoriteMapper.selectCount(new LambdaQueryWrapper<EpisodeFavorite>()
                .eq(EpisodeFavorite::getEpisodeId, episode.getId())
                .eq(EpisodeFavorite::getUserId, currentUserId)
                .eq(EpisodeFavorite::getStatus, FavoriteConstant.FAVORITE_ACTIVE));
        if (activeCount == null || activeCount == 0) {
            videoEpisodeMapper.update(null, new LambdaUpdateWrapper<VideoEpisode>()
                    .eq(VideoEpisode::getId, episode.getId())
                    .gt(VideoEpisode::getFavoriteCount, 0)
                    .setSql("favorite_count = favorite_count - 1"));
        }
    }

    @Override
    public IPage<FavoriteEpisodeVo> pageList(FavoriteListDto dto) {
        Long currentUserId = requireLogin();
        int page = dto.getPage() != null ? dto.getPage() : 1;
        int size = dto.getSize() != null ? dto.getSize() : 10;
        // 解析目标收藏夹（默认夹不存在返回空页，不创建）
        FavoriteFolder folder = favoriteFolderService.findTarget(currentUserId, dto.getFolderUid());
        Page<FavoriteEpisodeVo> voPage = new Page<>(page, size);
        if (folder == null) {
            voPage.setTotal(0);
            return voPage;
        }
        return buildPage(folder.getId(), page, size);
    }

    @Override
    public IPage<FavoriteSearchVo> pageSearch(FavoriteSearchDto dto) {
        Long currentUserId = requireLogin();
        int page = dto.getPage() != null ? dto.getPage() : 1;
        int size = dto.getSize() != null ? dto.getSize() : 10;
        String keyword = StringUtils.isEmpty(dto.getTitle()) ? null : dto.getTitle().trim();
        // folderUid 不传时搜索全部收藏夹；传入时仅搜索指定收藏夹（夹不存在报错）
        Long folderId = null;
        if (!StringUtils.isEmpty(dto.getFolderUid())) {
            folderId = favoriteFolderService.findTarget(currentUserId, dto.getFolderUid()).getId();
        }
        return episodeFavoriteMapper.selectFavoriteSearchPage(new Page<>(page, size), currentUserId, folderId, keyword);
    }

    @Override
    public IPage<FavoriteEpisodeVo> pagePublicList(FavoritePublicListDto dto) {
        if (StringUtils.isEmpty(dto.getUserUid())) {
            throw new GlobeException("用户uid不能为空");
        }
        if (StringUtils.isEmpty(dto.getFolderUid())) {
            throw new GlobeException("收藏夹标识不能为空");
        }
        int page = dto.getPage() != null ? dto.getPage() : 1;
        int size = dto.getSize() != null ? dto.getSize() : 10;
        // 按 uid 解析目标用户
        UserInfo targetUser = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getUid, dto.getUserUid()));
        if (targetUser == null) {
            throw new GlobeException("用户不存在");
        }
        // 仅允许查看目标用户的公开收藏夹（私密夹统一报不存在）
        FavoriteFolder folder = favoriteFolderMapper.selectOne(new LambdaQueryWrapper<FavoriteFolder>()
                .eq(FavoriteFolder::getAvid, dto.getFolderUid())
                .eq(FavoriteFolder::getUserId, targetUser.getId()));
        if (folder == null || (folder.getIsPrivate() != null && folder.getIsPrivate() == FavoriteConstant.FOLDER_PRIVATE)) {
            throw new GlobeException("收藏夹不存在");
        }
        return buildPage(folder.getId(), page, size);
    }

    @Override
    public FavoriteFolderDetailVo pageDetail(FavoriteFolderDetailDto dto) {
        if (StringUtils.isEmpty(dto.getFolderUid())) {
            throw new GlobeException("收藏夹标识不能为空");
        }
        int page = dto.getPage() != null ? dto.getPage() : 1;
        int size = dto.getSize() != null ? dto.getSize() : 10;
        // 按 avid 查询收藏夹（不限归属）
        FavoriteFolder folder = favoriteFolderMapper.selectOne(new LambdaQueryWrapper<FavoriteFolder>()
                .eq(FavoriteFolder::getAvid, dto.getFolderUid()));
        // 私密夹仅本人可见，他人（含游客）统一报不存在，避免泄露存在性
        if (folder == null || (folder.getIsPrivate() != null && folder.getIsPrivate() == FavoriteConstant.FOLDER_PRIVATE
                && !Objects.equals(folder.getUserId(), getCurrentUserId()))) {
            throw new GlobeException("收藏夹不存在");
        }
        FavoriteFolderDetailVo vo = new FavoriteFolderDetailVo();
        vo.setFolderUid(folder.getAvid());
        vo.setName(folder.getName());
        vo.setIsDefault(folder.getIsDefault() != null && folder.getIsDefault() == 1);
        vo.setIsPrivate(folder.getIsPrivate() != null && folder.getIsPrivate() == FavoriteConstant.FOLDER_PRIVATE);
        // 填充收藏夹所属用户信息
        UserInfo owner = userInfoMapper.selectById(folder.getUserId());
        if (owner != null) {
            vo.setUserUid(owner.getUid());
            vo.setNickName(owner.getNikeName());
            vo.setAvatarUrl(owner.getAvatar());
        }
        IPage<FavoriteEpisodeVo> favPage = buildPage(folder.getId(), page, size);
        vo.setList(favPage);
        // 封面：自定义封面优先，未设置时取夹内最新收藏视频封面
        vo.setCoverUrl(StringUtils.isEmpty(folder.getCoverUrl())
                ? latestFavoriteCover(favPage.getRecords())
                : folder.getCoverUrl());
        vo.setDescription(folder.getDescription());
        return vo;
    }

    /**
     * 取收藏列表中最新收藏视频的封面（记录已按收藏时间倒序）
     */
    private String latestFavoriteCover(List<FavoriteEpisodeVo> records) {
        if (records == null) {
            return null;
        }
        for (FavoriteEpisodeVo record : records) {
            if (!StringUtils.isEmpty(record.getVideoCover())) {
                return record.getVideoCover();
            }
        }
        return null;
    }

    /**
     * 按收藏夹ID分页查询 active 收藏并组装 VO（按收藏时间倒序）
     */
    private IPage<FavoriteEpisodeVo> buildPage(Long folderId, int page, int size) {
        // 分页查询该夹内 active 收藏记录
        Page<EpisodeFavorite> favPage = new Page<>(page, size);
        LambdaQueryWrapper<EpisodeFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EpisodeFavorite::getFolderId, folderId)
                .eq(EpisodeFavorite::getStatus, FavoriteConstant.FAVORITE_ACTIVE)
                .orderByDesc(EpisodeFavorite::getCreateTime);
        Page<EpisodeFavorite> result = episodeFavoriteMapper.selectPage(favPage, wrapper);
        List<EpisodeFavorite> favorites = result.getRecords();
        Page<FavoriteEpisodeVo> voPage = new Page<>(page, size);
        voPage.setTotal(result.getTotal());
        if (favorites.isEmpty()) {
            return voPage;
        }
        // 批量查询剧集信息
        List<Long> episodeIds = favorites.stream()
                .map(EpisodeFavorite::getEpisodeId).distinct().collect(Collectors.toList());
        Map<Long, VideoEpisode> episodeMap = videoEpisodeMapper.selectBatchIds(episodeIds).stream()
                .collect(Collectors.toMap(VideoEpisode::getId, e -> e));
        // 批量查询视频信息
        List<Long> videoIds = episodeMap.values().stream()
                .map(VideoEpisode::getVideoId).distinct().collect(Collectors.toList());
        Map<Long, Video> videoMap = videoMapper.selectBatchIds(videoIds).stream()
                .collect(Collectors.toMap(Video::getId, v -> v));
        // 批量查询UP主信息
        List<Long> upIds = videoMap.values().stream()
                .map(Video::getUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, UserInfo> userMap = userInfoMapper.selectBatchIds(upIds).stream()
                .collect(Collectors.toMap(UserInfo::getId, u -> u));
        // 组装 VO
        List<FavoriteEpisodeVo> voList = favorites.stream().map(fav -> {
            VideoEpisode ep = episodeMap.get(fav.getEpisodeId());
            if (ep == null) {
                return null;
            }
            FavoriteEpisodeVo vo = new FavoriteEpisodeVo();
            vo.setEpisodeAvid(ep.getAvid());
            vo.setEpisodeTitle(ep.getTitle());
            vo.setEpisodeNumber(ep.getEpisodeNumber());
            vo.setDuration(ep.getDuration());
            vo.setFavoriteTime(fav.getCreateTime());
            Video video = videoMap.get(ep.getVideoId());
            if (video != null) {
                vo.setVideoAvid(video.getAvid());
                vo.setVideoTitle(video.getTitle());
                vo.setVideoCover(video.getCoverUrl());
                UserInfo up = userMap.get(video.getUserId());
                if (up != null) {
                    vo.setNickName(up.getNikeName());
                    vo.setAvatarUrl(up.getAvatar());
                }
                if (video.getAvid() != null && ep.getAvid() != null) {
                    vo.setPlayLink("/client/video/" + video.getAvid() + "/episode/" + ep.getAvid());
                }
            }
            return vo;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 查询指定用户在指定收藏夹内的收藏记录
     */
    private EpisodeFavorite selectByFolder(Long episodeId, Long userId, Long folderId) {
        return episodeFavoriteMapper.selectOne(new LambdaQueryWrapper<EpisodeFavorite>()
                .eq(EpisodeFavorite::getEpisodeId, episodeId)
                .eq(EpisodeFavorite::getUserId, userId)
                .eq(EpisodeFavorite::getFolderId, folderId));
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
