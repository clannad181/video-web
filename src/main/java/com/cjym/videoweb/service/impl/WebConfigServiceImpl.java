package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.constant.BaseConstant;
import com.cjym.videoweb.mapper.WebConfigMapper;
import com.cjym.videoweb.model.entity.WebConfig;
import com.cjym.videoweb.model.vo.WebConfigVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.WebConfigService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.cjym.videoweb.constant.BaseConstant.WEB_CONFIG_REDIS_KEY_PREFIX;

@Service
@Slf4j
public class WebConfigServiceImpl extends ServiceImpl<WebConfigMapper, WebConfig> implements WebConfigService {

    private static final String REDIS_KEY_LIST_ALL = "web_config:list:ALL";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 系统启动时将所有配置加载到 Redis 缓存
     */
    @PostConstruct
    public void initCache() {
        log.info("初始化缓存网站配置信息");
        // 1. 加载全量数据
        List<WebConfigVo> allList = loadConfigListFromDb(null);
        try {
            stringRedisTemplate.opsForValue().set(REDIS_KEY_LIST_ALL, objectMapper.writeValueAsString(allList));
        } catch (JsonProcessingException ignored) {
        }
        // 2. 按分类分组写入
        allList.stream()
                .collect(Collectors.groupingBy(WebConfigVo::getCategory))
                .forEach((category, list) -> {
                    try {
                        stringRedisTemplate.opsForValue()
                                .set(WEB_CONFIG_REDIS_KEY_PREFIX + category, objectMapper.writeValueAsString(list));
                    } catch (JsonProcessingException ignored) {
                    }
                });
        log.info("初始化缓存网站配置信息完成");
    }

    @Override
    public R<List<WebConfigVo>> list(String category) {
        String redisKey = StringUtils.isNotEmpty(category)
                ? WEB_CONFIG_REDIS_KEY_PREFIX + category
                : REDIS_KEY_LIST_ALL;

        // 1. 尝试从 Redis 读取
        String cached = stringRedisTemplate.opsForValue().get(redisKey);
        if (StringUtils.isNotEmpty(cached)) {
            try {
                List<WebConfigVo> voList = objectMapper.readValue(cached,
                        new TypeReference<List<WebConfigVo>>() {});
                if (!voList.isEmpty()) {
                    return R.success(voList);
                }
                // 缓存为空列表时继续回源数据库，避免空列表永久缓存导致新增配置不可见
            } catch (JsonProcessingException e) {
                // 缓存数据异常，回源数据库
            }
        }

        // 2. Redis 未命中，查数据库
        List<WebConfigVo> voList = loadConfigListFromDb(category);

        // 3. 仅当有数据时回写缓存（空列表不缓存）
        if (!voList.isEmpty()) {
            try {
                stringRedisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(voList));
            } catch (JsonProcessingException ignored) {
            }
        }

        return R.success(voList);
    }

    /**
     * 从数据库加载配置列表（不查缓存）
     */
    private List<WebConfigVo> loadConfigListFromDb(String category) {
        LambdaQueryWrapper<WebConfig> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(category)) {
            wrapper.eq(WebConfig::getCategory, category);
        }
        wrapper.orderByAsc(WebConfig::getCategory, WebConfig::getKey);
        List<WebConfig> list = baseMapper.selectList(wrapper);
        return list.stream().map(e -> {
            WebConfigVo vo = new WebConfigVo();
            BeanUtils.copyProperties(e, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public String getConfigValue(String category, String key) {
        if (StringUtils.isEmpty(category) || StringUtils.isEmpty(key)) {
            return null;
        }
        String redisKey = WEB_CONFIG_REDIS_KEY_PREFIX + category;

        // 1. 优先从 Redis 读取该分类配置
        String cached = stringRedisTemplate.opsForValue().get(redisKey);
        List<WebConfigVo> configList = null;
        if (StringUtils.isNotEmpty(cached)) {
            try {
                configList = objectMapper.readValue(cached,
                        new TypeReference<List<WebConfigVo>>() {});
                if (configList.isEmpty()) {
                    // 缓存为空列表视为未命中，回源数据库（自愈残留空缓存）
                    configList = null;
                }
            } catch (JsonProcessingException ignored) {
                // 缓存数据异常，回源数据库
            }
        }

        // 2. Redis 未命中或解析失败，回源数据库并回写缓存（空列表不缓存）
        if (configList == null) {
            configList = loadConfigListFromDb(category);
            if (!configList.isEmpty()) {
                try {
                    stringRedisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(configList));
                } catch (JsonProcessingException ignored) {
                }
            }
        }

        // 3. 按 key 匹配配置值
        for (WebConfigVo vo : configList) {
            if (key.equals(vo.getKey()) && StringUtils.isNotEmpty(vo.getValue())) {
                return vo.getValue();
            }
        }
        return null;
    }

    @Override
    public boolean getConfigBoolean(String category, String key) {
        return BaseConstant.WEB_CONFIG_VALUE_TRUE.equalsIgnoreCase(getConfigValue(category, key));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> update(List<WebConfigVo> configList) {
        if (configList == null || configList.isEmpty()) {
            return R.error("配置列表不能为空");
        }
        Set<String> affectedCategories = new HashSet<>();
        for (WebConfigVo vo : configList) {
            if (vo.getId() == null) {
                return R.error("配置ID不能为空");
            }
            WebConfig exist = baseMapper.selectById(vo.getId());
            if (exist == null) {
                return R.error("配置[id=" + vo.getId() + "]不存在");
            }
            affectedCategories.add(exist.getCategory());
            // 如果修改了 category 或 key，检查是否重复
            if ((StringUtils.isNotEmpty(vo.getCategory()) && !vo.getCategory().equals(exist.getCategory()))
                    || (StringUtils.isNotEmpty(vo.getKey()) && !vo.getKey().equals(exist.getKey()))) {
                String newCategory = StringUtils.isNotEmpty(vo.getCategory()) ? vo.getCategory() : exist.getCategory();
                String newKey = StringUtils.isNotEmpty(vo.getKey()) ? vo.getKey() : exist.getKey();
                LambdaQueryWrapper<WebConfig> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(WebConfig::getCategory, newCategory)
                        .eq(WebConfig::getKey, newKey)
                        .ne(WebConfig::getId, vo.getId());
                if (baseMapper.selectCount(wrapper) > 0) {
                    return R.error("分类[" + newCategory + "]下已存在键[" + newKey + "]");
                }
                affectedCategories.add(newCategory);
            }
            WebConfig config = new WebConfig();
            BeanUtils.copyProperties(vo, config);
            baseMapper.updateById(config);
        }
        // 批量清除受影响分类的 Redis 缓存
        for (String category : affectedCategories) {
            clearCache(category);
        }
        return R.success("更新配置成功");
    }

    /**
     * 清除指定分类及全量列表的 Redis 缓存
     */
    private void clearCache(String category) {
        if (StringUtils.isNotEmpty(category)) {
            stringRedisTemplate.delete(WEB_CONFIG_REDIS_KEY_PREFIX + category);
        }
        stringRedisTemplate.delete(REDIS_KEY_LIST_ALL);
    }
}
