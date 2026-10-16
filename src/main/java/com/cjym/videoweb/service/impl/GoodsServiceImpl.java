package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.constant.GoodsConstant;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.DictDataMapper;
import com.cjym.videoweb.mapper.GoodsMapper;
import com.cjym.videoweb.model.dto.GoodsPageDto;
import com.cjym.videoweb.model.entity.DictData;
import com.cjym.videoweb.model.entity.Goods;
import com.cjym.videoweb.model.vo.GoodsDetailVo;
import com.cjym.videoweb.model.vo.GoodsSaveVo;
import com.cjym.videoweb.service.GoodsService;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    @Resource
    private DictDataMapper dictDataMapper;

    @Override
    public IPage<GoodsPageDto> pageGoods(Integer page, Integer size, String name, Long categoryId, Integer status) {
        Page<GoodsPageDto> pageParam = new Page<>(page == null ? 1 : page, size == null ? 10 : size);
        return baseMapper.selectGoodsPage(pageParam, name, categoryId, status);
    }

    @Override
    public GoodsDetailVo getDetail(Long id) {
        if (id == null) {
            throw new GlobeException("商品ID不能为空");
        }
        GoodsPageDto dto = baseMapper.selectDetailById(id);
        if (dto == null) {
            throw new GlobeException("商品不存在");
        }
        Goods goods = baseMapper.selectById(id);
        GoodsDetailVo vo = new GoodsDetailVo();
        BeanUtils.copyProperties(dto, vo);
        vo.setImages(strToImages(goods == null ? null : goods.getImages()));
        vo.setDescription(goods == null ? null : goods.getDescription());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(GoodsSaveVo vo) {
        validate(vo);
        Goods goods = new Goods();
        applySaveVo(goods, vo);
        save(goods);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(GoodsSaveVo vo) {
        if (vo == null || vo.getId() == null) {
            throw new GlobeException("商品ID不能为空");
        }
        Goods exist = baseMapper.selectById(vo.getId());
        if (exist == null) {
            throw new GlobeException("商品不存在");
        }
        validate(vo);
        applySaveVo(exist, vo);
        updateById(exist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (id == null) {
            throw new GlobeException("商品ID不能为空");
        }
        Goods exist = baseMapper.selectById(id);
        if (exist == null) {
            throw new GlobeException("商品不存在");
        }
        removeById(id);
    }

    /**
     * 新增/修改参数校验：名称、分类、价格、状态
     */
    private void validate(GoodsSaveVo vo) {
        if (vo == null) {
            throw new GlobeException("请求参数不能为空");
        }
        if (StringUtils.isEmpty(vo.getName())) {
            throw new GlobeException("商品名称不能为空");
        }
        if (vo.getCategoryId() == null) {
            throw new GlobeException("商品分类不能为空");
        }
        // 分类必须存在于商品分类字典中（type_code = goods_category）
        DictData category = dictDataMapper.selectOne(new LambdaQueryWrapper<DictData>()
                .eq(DictData::getId, vo.getCategoryId())
                .eq(DictData::getTypeCode, GoodsConstant.DICT_TYPE_CODE));
        if (category == null) {
            throw new GlobeException("商品分类不存在");
        }
        if (vo.getOriginalPrice() == null || vo.getOriginalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new GlobeException("原价必须大于0");
        }
        if (vo.getDiscountPrice() != null) {
            if (vo.getDiscountPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new GlobeException("优惠价必须大于0");
            }
            if (vo.getDiscountPrice().compareTo(vo.getOriginalPrice()) >= 0) {
                throw new GlobeException("优惠价必须低于原价");
            }
        }
        if (vo.getStatus() == null) {
            throw new GlobeException("商品状态不能为空");
        }
        if (vo.getStatus() != GoodsConstant.STATUS_OFF && vo.getStatus() != GoodsConstant.STATUS_ON) {
            throw new GlobeException("商品状态不合法");
        }
    }

    /**
     * 将请求参数写入实体（新增/修改共用）
     */
    private void applySaveVo(Goods goods, GoodsSaveVo vo) {
        goods.setCategoryId(vo.getCategoryId());
        goods.setName(vo.getName());
        goods.setOriginalPrice(vo.getOriginalPrice());
        goods.setDiscountPrice(vo.getDiscountPrice());
        goods.setCover(vo.getCover());
        goods.setImages(imagesToStr(vo.getImages()));
        goods.setDescription(vo.getDescription());
        goods.setStatus(vo.getStatus());
        goods.setSort(vo.getSort() == null ? 0 : vo.getSort());
    }

    /**
     * 图片URL列表 → 逗号分隔字符串
     */
    private String imagesToStr(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(","));
    }

    /**
     * 逗号分隔字符串 → 图片URL列表
     */
    private List<String> strToImages(String imagesStr) {
        if (imagesStr == null || imagesStr.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(imagesStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
