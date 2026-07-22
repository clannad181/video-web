package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.dto.MenuDto;
import com.cjym.videoweb.model.entity.Menu;
import com.cjym.videoweb.model.vo.MenuOptionVo;
import com.cjym.videoweb.result.R;

import java.util.List;

public interface MenuService extends IService<Menu> {

    /**
     * 新增菜单
     */
    R<String> addMenu(Menu menu);

    /**
     * 修改菜单
     */
    R<String> updateMenu(Menu menu);

    /**
     * 删除菜单
     */
    R<String> deleteMenu(Long id);

    /**
     * 根据id查询菜单
     */
    R<Menu> getMenuById(Long id);

    /**
     * 多条件分页查询菜单列表（含父菜单名称）
     */
    R<IPage<MenuDto>> listMenus(Integer page, Integer size, String menuName, Boolean status);

    /**
     * 获取菜单树结构
     */
    R<List<MenuDto>> getMenuTree();

    /**
     * 获取菜单列表（仅返回id和名称）
     */
    R<List<MenuOptionVo>> getMenuOptions();
}
