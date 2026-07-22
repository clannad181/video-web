package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.entity.RoleMenuBind;
import com.cjym.videoweb.result.R;

import java.util.List;

public interface RoleMenuBindService extends IService<RoleMenuBind> {

    /**
     * 切换角色菜单绑定：任一菜单已绑定则删除该角色所有绑定，否则批量新增
     */
    R<String> toggleBind(Long roleId, List<Long> menuIds);

    R<List<Long>> getBindMenuIds(Long roleId);
}
