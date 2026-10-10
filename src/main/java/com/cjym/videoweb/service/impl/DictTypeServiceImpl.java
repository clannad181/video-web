package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.constant.BaseConstant;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.DictDataMapper;
import com.cjym.videoweb.mapper.DictTypeMapper;
import com.cjym.videoweb.model.dto.DictTypePageDto;
import com.cjym.videoweb.model.entity.DictData;
import com.cjym.videoweb.model.entity.DictType;
import com.cjym.videoweb.model.vo.DictTypeVo;
import com.cjym.videoweb.service.DictTypeService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

/**
 * 字典类型 Service 实现
 */
@Service
public class DictTypeServiceImpl extends ServiceImpl<DictTypeMapper, DictType> implements DictTypeService {

    /**
     * 前端传参 → 数据库列名 映射，白名单校验防 SQL 注入
     */
    private static final Map<String, String> COLUMN_MAPPING = Map.of(
            "id", "id",
            "typeCode", "type_code",
            "typeName", "type_name",
            "status", "status",
            "createTime", "create_time",
            "updateTime", "update_time"
    );

    private static final Set<String> ALLOWED_DIRECTIONS = Set.of("ASC", "DESC");

    @Resource
    private DictDataMapper dictDataMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public IPage<DictTypePageDto> pageDictType(Integer page, Integer size, String typeCode, String typeName,
                                               Boolean status, String field, String descOrAsc) {
        Page<DictTypePageDto> pageParam = new Page<>(page, size);
        String orderColumn = resolveColumn(field);
        String orderDirection = resolveDirection(descOrAsc);
        return baseMapper.selectDictTypePage(pageParam, typeCode, typeName, status,
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
    public DictTypeVo getDetailById(Long id) {
        if (id == null) {
            throw new GlobeException("字典类型ID不能为空");
        }
        DictType dictType = baseMapper.selectById(id);
        if (dictType == null) {
            throw new GlobeException("字典类型不存在");
        }
        DictTypeVo vo = new DictTypeVo();
        BeanUtils.copyProperties(dictType, vo);
        return vo;
    }

    @Override
    public void add(DictType dictType) {
        if (dictType.getTypeCode() == null || dictType.getTypeCode().isBlank()) {
            throw new GlobeException("字典类型编码不能为空");
        }
        if (dictType.getTypeName() == null || dictType.getTypeName().isBlank()) {
            throw new GlobeException("字典类型名称不能为空");
        }
        // 校验编码唯一
        Long codeCount = baseMapper.selectCount(
                new LambdaQueryWrapper<DictType>().eq(DictType::getTypeCode, dictType.getTypeCode())
        );
        if (codeCount > 0) {
            throw new GlobeException("字典类型编码【" + dictType.getTypeCode() + "】已存在");
        }
        // 校验名称唯一
        Long nameCount = baseMapper.selectCount(
                new LambdaQueryWrapper<DictType>().eq(DictType::getTypeName, dictType.getTypeName())
        );
        if (nameCount > 0) {
            throw new GlobeException("字典类型名称【" + dictType.getTypeName() + "】已存在");
        }
        // 新增字典类型不允许设置为系统内置
        dictType.setIsSystem(false);
        if (dictType.getStatus() == null) {
            dictType.setStatus(true);
        }
        baseMapper.insert(dictType);
    }

    @Override
    @Transactional
    public void update(DictType dictType) {
        if (dictType.getId() == null) {
            throw new GlobeException("字典类型ID不能为空");
        }
        DictType exist = baseMapper.selectById(dictType.getId());
        if (exist == null) {
            throw new GlobeException("字典类型不存在");
        }
        // 系统内置字典不可修改编码
        if (Boolean.TRUE.equals(exist.getIsSystem())
                && dictType.getTypeCode() != null
                && !dictType.getTypeCode().equals(exist.getTypeCode())) {
            throw new GlobeException("系统内置字典不可修改编码");
        }
        // 如果修改了编码，校验是否与其他字典类型重复
        if (dictType.getTypeCode() != null && !dictType.getTypeCode().equals(exist.getTypeCode())) {
            Long codeCount = baseMapper.selectCount(
                    new LambdaQueryWrapper<DictType>()
                            .eq(DictType::getTypeCode, dictType.getTypeCode())
                            .ne(DictType::getId, dictType.getId())
            );
            if (codeCount > 0) {
                throw new GlobeException("字典类型编码【" + dictType.getTypeCode() + "】已存在");
            }
        }
        // 如果修改了名称，校验是否与其他字典类型重复
        if (dictType.getTypeName() != null && !dictType.getTypeName().equals(exist.getTypeName())) {
            Long nameCount = baseMapper.selectCount(
                    new LambdaQueryWrapper<DictType>()
                            .eq(DictType::getTypeName, dictType.getTypeName())
                            .ne(DictType::getId, dictType.getId())
            );
            if (nameCount > 0) {
                throw new GlobeException("字典类型名称【" + dictType.getTypeName() + "】已存在");
            }
        }
        baseMapper.updateById(dictType);
        // 编码变更时同步更新其下所有字典数据的 type_code，并清除新旧编码缓存
        if (dictType.getTypeCode() != null && !dictType.getTypeCode().equals(exist.getTypeCode())) {
            dictDataMapper.update(null, new LambdaUpdateWrapper<DictData>()
                    .set(DictData::getTypeCode, dictType.getTypeCode())
                    .eq(DictData::getTypeCode, exist.getTypeCode()));
            clearCache(exist.getTypeCode());
            clearCache(dictType.getTypeCode());
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new GlobeException("字典类型ID不能为空");
        }
        DictType exist = baseMapper.selectById(id);
        if (exist == null) {
            throw new GlobeException("字典类型不存在");
        }
        if (Boolean.TRUE.equals(exist.getIsSystem())) {
            throw new GlobeException("系统内置字典不可删除");
        }
        baseMapper.deleteById(id);
        // 级联逻辑删除该类型下所有字典数据
        dictDataMapper.delete(new LambdaQueryWrapper<DictData>()
                .eq(DictData::getTypeCode, exist.getTypeCode()));
        clearCache(exist.getTypeCode());
    }

    /**
     * 清除指定类型编码的字典数据 Redis 缓存
     */
    private void clearCache(String typeCode) {
        stringRedisTemplate.delete(BaseConstant.DICT_REDIS_KEY_PREFIX + typeCode);
    }
}
