package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.model.dto.DictTypePageDto;
import com.cjym.videoweb.model.entity.DictType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface DictTypeMapper extends BaseMapper<DictType> {

    /**
     * 分页查询字典类型（支持按编码/名称模糊搜索、状态筛选、动态排序，含该类型下字典数据条数统计）
     */
    IPage<DictTypePageDto> selectDictTypePage(Page<DictTypePageDto> page,
                                              @Param("typeCode") String typeCode,
                                              @Param("typeName") String typeName,
                                              @Param("status") Boolean status,
                                              @Param("orderColumn") String orderColumn,
                                              @Param("orderDirection") String orderDirection);
}
