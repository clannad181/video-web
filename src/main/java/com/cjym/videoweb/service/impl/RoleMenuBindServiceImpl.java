package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.mapper.RoleMenuBindMapper;
import com.cjym.videoweb.model.entity.RoleMenuBind;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.RoleMenuBindService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoleMenuBindServiceImpl extends ServiceImpl<RoleMenuBindMapper, RoleMenuBind> implements RoleMenuBindService {

    @Resource
    private RoleMenuBindMapper roleMenuBindMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> toggleBind(Long roleId, List<Long> menuIds) {
        if (roleId == null || menuIds == null || menuIds.isEmpty()) {
            return R.error("角色id和菜单id不能为空");
        }

        // 查询该角色下是否已有任意一个菜单的绑定
        LambdaQueryWrapper<RoleMenuBind> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleMenuBind::getRoleId, roleId)
               .in(RoleMenuBind::getMenuId, menuIds);
        Long existCount = baseMapper.selectCount(wrapper);

        if (existCount != null && existCount > 0) {
            // 已存在 → 删除该角色下所有绑定的菜单
            LambdaQueryWrapper<RoleMenuBind> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(RoleMenuBind::getRoleId, roleId);
            baseMapper.delete(deleteWrapper);
        }
        List<RoleMenuBind> bindList = new ArrayList<>();
        for (Long menuId : menuIds) {
            RoleMenuBind bind = new RoleMenuBind();
            bind.setRoleId(roleId);
            bind.setMenuId(menuId);
            bind.setCreateTime(LocalDateTime.now());
            bindList.add(bind);
        }
        saveBatch(bindList);
        return R.success("菜单绑定成功");
    }

    @Override
    public R<List<Long>> getBindMenuIds(Long roleId) {
        if (roleId == null) {
            return R.error("角色id不能为空");
        }
        LambdaQueryWrapper<RoleMenuBind>  wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleMenuBind::getRoleId, roleId);
        wrapper.select(RoleMenuBind::getMenuId);
        List<RoleMenuBind> roleMenuBinds = roleMenuBindMapper.selectList(wrapper);
        List<Long> menuIds = roleMenuBinds.stream().map(RoleMenuBind::getMenuId).toList();
        return R.success(menuIds);
    }
}
