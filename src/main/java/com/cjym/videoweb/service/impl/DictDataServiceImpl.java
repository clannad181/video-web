package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.constant.BaseConstant;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.DictDataMapper;
import com.cjym.videoweb.mapper.DictTypeMapper;
import com.cjym.videoweb.model.dto.DictDataPageDto;
import com.cjym.videoweb.model.entity.DictData;
import com.cjym.videoweb.model.entity.DictType;
import com.cjym.videoweb.model.vo.DictDataSimpleVo;
import com.cjym.videoweb.model.vo.DictDataVo;
import com.cjym.videoweb.service.DictDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 字典数据 Service 实现
 */
@Service
public class DictDataServiceImpl extends ServiceImpl<DictDataMapper, DictData> implements DictDataService {

    /**
     * 前端传参 → 数据库列名 映射，白名单校验防 SQL 注入
     */
    private static final Map<String, String> COLUMN_MAPPING = Map.of(
            "id", "id",
            "label", "label",
            "value", "value",
            "sort", "sort",
            "status", "status",
            "createTime", "create_time",
            "updateTime", "update_time"
    );

    private static final Set<String> ALLOWED_DIRECTIONS = Set.of("ASC", "DESC");

    @Resource
    private DictTypeMapper dictTypeMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public IPage<DictDataPageDto> pageDictData(Integer page, Integer size, String typeCode, String label,
                                               Boolean status, String field, String descOrAsc) {
        if (typeCode == null || typeCode.isBlank()) {
            throw new GlobeException("字典类型编码不能为空");
        }
        Page<DictDataPageDto> pageParam = new Page<>(page, size);
        String orderColumn = resolveColumn(field);
        String orderDirection = resolveDirection(descOrAsc);
        return baseMapper.selectDictDataPage(pageParam, typeCode, label, status,
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
    public DictDataVo getDetailById(Long id) {
        if (id == null) {
            throw new GlobeException("字典数据ID不能为空");
        }
        DictData dictData = baseMapper.selectById(id);
        if (dictData == null) {
            throw new GlobeException("字典数据不存在");
        }
        DictDataVo vo = new DictDataVo();
        BeanUtils.copyProperties(dictData, vo);
        return vo;
    }

    @Override
    public void add(DictData dictData) {
        if (dictData.getTypeCode() == null || dictData.getTypeCode().isBlank()) {
            throw new GlobeException("字典类型编码不能为空");
        }
        if (dictData.getLabel() == null || dictData.getLabel().isBlank()) {
            throw new GlobeException("字典标签不能为空");
        }
        if (dictData.getValue() == null || dictData.getValue().isBlank()) {
            throw new GlobeException("字典值不能为空");
        }
        // 校验所属类型存在
        Long typeCount = dictTypeMapper.selectCount(
                new LambdaQueryWrapper<DictType>().eq(DictType::getTypeCode, dictData.getTypeCode())
        );
        if (typeCount == 0) {
            throw new GlobeException("字典类型【" + dictData.getTypeCode() + "】不存在");
        }
        // 校验同类型下标签唯一
        Long labelCount = baseMapper.selectCount(
                new LambdaQueryWrapper<DictData>()
                        .eq(DictData::getTypeCode, dictData.getTypeCode())
                        .eq(DictData::getLabel, dictData.getLabel())
        );
        if (labelCount > 0) {
            throw new GlobeException("字典标签【" + dictData.getLabel() + "】已存在");
        }
        // 校验同类型下字典值唯一
        Long valueCount = baseMapper.selectCount(
                new LambdaQueryWrapper<DictData>()
                        .eq(DictData::getTypeCode, dictData.getTypeCode())
                        .eq(DictData::getValue, dictData.getValue())
        );
        if (valueCount > 0) {
            throw new GlobeException("字典值【" + dictData.getValue() + "】已存在");
        }
        if (dictData.getSort() == null) {
            dictData.setSort(0);
        }
        if (dictData.getIsDefault() == null) {
            dictData.setIsDefault(false);
        }
        if (dictData.getStatus() == null) {
            dictData.setStatus(true);
        }
        baseMapper.insert(dictData);
        clearCache(dictData.getTypeCode());
    }

    @Override
    public void update(DictData dictData) {
        if (dictData.getId() == null) {
            throw new GlobeException("字典数据ID不能为空");
        }
        DictData exist = baseMapper.selectById(dictData.getId());
        if (exist == null) {
            throw new GlobeException("字典数据不存在");
        }
        // 变更生效后的目标类型编码（未传则保持原编码）
        String targetTypeCode = dictData.getTypeCode() != null ? dictData.getTypeCode() : exist.getTypeCode();
        // 变更类型编码时校验新类型存在
        if (dictData.getTypeCode() != null && !dictData.getTypeCode().equals(exist.getTypeCode())) {
            Long typeCount = dictTypeMapper.selectCount(
                    new LambdaQueryWrapper<DictType>().eq(DictType::getTypeCode, dictData.getTypeCode())
            );
            if (typeCount == 0) {
                throw new GlobeException("字典类型【" + dictData.getTypeCode() + "】不存在");
            }
        }
        // 变更标签时校验同类型下标签唯一（排除自身）
        if (dictData.getLabel() != null && !dictData.getLabel().equals(exist.getLabel())) {
            Long labelCount = baseMapper.selectCount(
                    new LambdaQueryWrapper<DictData>()
                            .eq(DictData::getTypeCode, targetTypeCode)
                            .eq(DictData::getLabel, dictData.getLabel())
                            .ne(DictData::getId, dictData.getId())
            );
            if (labelCount > 0) {
                throw new GlobeException("字典标签【" + dictData.getLabel() + "】已存在");
            }
        }
        // 变更字典值时校验同类型下字典值唯一（排除自身）
        if (dictData.getValue() != null && !dictData.getValue().equals(exist.getValue())) {
            Long valueCount = baseMapper.selectCount(
                    new LambdaQueryWrapper<DictData>()
                            .eq(DictData::getTypeCode, targetTypeCode)
                            .eq(DictData::getValue, dictData.getValue())
                            .ne(DictData::getId, dictData.getId())
            );
            if (valueCount > 0) {
                throw new GlobeException("字典值【" + dictData.getValue() + "】已存在");
            }
        }
        baseMapper.updateById(dictData);
        // 清除缓存（类型编码变更时新旧编码缓存均清除）
        clearCache(exist.getTypeCode());
        if (!targetTypeCode.equals(exist.getTypeCode())) {
            clearCache(targetTypeCode);
        }
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new GlobeException("字典数据ID不能为空");
        }
        DictData exist = baseMapper.selectById(id);
        if (exist == null) {
            throw new GlobeException("字典数据不存在");
        }
        baseMapper.deleteById(id);
        clearCache(exist.getTypeCode());
    }

    @Override
    public List<DictDataSimpleVo> listEnabledByTypeCode(String typeCode) {
        if (typeCode == null || typeCode.isBlank()) {
            throw new GlobeException("字典类型编码不能为空");
        }
        // 校验类型存在且启用
        DictType dictType = dictTypeMapper.selectOne(
                new LambdaQueryWrapper<DictType>().eq(DictType::getTypeCode, typeCode)
        );
        if (dictType == null) {
            throw new GlobeException("字典类型【" + typeCode + "】不存在");
        }
        if (!Boolean.TRUE.equals(dictType.getStatus())) {
            throw new GlobeException("字典类型【" + typeCode + "】已禁用");
        }

        String cacheKey = BaseConstant.DICT_REDIS_KEY_PREFIX + typeCode;
        // 1. 尝试从 Redis 读取
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            try {
                return objectMapper.readValue(cached, new TypeReference<List<DictDataSimpleVo>>() {});
            } catch (JsonProcessingException e) {
                // 缓存数据异常，回源数据库
            }
        }

        // 2. Redis 未命中或异常，查数据库
        List<DictDataSimpleVo> voList = loadEnabledFromDb(typeCode);

        // 3. 回写 Redis
        try {
            stringRedisTemplate.opsForValue().set(cacheKey,
                    objectMapper.writeValueAsString(voList));
        } catch (JsonProcessingException ignored) {
        }

        return voList;
    }

    /**
     * 从数据库加载启用中的字典数据列表（不查缓存）
     */
    private List<DictDataSimpleVo> loadEnabledFromDb(String typeCode) {
        List<DictData> list = baseMapper.selectList(
                new LambdaQueryWrapper<DictData>()
                        .eq(DictData::getTypeCode, typeCode)
                        .eq(DictData::getStatus, true)
                        .orderByAsc(DictData::getSort, DictData::getId)
        );
        return list.stream().map(e -> {
            DictDataSimpleVo vo = new DictDataSimpleVo();
            vo.setId(e.getId());
            vo.setLabel(e.getLabel());
            vo.setValue(e.getValue());
            vo.setSort(e.getSort());
            vo.setIsDefault(e.getIsDefault());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 清除指定类型编码的字典数据 Redis 缓存
     */
    private void clearCache(String typeCode) {
        stringRedisTemplate.delete(BaseConstant.DICT_REDIS_KEY_PREFIX + typeCode);
    }
}
