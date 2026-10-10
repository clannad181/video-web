package com.cjym.videoweb.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.MenuDto;
import com.cjym.videoweb.model.entity.Menu;
import com.cjym.videoweb.model.vo.MenuOptionVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/menu")
@Tag(name = "菜单管理", description = "菜单管理")
public class MenuController {

    @Resource
    private MenuService menuService;

//    @PostMapping("/add")
//    @Operation(summary = "新增菜单", description = "新增菜单")
//    public R<String> addMenu(@RequestBody Menu menu) {
//        return menuService.addMenu(menu);
//    }

    @PostMapping("/update")
    @Operation(summary = "修改菜单", description = "修改菜单")
    public R<String> updateMenu(@RequestBody Menu menu) {
        return menuService.updateMenu(menu);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除菜单", description = "删除菜单")
    public R<String> deleteMenu(@PathVariable Long id) {
        return menuService.deleteMenu(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据id查询菜单", description = "根据id查询菜单")
    public R<Menu> getMenuById(@PathVariable Long id) {
        return menuService.getMenuById(id);
    }

    @GetMapping("/list")
    @Operation(summary = "多条件分页查询菜单列表", description = "仅一级菜单参与分页（含菜单名称模糊搜索、状态筛选），子菜单组装在各一级菜单的children中")
    public R<IPage<MenuDto>> listMenus(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) String menuName,
                                       @RequestParam(required = false) Boolean status) {
        return menuService.listMenus(page, size, menuName, status);
    }

    @GetMapping("/tree")
    @Operation(summary = "获取菜单树", description = "获取完整的菜单树结构")
    public R<List<MenuDto>> getMenuTree() {
        return menuService.getMenuTree();
    }

    @GetMapping("/options")
    @Operation(summary = "获取菜单选项列表", description = "获取全部菜单id和名称，用于下拉选择")
    public R<List<MenuOptionVo>> getMenuOptions() {
        return menuService.getMenuOptions();
    }
}
