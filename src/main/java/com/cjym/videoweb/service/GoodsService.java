package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.dto.GoodsPageDto;
import com.cjym.videoweb.model.entity.Goods;
import com.cjym.videoweb.model.vo.GoodsDetailVo;
import com.cjym.videoweb.model.vo.GoodsSaveVo;

/**
 * 商品 Service
 */
public interface GoodsService extends IService<Goods> {

    /**
     * 分页查询商品列表（管理端，支持按名称模糊、分类、状态筛选）
     */
    IPage<GoodsPageDto> pageGoods(Integer page, Integer size, String name, Long categoryId, Integer status);

    /**
     * 查询商品详情（管理端）
     */
    GoodsDetailVo getDetail(Long id);

    /**
     * 新增商品
     */
    void add(GoodsSaveVo vo);

    /**
     * 修改商品
     */
    void update(GoodsSaveVo vo);

    /**
     * 删除商品（逻辑删除）
     */
    void delete(Long id);
}
