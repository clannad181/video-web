package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WebConfigServiceImpl extends ServiceImpl<WebConfigMapper, WebConfig> implements WebConfigService {

    private static final String REDIS_KEY_LIST_ALL = "web_config:list:ALL";
    private static final String REDIS_KEY_LIST_PREFIX = "web_config:list:";

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
                                .set(REDIS_KEY_LIST_PREFIX + category, objectMapper.writeValueAsString(list));
                    } catch (JsonProcessingException ignored) {
                    }
                });
        log.info("初始化缓存网站配置信息完成");
    }

    @Override
    public R<List<WebConfigVo>> list(String category) {
        String redisKey = StringUtils.isNotEmpty(category)
                ? REDIS_KEY_LIST_PREFIX + category
                : REDIS_KEY_LIST_ALL;

        // 1. 尝试从 Redis 读取
        String cached = stringRedisTemplate.opsForValue().get(redisKey);
        if (StringUtils.isNotEmpty(cached)) {
            try {
                List<WebConfigVo> voList = objectMapper.readValue(cached,
                        new TypeReference<List<WebConfigVo>>() {});
                return R.success(voList);
            } catch (JsonProcessingException e) {
                // 缓存数据异常，回源数据库
            }
        }

        // 2. Redis 未命中，查数据库
        List<WebConfigVo> voList = loadConfigListFromDb(category);

        // 3. 写入 Redis 缓存
        try {
            stringRedisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(voList));
        } catch (JsonProcessingException ignored) {
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
            stringRedisTemplate.delete(REDIS_KEY_LIST_PREFIX + category);
        }
        stringRedisTemplate.delete(REDIS_KEY_LIST_ALL);
    }
}
