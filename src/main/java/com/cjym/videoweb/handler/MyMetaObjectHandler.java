package com.cjym.videoweb.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.cjym.videoweb.mapper.UserInfoMapper;
import com.cjym.videoweb.model.entity.UserInfo;
import com.cjym.videoweb.utils.JWTUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Lazy
    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public void insertFill(MetaObject metaObject) {
        // 创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // 是否删除（默认 0 = 未删除）
        this.strictInsertFill(metaObject, "isDelete", Integer.class, 0);
        // 创建人：从当前请求的 JWT token 中获取用户ID
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            this.strictInsertFill(metaObject, "createUser", Long.class, currentUserId);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // Role 表暂无需要更新时自动填充的字段
    }

    /**
     * 从当前请求的 JWT token 中获取用户 ID
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
