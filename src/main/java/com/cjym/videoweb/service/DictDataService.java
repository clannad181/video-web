package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.dto.DictDataPageDto;
import com.cjym.videoweb.model.entity.DictData;
import com.cjym.videoweb.model.vo.DictDataSimpleVo;
import com.cjym.videoweb.model.vo.DictDataVo;

import java.util.List;

/**
 * 字典数据 Service
 */
public interface DictDataService extends IService<DictData> {

    /**
     * 分页查询字典数据列表（支持按类型编码精确筛选、标签模糊搜索、状态筛选、动态排序）
     */
    IPage<DictDataPageDto> pageDictData(Integer page, Integer size, String typeCode, String label,
                                        Boolean status, String field, String descOrAsc);

    /**
     * 查询字典数据详情
     */
    DictDataVo getDetailById(Long id);

    /**
     * 新增字典数据
     */
    void add(DictData dictData);

    /**
     * 修改字典数据
     */
    void update(DictData dictData);

    /**
     * 删除字典数据（含缓存清除）
     */
    void delete(Long id);

    /**
     * 按类型编码获取启用中的字典数据列表（仅返回id/label/value/sort/isDefault，Redis缓存优先）
     */
    List<DictDataSimpleVo> listEnabledByTypeCode(String typeCode);
}
