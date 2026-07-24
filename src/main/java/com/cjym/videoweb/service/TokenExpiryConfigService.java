package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cjym.videoweb.constant.BaseConstant;
import com.cjym.videoweb.mapper.WebConfigMapper;
import com.cjym.videoweb.model.entity.WebConfig;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Token 有效期配置服务
 * <p>
 * 从 web_config 表读取 token 有效期并缓存到 Redis，支持前台/后台各自配置：
 * front_access_token_expiry  /  front_refresh_token_expiry
 * backend_access_token_expiry / backend_refresh_token_expiry
 */
@Service
@Slf4j
public class TokenExpiryConfigService {

    private static final String REDIS_KEY_PREFIX = "token_expiry:";

    /** 默认 access token 有效期：15 分钟 */
    private static final long DEFAULT_ACCESS_EXPIRY = 900_000L;
    /** 默认 refresh token 有效期：7 天 */
    private static final long DEFAULT_REFRESH_EXPIRY = 604_800_000L;
    /** cleanExpiredTokens 兜底窗口：30 天 */
    public static final long MAX_REFRESH_WINDOW = 2_592_000_000L;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private WebConfigMapper webConfigMapper;

    /**
     * 启动时预热4个配置到 Redis
     */
    @PostConstruct
    public void initCache() {
        String[] keys = {
                BaseConstant.FRONT_ACCESS_TOKEN_EXPIRY,
                BaseConstant.FRONT_REFRESH_TOKEN_EXPIRY,
                BaseConstant.BACKEND_ACCESS_TOKEN_EXPIRY,
                BaseConstant.BACKEND_REFRESH_TOKEN_EXPIRY
        };
        for (String key : keys) {
            getExpiryFromSource(key);
        }
        log.info("Token有效期配置已加载到Redis");
    }

    /**
     * 根据 serverType 获取 access token 有效期（毫秒）
     *
     * @param serverType {@link BaseConstant#SYS_ADMIN} 或 {@link BaseConstant#SYS_FRONT}
     */
    public long getAccessTokenExpiry(String serverType) {
        String key = BaseConstant.SYS_ADMIN.equals(serverType)
                ? BaseConstant.BACKEND_ACCESS_TOKEN_EXPIRY
                : BaseConstant.FRONT_ACCESS_TOKEN_EXPIRY;
        return getExpiry(key, DEFAULT_ACCESS_EXPIRY);
    }

    /**
     * 根据 serverType 获取 refresh token 有效期（毫秒）
     *
     * @param serverType {@link BaseConstant#SYS_ADMIN} 或 {@link BaseConstant#SYS_FRONT}
     */
    public long getRefreshTokenExpiry(String serverType) {
        String key = BaseConstant.SYS_ADMIN.equals(serverType)
                ? BaseConstant.BACKEND_REFRESH_TOKEN_EXPIRY
                : BaseConstant.FRONT_REFRESH_TOKEN_EXPIRY;
        return getExpiry(key, DEFAULT_REFRESH_EXPIRY);
    }

    /**
     * 从 Redis → DB 逐级读取配置值（含缓存回填）
     */
    private long getExpiry(String configKey, long defaultMillis) {
        // 1. Redis 缓存
        String redisValue = stringRedisTemplate.opsForValue().get(REDIS_KEY_PREFIX + configKey);
        if (StringUtils.isNotEmpty(redisValue)) {
            try {
                return Long.parseLong(redisValue);
            } catch (NumberFormatException e) {
                log.warn("Token有效期Redis值格式错误[key={},value={}]", configKey, redisValue);
            }
        }
        // 2. 回源 DB 并写入 Redis
        return getExpiryFromSource(configKey, defaultMillis);
    }

    private long getExpiryFromSource(String configKey) {
        return getExpiryFromSource(configKey, DEFAULT_ACCESS_EXPIRY);
    }

    private long getExpiryFromSource(String configKey, long defaultMillis) {
        LambdaQueryWrapper<WebConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WebConfig::getKey, configKey);
        WebConfig config = webConfigMapper.selectOne(wrapper);
        long expiry = defaultMillis;
        if (config != null && StringUtils.isNotEmpty(config.getValue())) {
            try {
                expiry = Long.parseLong(config.getValue());
            } catch (NumberFormatException e) {
                log.warn("Token有效期DB值格式错误[key={},value={}]，使用默认值", configKey, config.getValue());
            }
        } else {
            log.info("Token有效期配置不存在[key={}]，使用默认值{}ms", configKey, defaultMillis);
        }
        stringRedisTemplate.opsForValue().set(REDIS_KEY_PREFIX + configKey, String.valueOf(expiry));
        return expiry;
    }
}
