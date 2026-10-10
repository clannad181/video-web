package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.model.dto.DictDataPageDto;
import com.cjym.videoweb.model.entity.DictData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DictDataMapper extends BaseMapper<DictData> {

    /**
     * 分页查询字典数据（支持按类型编码精确筛选、标签模糊搜索、状态筛选、动态排序）
     */
    IPage<DictDataPageDto> selectDictDataPage(Page<DictDataPageDto> page,
                                              @Param("typeCode") String typeCode,
                                              @Param("label") String label,
                                              @Param("status") Boolean status,
                                              @Param("orderColumn") String orderColumn,
                                              @Param("orderDirection") String orderDirection);
}
