package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.dto.CategoryPageDto;
import com.cjym.videoweb.model.entity.Category;
import com.cjym.videoweb.model.vo.CategorySimpleVo;
import com.cjym.videoweb.model.vo.CategoryVo;

import java.util.List;

/**
 * 视频分类 Service
 */
public interface CategoryService extends IService<Category> {

    /**
     * 分页查询分类列表（支持按名称模糊搜索、状态、是否显示筛选、动态排序）
     */
    IPage<CategoryPageDto> pageCategory(Integer page, Integer size, String name,
                                        Boolean status, Boolean isShow,
                                        String field, String descOrAsc);

    /**
     * 查询分类详情
     */
    CategoryVo getDetailById(Long id);

    /**
     * 新增分类
     */
    void add(Category category);

    /**
     * 修改分类
     */
    void update(Category category);

    /**
     * 删除分类（含系统保留分类校验及缓存清除）
     */
    void delete(Long id);

    /**
     * 获取所有启用中的分类列表（仅返回id/name/sort，供下拉选择使用）
     */
    List<CategorySimpleVo> listEnabled();
}
