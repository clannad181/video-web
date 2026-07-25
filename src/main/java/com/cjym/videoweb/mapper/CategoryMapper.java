package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.model.dto.CategoryPageDto;
import com.cjym.videoweb.model.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 视频分类 Mapper
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 分页查询分类（支持按名称模糊搜索）
     */
    IPage<CategoryPageDto> selectCategoryPage(Page<CategoryPageDto> page,
                                               @Param("name") String name);
}
