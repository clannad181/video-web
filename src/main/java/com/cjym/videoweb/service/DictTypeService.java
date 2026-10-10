package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.dto.DictTypePageDto;
import com.cjym.videoweb.model.entity.DictType;
import com.cjym.videoweb.model.vo.DictTypeVo;

/**
 * 字典类型 Service
 */
public interface DictTypeService extends IService<DictType> {

    /**
     * 分页查询字典类型列表（支持按编码/名称模糊搜索、状态筛选、动态排序，含字典数据条数统计）
     */
    IPage<DictTypePageDto> pageDictType(Integer page, Integer size, String typeCode, String typeName,
                                        Boolean status, String field, String descOrAsc);

    /**
     * 查询字典类型详情
     */
    DictTypeVo getDetailById(Long id);

    /**
     * 新增字典类型
     */
    void add(DictType dictType);

    /**
     * 修改字典类型（编码变更时同步更新其下字典数据）
     */
    void update(DictType dictType);

    /**
     * 删除字典类型（含系统内置校验、级联逻辑删除其下字典数据及缓存清除）
     */
    void delete(Long id);
}
