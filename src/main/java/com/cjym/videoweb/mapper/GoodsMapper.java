package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.model.dto.GoodsPageDto;
import com.cjym.videoweb.model.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {

    /**
     * 分页查询商品列表（关联字典数据取分类名称，支持按名称模糊、分类、状态筛选）
     */
    IPage<GoodsPageDto> selectGoodsPage(Page<GoodsPageDto> page,
                                        @Param("name") String name,
                                        @Param("categoryId") Long categoryId,
                                        @Param("status") Integer status);

    /**
     * 按ID查询商品详情（关联字典数据取分类名称）
     */
    GoodsPageDto selectDetailById(@Param("id") Long id);
}
