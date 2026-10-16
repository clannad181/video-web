package com.cjym.videoweb.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.GoodsPageDto;
import com.cjym.videoweb.model.vo.GoodsDetailVo;
import com.cjym.videoweb.model.vo.GoodsSaveVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.GoodsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品管理（管理端）
 * 商品分类复用字典体系（type_code=goods_category），分类项由字典数据接口维护；
 * 图片通过文件上传接口（POST /file/upload）上传后返回 URL
 */
@RestController
@RequestMapping("/admin/goods")
@Tag(name = "商品管理", description = "商品管理（分类走字典体系 goods_category）")
public class GoodsController {

    @Resource
    private GoodsService goodsService;

    @GetMapping("/list")
    @Operation(summary = "分页查询商品列表", description = "支持按商品名称模糊搜索、分类、状态（0-下架，1-上架）筛选，按排序升序、创建时间倒序")
    public R<IPage<GoodsPageDto>> list(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) String name,
                                       @RequestParam(required = false) Long categoryId,
                                       @RequestParam(required = false) Integer status) {
        return R.success(goodsService.pageGoods(page, size, name, categoryId, status));
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "查询商品详情", description = "按商品ID查询商品详情（含详情图列表、描述）")
    public R<GoodsDetailVo> detail(@PathVariable Long id) {
        return R.success(goodsService.getDetail(id));
    }

    @PostMapping("/add")
    @Operation(summary = "新增商品", description = "新增商品，原价必填大于0，优惠价选填且必须低于原价，分类为商品分类字典数据ID")
    public R<String> add(@RequestBody GoodsSaveVo vo) {
        goodsService.add(vo);
        return R.success("新增成功");
    }

    @PostMapping("/update")
    @Operation(summary = "修改商品", description = "修改商品（含上下架、价格、图片等信息），校验规则同新增")
    public R<String> update(@RequestBody GoodsSaveVo vo) {
        goodsService.update(vo);
        return R.success("修改成功");
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除商品", description = "按商品ID逻辑删除商品")
    public R<String> delete(@PathVariable Long id) {
        goodsService.delete(id);
        return R.success("删除成功");
    }
}
