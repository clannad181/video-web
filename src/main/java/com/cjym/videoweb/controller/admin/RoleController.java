package com.cjym.videoweb.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.RoleDto;
import com.cjym.videoweb.model.dto.RoleMenuBindDto;
import com.cjym.videoweb.model.entity.Role;
import com.cjym.videoweb.model.vo.RoleOptionVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.RoleMenuBindService;
import com.cjym.videoweb.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/role")
@Tag(name = "角色管理", description = "角色管理")
public class RoleController {

    @Resource
    private RoleService roleService;

    @Resource
    private RoleMenuBindService roleMenuBindService;

    @PostMapping("/add")
    @Operation(summary = "新增角色", description = "新增角色")
    public R<String> addRole(@RequestBody Role role) {
        return roleService.addRole(role);
    }

    @PostMapping("/update")
    @Operation(summary = "修改角色", description = "修改角色")
    public R<String> updateRole(@RequestBody Role role, HttpServletRequest request) {
        return roleService.updateRole(role, request);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除角色", description = "删除角色")
    public R<String> deleteRole(@PathVariable Long id, HttpServletRequest request) {
        return roleService.deleteRole(id, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据id查询角色", description = "根据id查询角色")
    public R<Role> getRoleById(@PathVariable Long id) {
        return roleService.getRoleById(id);
    }

    @GetMapping("/list")
    @Operation(summary = "多条件分页查询角色列表", description = "多条件分页查询角色列表（含创建人昵称）")
    public R<IPage<RoleDto>> listRoles(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) String name,
                                       @RequestParam(required = false) Boolean status,
                                       @RequestParam(required = false) String createUserName) {
        return roleService.listRoles(page, size, name, status, createUserName);
    }

    @PostMapping("/toggleMenu")
    @Operation(summary = "切换角色菜单绑定", description = "任一菜单已绑定则取消该角色全部菜单绑定，均未绑定则批量新增")
    public R<String> toggleMenuBind(@RequestBody RoleMenuBindDto dto) {
        return roleMenuBindService.toggleBind(dto.getRoleId(), dto.getMenuIds());
    }

    @GetMapping("/getBindMenuIds")
    @Operation(summary = "获取角色绑定的菜单", description = "获取角色绑定的菜单")
    public R<List<Long>> getBindMenuIds(Long roleId) {
        return roleMenuBindService.getBindMenuIds(roleId);
    }

    @GetMapping("/options")
    @Operation(summary = "获取所有角色的id和名称", description = "获取所有角色选项（id+名称），用于下拉框")
    public R<List<RoleOptionVo>> getAllRoleOptions() {
        return roleService.getAllRoleOptions();
    }
    
}
