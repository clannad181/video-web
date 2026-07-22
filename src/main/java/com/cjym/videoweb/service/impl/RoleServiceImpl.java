package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.RoleMapper;
import com.cjym.videoweb.model.dto.RoleDto;
import com.cjym.videoweb.model.entity.Role;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.RoleService;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public R<String> addRole(Role role) {
        if (StringUtils.isEmpty(role.getName())) {
            return R.error("角色名称不能为空");
        }
        // 检查角色名称是否已存在
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getName, role.getName());
        if (baseMapper.selectCount(wrapper) > 0) {
           return R.error("角色名称已存在");
        }
        baseMapper.insert(role);
        return R.success("新增角色成功");
    }

    @Override
    public R<String> updateRole(Role role) {
        if (role.getId() == null) {
            throw new GlobeException("角色id不能为空");
        }
        Role existRole = baseMapper.selectById(role.getId());
        if (existRole == null) {
            throw new GlobeException("角色不存在");
        }
        // 检查角色名称是否与其他角色重复
        if (StringUtils.isNotEmpty(role.getName()) && !role.getName().equals(existRole.getName())) {
            LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Role::getName, role.getName());
            if (baseMapper.selectCount(wrapper) > 0) {
                throw new GlobeException("角色名称已存在");
            }
        }
        baseMapper.updateById(role);
        return R.success("修改角色成功");
    }

    @Override
    public R<String> deleteRole(Long id) {
        if (id == null) {
            return R.error("角色id不能为空");
        }
        Role role = baseMapper.selectById(id);
        if (role == null) {
            return R.error("角色不存在");
        }
        if (role.getSysDef()) {
            return R.error("系统保留角色不能删除");
        }
        baseMapper.deleteById(id);
        return R.success("删除角色成功");
    }

    @Override
    public R<Role> getRoleById(Long id) {
        if (id == null) {
            throw new GlobeException("角色id不能为空");
        }
        Role role = baseMapper.selectById(id);
        if (role == null) {
            throw new GlobeException("角色不存在");
        }
        return R.success(role);
    }

    @Override
    public R<IPage<RoleDto>> listRoles(Integer page, Integer size, String name, Boolean status, String createUserName) {
        Page<RoleDto> pageParam = new Page<>(page, size);
        IPage<RoleDto> result = baseMapper.selectPageList(pageParam, name, status, createUserName);
        return R.success(result);
    }
}
