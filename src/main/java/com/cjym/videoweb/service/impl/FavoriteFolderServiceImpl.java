package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cjym.videoweb.constant.FavoriteConstant;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.EpisodeFavoriteMapper;
import com.cjym.videoweb.mapper.FavoriteFolderMapper;
import com.cjym.videoweb.mapper.UserInfoMapper;
import com.cjym.videoweb.model.dto.FavoriteFolderCreateDto;
import com.cjym.videoweb.model.dto.FavoriteFolderDeleteDto;
import com.cjym.videoweb.model.dto.FavoriteFolderUpdateDto;
import com.cjym.videoweb.model.dto.FavoritePublicFolderListDto;
import com.cjym.videoweb.model.entity.EpisodeFavorite;
import com.cjym.videoweb.model.entity.FavoriteFolder;
import com.cjym.videoweb.model.entity.UserInfo;
import com.cjym.videoweb.model.vo.FavoriteFolderVo;
import com.cjym.videoweb.service.FavoriteFolderService;
import com.cjym.videoweb.utils.Base62Converter;
import com.cjym.videoweb.utils.JWTUtil;
import com.cjym.videoweb.utils.SnowflakeIdGenerator;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * 收藏文件夹 Service 实现
 */
@Service
public class FavoriteFolderServiceImpl implements FavoriteFolderService {

    @Resource
    private FavoriteFolderMapper favoriteFolderMapper;

    @Resource
    private EpisodeFavoriteMapper episodeFavoriteMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FavoriteFolder getOrCreateDefault(Long userId) {
        FavoriteFolder defaultFolder = favoriteFolderMapper.selectOne(new LambdaQueryWrapper<FavoriteFolder>()
                .eq(FavoriteFolder::getUserId, userId)
                .eq(FavoriteFolder::getIsDefault, 1));
        if (defaultFolder != null) {
            return defaultFolder;
        }
        // 惰性创建默认收藏夹
        FavoriteFolder folder = new FavoriteFolder();
        folder.setAvid(FavoriteConstant.FOLDER_PREFIX + Base62Converter.encode(snowflakeIdGenerator.nextId()));
        folder.setUserId(userId);
        folder.setName(FavoriteConstant.DEFAULT_FOLDER_NAME);
        folder.setIsDefault(1);
        favoriteFolderMapper.insert(folder);
        return folder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FavoriteFolder resolveTarget(Long userId, String folderUid) {
        if (StringUtils.isEmpty(folderUid)) {
            return getOrCreateDefault(userId);
        }
        return requireFolder(userId, folderUid);
    }

    @Override
    public FavoriteFolder findTarget(Long userId, String folderUid) {
        if (StringUtils.isEmpty(folderUid)) {
            // 默认夹不存在时返回 null，不创建
            return favoriteFolderMapper.selectOne(new LambdaQueryWrapper<FavoriteFolder>()
                    .eq(FavoriteFolder::getUserId, userId)
                    .eq(FavoriteFolder::getIsDefault, 1));
        }
        return requireFolder(userId, folderUid);
    }

    @Override
    public List<FavoriteFolderVo> list() {
        Long currentUserId = requireLogin();
        return favoriteFolderMapper.selectFolderListWithCount(currentUserId, false);
    }

    @Override
    public List<FavoriteFolderVo> listPublic(FavoritePublicFolderListDto dto) {
        if (StringUtils.isEmpty(dto.getUserUid())) {
            throw new GlobeException("用户uid不能为空");
        }
        // 按 uid 解析目标用户
        UserInfo targetUser = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getUid, dto.getUserUid()));
        if (targetUser == null) {
            throw new GlobeException("用户不存在");
        }
        // 仅返回公开收藏夹
        return favoriteFolderMapper.selectFolderListWithCount(targetUser.getId(), true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(FavoriteFolderCreateDto dto) {
        Long currentUserId = requireLogin();
        String name = checkName(dto.getName());
        // 收藏夹数量上限校验
        Long folderCount = favoriteFolderMapper.selectCount(new LambdaQueryWrapper<FavoriteFolder>()
                .eq(FavoriteFolder::getUserId, currentUserId));
        if (folderCount != null && folderCount >= FavoriteConstant.FOLDER_COUNT_MAX) {
            throw new GlobeException("收藏夹数量已达上限" + FavoriteConstant.FOLDER_COUNT_MAX + "个");
        }
        // 同用户收藏夹名称唯一校验
        checkNameUnique(currentUserId, name, null);
        FavoriteFolder folder = new FavoriteFolder();
        folder.setAvid(FavoriteConstant.FOLDER_PREFIX + Base62Converter.encode(snowflakeIdGenerator.nextId()));
        folder.setUserId(currentUserId);
        folder.setName(name);
        folder.setIsDefault(0);
        folder.setIsPrivate(Boolean.TRUE.equals(dto.getIsPrivate())
                ? FavoriteConstant.FOLDER_PRIVATE : FavoriteConstant.FOLDER_PUBLIC);
        favoriteFolderMapper.insert(folder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(FavoriteFolderUpdateDto dto) {
        Long currentUserId = requireLogin();
        FavoriteFolder folder = requireFolder(currentUserId, dto.getFolderUid());
        // name 与 isPrivate 至少传一个
        if (StringUtils.isEmpty(dto.getName()) && dto.getIsPrivate() == null) {
            throw new GlobeException("没有需要修改的内容");
        }
        // 名称非空时校验并更新（默认夹可改名）
        if (!StringUtils.isEmpty(dto.getName())) {
            String name = checkName(dto.getName());
            checkNameUnique(currentUserId, name, folder.getId());
            folder.setName(name);
        }
        // 可见性非空时更新（默认夹同样支持私密/公开切换）
        if (dto.getIsPrivate() != null) {
            folder.setIsPrivate(dto.getIsPrivate()
                    ? FavoriteConstant.FOLDER_PRIVATE : FavoriteConstant.FOLDER_PUBLIC);
        }
        favoriteFolderMapper.updateById(folder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(FavoriteFolderDeleteDto dto) {
        Long currentUserId = requireLogin();
        FavoriteFolder folder = requireFolder(currentUserId, dto.getFolderUid());
        if (folder.getIsDefault() != null && folder.getIsDefault() == 1) {
            throw new GlobeException("默认收藏夹不可删除");
        }
        // 夹内 active 收藏移回默认夹（默认夹已有同剧集收藏则直接取消原记录），favorite_count 不变
        FavoriteFolder defaultFolder = getOrCreateDefault(currentUserId);
        List<EpisodeFavorite> favorites = episodeFavoriteMapper.selectList(new LambdaQueryWrapper<EpisodeFavorite>()
                .eq(EpisodeFavorite::getFolderId, folder.getId())
                .eq(EpisodeFavorite::getStatus, FavoriteConstant.FAVORITE_ACTIVE));
        for (EpisodeFavorite favorite : favorites) {
            Long existCount = episodeFavoriteMapper.selectCount(new LambdaQueryWrapper<EpisodeFavorite>()
                    .eq(EpisodeFavorite::getEpisodeId, favorite.getEpisodeId())
                    .eq(EpisodeFavorite::getUserId, currentUserId)
                    .eq(EpisodeFavorite::getFolderId, defaultFolder.getId())
                    .eq(EpisodeFavorite::getStatus, FavoriteConstant.FAVORITE_ACTIVE));
            if (existCount != null && existCount > 0) {
                favorite.setStatus(FavoriteConstant.FAVORITE_CANCELLED);
                episodeFavoriteMapper.updateById(favorite);
            } else {
                favorite.setFolderId(defaultFolder.getId());
                episodeFavoriteMapper.updateById(favorite);
            }
        }
        // 软删除文件夹
        favoriteFolderMapper.deleteById(folder.getId());
    }

    /**
     * 按 avid 查询收藏夹并校验归属当前用户
     */
    private FavoriteFolder requireFolder(Long userId, String folderUid) {
        if (StringUtils.isEmpty(folderUid)) {
            throw new GlobeException("收藏夹标识不能为空");
        }
        FavoriteFolder folder = favoriteFolderMapper.selectOne(new LambdaQueryWrapper<FavoriteFolder>()
                .eq(FavoriteFolder::getAvid, folderUid)
                .eq(FavoriteFolder::getUserId, userId));
        if (folder == null) {
            throw new GlobeException("收藏夹不存在");
        }
        return folder;
    }

    /**
     * 校验收藏夹名称：非空且不超过最大长度
     */
    private String checkName(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new GlobeException("收藏夹名称不能为空");
        }
        String trimmed = name.trim();
        if (trimmed.length() > FavoriteConstant.FOLDER_NAME_MAX_LENGTH) {
            throw new GlobeException("收藏夹名称最长" + FavoriteConstant.FOLDER_NAME_MAX_LENGTH + "字");
        }
        return trimmed;
    }

    /**
     * 校验同用户收藏夹名称唯一（排除指定文件夹）
     */
    private void checkNameUnique(Long userId, String name, Long excludeId) {
        LambdaQueryWrapper<FavoriteFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FavoriteFolder::getUserId, userId)
                .eq(FavoriteFolder::getName, name);
        if (excludeId != null) {
            wrapper.ne(FavoriteFolder::getId, excludeId);
        }
        if (favoriteFolderMapper.selectCount(wrapper) > 0) {
            throw new GlobeException("收藏夹名称已存在");
        }
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
            String username = JWTUtil.getUsername(token);
            UserInfo userInfo = userInfoMapper.selectByUserName(username);
            return userInfo != null ? userInfo.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
