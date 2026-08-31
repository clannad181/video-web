package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.CategoryMapper;
import com.cjym.videoweb.model.dto.CategoryPageDto;
import com.cjym.videoweb.model.entity.Category;
import com.cjym.videoweb.model.vo.CategorySimpleVo;
import com.cjym.videoweb.model.vo.CategoryVo;
import com.cjym.videoweb.service.CategoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 视频分类 Service 实现
 */
@Slf4j
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private static final String REDIS_KEY_LIST_ENABLED = "category:list:enabled";

    /**
     * 前端传参 → 数据库列名 映射，白名单校验防 SQL 注入
     */
    private static final Map<String, String> COLUMN_MAPPING = Map.of(
            "id", "id",
            "name", "name",
            "sort", "sort",
            "isShow", "is_show",
            "status", "status",
            "createTime", "create_time",
            "updateTime", "update_time"
    );

    private static final Set<String> ALLOWED_DIRECTIONS = Set.of("ASC", "DESC");

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 系统启动时将启用中的分类列表加载到 Redis 缓存
     */
    @PostConstruct
    public void initCache() {
        log.info("初始化缓存分类列表");
        List<CategorySimpleVo> list = loadEnabledFromDb();
        try {
            stringRedisTemplate.opsForValue().set(REDIS_KEY_LIST_ENABLED,
                    objectMapper.writeValueAsString(list));
        } catch (JsonProcessingException ignored) {
        }
        log.info("初始化缓存分类列表完成，共 {} 条", list.size());
    }

    @Override
    public IPage<CategoryPageDto> pageCategory(Integer page, Integer size, String name,
                                                Boolean status, Boolean isShow,
                                                String field, String descOrAsc) {
        Page<CategoryPageDto> pageParam = new Page<>(page, size);
        String orderColumn = resolveColumn(field);
        String orderDirection = resolveDirection(descOrAsc);
        return baseMapper.selectCategoryPage(pageParam, name, status, isShow,
                orderColumn, orderDirection);
    }

    /**
     * 将前端传参的字段名映射为数据库列名，不在白名单内则返回 null（走默认排序）
     */
    private String resolveColumn(String field) {
        if (field == null || field.isBlank()) {
            return null;
        }
        return COLUMN_MAPPING.get(field);
    }

    /**
     * 校验排序方向，仅允许 asc/desc（不区分大小写），非法值返回 null（走默认排序）
     */
    private String resolveDirection(String descOrAsc) {
        if (descOrAsc == null || descOrAsc.isBlank()) {
            return null;
        }
        String upper = descOrAsc.toUpperCase();
        return ALLOWED_DIRECTIONS.contains(upper) ? upper : null;
    }

    @Override
    public CategoryVo getDetailById(Long id) {
        if (id == null) {
            throw new GlobeException("分类ID不能为空");
        }
        Category category = baseMapper.selectById(id);
        if (category == null) {
            throw new GlobeException("分类不存在");
        }
        CategoryVo vo = new CategoryVo();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }

    @Override
    public void add(Category category) {
        if (category.getName() == null || category.getName().isBlank()) {
            throw new GlobeException("分类名称不能为空");
        }
        // 校验名称唯一
        Long count = baseMapper.selectCount(
                new LambdaQueryWrapper<Category>().eq(Category::getName, category.getName())
        );
        if (count > 0) {
            throw new GlobeException("分类名称【" + category.getName() + "】已存在");
        }
        if (category.getIsShow() == null) {
            category.setIsShow(true);
        }
        // 新增分类不允许设置为系统保留分类
        category.setIsSystem(false);
        if (category.getStatus() == null) {
            category.setStatus(true);
        }
        baseMapper.insert(category);
        clearEnabledCache();
    }

    @Override
    public void update(Category category) {
        if (category.getId() == null) {
            throw new GlobeException("分类ID不能为空");
        }
        Category exist = baseMapper.selectById(category.getId());
        if (exist == null) {
            throw new GlobeException("分类不存在");
        }
        // 系统保留分类不可修改名称
        if (Boolean.TRUE.equals(exist.getIsSystem())
                && category.getName() != null
                && !category.getName().equals(exist.getName())) {
            throw new GlobeException("系统保留分类不可修改名称");
        }
        // 如果修改了名称，校验是否与其他分类重复
        if (category.getName() != null && !category.getName().equals(exist.getName())) {
            Long count = baseMapper.selectCount(
                    new LambdaQueryWrapper<Category>()
                            .eq(Category::getName, category.getName())
                            .ne(Category::getId, category.getId())
            );
            if (count > 0) {
                throw new GlobeException("分类名称【" + category.getName() + "】已存在");
            }
        }
        baseMapper.updateById(category);
        clearEnabledCache();
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new GlobeException("分类ID不能为空");
        }
        Category exist = baseMapper.selectById(id);
        if (exist == null) {
            throw new GlobeException("分类不存在");
        }
        if (Boolean.TRUE.equals(exist.getIsSystem())) {
            throw new GlobeException("系统保留分类不可删除");
        }
        baseMapper.deleteById(id);
        clearEnabledCache();
    }

    @Override
    public List<CategorySimpleVo> listEnabled() {
        // 1. 尝试从 Redis 读取
        String cached = stringRedisTemplate.opsForValue().get(REDIS_KEY_LIST_ENABLED);
        if (cached != null && !cached.isEmpty()) {
            try {
                return objectMapper.readValue(cached, new TypeReference<List<CategorySimpleVo>>() {});
            } catch (JsonProcessingException e) {
                // 缓存数据异常，回源数据库
            }
        }

        // 2. Redis 未命中或异常，查数据库
        List<CategorySimpleVo> voList = loadEnabledFromDb();

        // 3. 回写 Redis
        try {
            stringRedisTemplate.opsForValue().set(REDIS_KEY_LIST_ENABLED,
                    objectMapper.writeValueAsString(voList));
        } catch (JsonProcessingException ignored) {
        }

        return voList;
    }

    /**
     * 从数据库加载启用中的分类列表（不查缓存）
     */
    private List<CategorySimpleVo> loadEnabledFromDb() {
        List<Category> list = baseMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, true)
                        .orderByAsc(Category::getSort, Category::getId)
        );
        return list.stream().map(e -> {
            CategorySimpleVo vo = new CategorySimpleVo();
            vo.setId(e.getId());
            vo.setName(e.getName());
            vo.setSort(e.getSort());
            vo.setSvg(e.getSvg());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 清除启用列表的 Redis 缓存
     */
    private void clearEnabledCache() {
        stringRedisTemplate.delete(REDIS_KEY_LIST_ENABLED);
    }
}
