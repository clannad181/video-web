package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.CategoryMapper;
import com.cjym.videoweb.model.dto.CategoryPageDto;
import com.cjym.videoweb.model.entity.Category;
import com.cjym.videoweb.model.vo.CategoryVo;
import com.cjym.videoweb.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 视频分类 Service 实现
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public IPage<CategoryPageDto> pageCategory(Integer page, Integer size, String name) {
        Page<CategoryPageDto> pageParam = new Page<>(page, size);
        return baseMapper.selectCategoryPage(pageParam, name);
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
        if (category.getStatus() == null) {
            category.setStatus(true);
        }
        baseMapper.insert(category);
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
    }

    @Override
    public List<CategoryVo> listEnabled() {
        List<Category> list = baseMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, true)
                        .orderByAsc(Category::getSort, Category::getId)
        );
        return list.stream().map(e -> {
            CategoryVo vo = new CategoryVo();
            BeanUtils.copyProperties(e, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}
