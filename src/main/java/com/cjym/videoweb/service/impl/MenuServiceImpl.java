package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.MenuMapper;
import com.cjym.videoweb.model.dto.MenuDto;
import com.cjym.videoweb.model.entity.Menu;
import com.cjym.videoweb.model.vo.MenuOptionVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.MenuService;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    @Override
    public R<String> addMenu(Menu menu) {
        if (StringUtils.isEmpty(menu.getMenuName())) {
            return R.error("菜单名称不能为空");
        }
        // 检查菜单名称是否已存在
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getMenuName, menu.getMenuName());
        if (baseMapper.selectCount(wrapper) > 0) {
            return R.error("菜单名称已存在");
        }
        // 如果设置了父菜单，校验父菜单是否存在
        if (menu.getParentId() != null && menu.getParentId() != 0) {
            Menu parentMenu = baseMapper.selectById(menu.getParentId());
            if (parentMenu == null) {
                return R.error("父菜单不存在");
            }
        }
        baseMapper.insert(menu);
        return R.success("新增菜单成功");
    }

    @Override
    public R<String> updateMenu(Menu menu) {
        if (menu.getId() == null) {
            return R.error("菜单id不能为空");
        }
        Menu existMenu = baseMapper.selectById(menu.getId());
        if (existMenu == null) {
            return R.error("菜单不存在");
        }
        // 不能将父菜单设为自己
        if (menu.getParentId() != null && menu.getParentId().equals(menu.getId())) {
            return R.error("父菜单不能为自己");
        }
        // 如果修改了父菜单，校验父菜单是否存在
        if (menu.getParentId() != null && menu.getParentId() != 0
                && !menu.getParentId().equals(existMenu.getParentId())) {
            Menu parentMenu = baseMapper.selectById(menu.getParentId());
            if (parentMenu == null) {
                return R.error("父菜单不存在");
            }
        }
        // 检查菜单名称是否与其他菜单重复
        if (StringUtils.isNotEmpty(menu.getMenuName())
                && !menu.getMenuName().equals(existMenu.getMenuName())) {
            LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Menu::getMenuName, menu.getMenuName());
            if (baseMapper.selectCount(wrapper) > 0) {
                return R.error("菜单名称已存在");
            }
        }
        baseMapper.updateById(menu);
        return R.success("修改菜单成功");
    }

    @Override
    public R<String> deleteMenu(Long id) {
        if (id == null) {
            return R.error("菜单id不能为空");
        }
        Menu menu = baseMapper.selectById(id);
        if (menu == null) {
            return R.error("菜单不存在");
        }
        // 检查是否有子菜单
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getParentId, id);
        if (baseMapper.selectCount(wrapper) > 0) {
            return R.error("存在子菜单，无法删除");
        }
        baseMapper.deleteById(id);
        return R.success("删除菜单成功");
    }

    @Override
    public R<Menu> getMenuById(Long id) {
        if (id == null) {
            return R.error("菜单id不能为空");
        }
        Menu menu = baseMapper.selectById(id);
        if (menu == null) {
            return R.error("菜单不存在");
        }
        return R.success(menu);
    }

    @Override
    public R<IPage<MenuDto>> listMenus(Integer page, Integer size, String menuName, Boolean status) {
        Page<MenuDto> pageParam = new Page<>(page, size);
        IPage<MenuDto> result = baseMapper.selectPageList(pageParam, menuName, status);
        return R.success(result);
    }

    @Override
    public R<List<MenuDto>> getMenuTree() {
        // 查询所有未删除的菜单
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Menu::getDescSort, Menu::getId);
        List<Menu> allMenus = baseMapper.selectList(wrapper);

        // 转换为 MenuDto 列表
        List<MenuDto> allDtos = allMenus.stream().map(m -> {
            MenuDto dto = new MenuDto();
            dto.setId(m.getId());
            dto.setMenuName(m.getMenuName());
            dto.setMenuIdentification(m.getMenuIdentification());
            dto.setStatus(m.getStatus());
            dto.setParentId(m.getParentId());
            dto.setDescSort(m.getDescSort());
            dto.setIcon(m.getIcon());
            dto.setCreateTime(m.getCreateTime());
            dto.setIsDelete(m.getIsDelete());
            return dto;
        }).collect(Collectors.toList());

        // 建立 id → MenuDto 的映射，便于查找父节点
        Map<Long, MenuDto> idMap = allDtos.stream()
                .collect(Collectors.toMap(MenuDto::getId, d -> d));

        // 为每个节点设置父菜单名称，并挂载到父节点的 children 中
        for (MenuDto dto : allDtos) {
            if (dto.getParentId() != null && dto.getParentId() != 0) {
                MenuDto parent = idMap.get(dto.getParentId());
                if (parent != null) {
                    dto.setParentMenuName(parent.getMenuName());
                    parent.getChildren().add(dto);
                }
            }
        }

        // 返回根节点（已自动包含子节点）
        List<MenuDto> treeRoots = allDtos.stream()
                .filter(d -> d.getParentId() == null || d.getParentId() == 0)
                .collect(Collectors.toList());
        return R.success(treeRoots);
    }

    @Override
    public R<List<MenuOptionVo>> getMenuOptions() {
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Menu::getId, Menu::getMenuName, Menu::getParentId, Menu::getIcon)
               .orderByAsc(Menu::getDescSort, Menu::getId);
        List<Menu> menus = baseMapper.selectList(wrapper);

        // 转为 VO 并建立 id → VO 映射
        List<MenuOptionVo> allVos = menus.stream()
                .map(m -> new MenuOptionVo(m.getId(), m.getMenuName(), m.getParentId(), m.getIcon()))
                .collect(Collectors.toList());
        Map<Long, List<MenuOptionVo>> parentIdMap = allVos.stream()
                .filter(v -> v.getParentId() != null && v.getParentId() != 0)
                .collect(Collectors.groupingBy(MenuOptionVo::getParentId));

        // 构建树形结构
        List<MenuOptionVo> tree = new ArrayList<>();
        for (MenuOptionVo vo : allVos) {
            if (vo.getParentId() == null || vo.getParentId() == 0) {
                tree.add(vo);
            }
            List<MenuOptionVo> children = parentIdMap.get(vo.getId());
            if (children != null) {
                vo.setChildren(children);
            }
        }
        return R.success(tree);
    }
}
