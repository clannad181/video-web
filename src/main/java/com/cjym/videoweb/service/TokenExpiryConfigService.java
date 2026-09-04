package com.cjym.videoweb.service;

import com.cjym.videoweb.constant.BaseConstant;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Token 有效期配置服务
 * <p>
 * 从 web_config 表 token_expiry 分类读取 token 有效期，支持前台/后台各自配置：
 * front_access_token_expiry  /  front_refresh_token_expiry
 * backend_access_token_expiry / backend_refresh_token_expiry
 * <p>
 * 缓存策略复用 {@link WebConfigService#getConfigValue}（Redis 缓存优先、回源数据库降级），
 * 配置修改后由 WebConfigService 统一失效缓存，无需独立维护缓存 key。
 */
@Service
@Slf4j
public class TokenExpiryConfigService {

    /** 默认 access token 有效期：15 分钟 */
    private static final long DEFAULT_ACCESS_EXPIRY = 900_000L;
    /** 默认 refresh token 有效期：7 天 */
    private static final long DEFAULT_REFRESH_EXPIRY = 604_800_000L;
    /** cleanExpiredTokens 兜底窗口：30 天 */
    public static final long MAX_REFRESH_WINDOW = 2_592_000_000L;

    @Resource
    private WebConfigService webConfigService;

    /**
     * 启动时校验 4 个配置项是否存在，缺失时告警提示（运行期不再重复输出日志）
     */
    @PostConstruct
    public void initCheck() {
        String[] keys = {
                BaseConstant.FRONT_ACCESS_TOKEN_EXPIRY,
                BaseConstant.FRONT_REFRESH_TOKEN_EXPIRY,
                BaseConstant.BACKEND_ACCESS_TOKEN_EXPIRY,
                BaseConstant.BACKEND_REFRESH_TOKEN_EXPIRY
        };
        for (String key : keys) {
            if (StringUtils.isEmpty(webConfigService.getConfigValue(
                    BaseConstant.WEB_CONFIG_CATEGORY_TOKEN_EXPIRY, key))) {
                log.warn("Token有效期配置缺失[key={}]，将使用默认值，可执行 db/init_token_expiry.sql 初始化", key);
            }
        }
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
     * 读取 token 有效期配置（统一走 WebConfigService：Redis 缓存优先，未命中回源数据库）
     *
     * @param configKey     web_config 表中的配置键
     * @param defaultMillis 配置不存在或格式错误时的默认值
     */
    private long getExpiry(String configKey, long defaultMillis) {
        String value = webConfigService.getConfigValue(BaseConstant.WEB_CONFIG_CATEGORY_TOKEN_EXPIRY, configKey);
        if (StringUtils.isEmpty(value)) {
            // 缺失提示已在启动时 warn 过，运行期降为 debug 避免刷屏
            log.debug("Token有效期配置不存在[key={}]，使用默认值{}ms", configKey, defaultMillis);
            return defaultMillis;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.warn("Token有效期配置值格式错误[key={},value={}]，使用默认值{}ms", configKey, value, defaultMillis);
            return defaultMillis;
        }
    }
}
