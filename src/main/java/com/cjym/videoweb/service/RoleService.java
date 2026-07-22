package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.dto.RoleDto;
import com.cjym.videoweb.model.entity.Role;
import com.cjym.videoweb.result.R;

public interface RoleService extends IService<Role> {

    /**
     * 新增角色
     */
    R<String> addRole(Role role);

    /**
     * 修改角色
     */
    R<String> updateRole(Role role);

    /**
     * 删除角色
     */
    R<String> deleteRole(Long id);

    /**
     * 根据id查询角色
     */
    R<Role> getRoleById(Long id);

    /**
     * 多条件分页查询角色列表（含创建人昵称）
     */
    R<IPage<RoleDto>> listRoles(Integer page, Integer size, String name, Boolean status, String createUserName);
}
