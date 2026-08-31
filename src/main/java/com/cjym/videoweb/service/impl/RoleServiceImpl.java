package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.mapper.RoleMapper;
import com.cjym.videoweb.mapper.RoleMenuBindMapper;
import com.cjym.videoweb.mapper.UserInfoMapper;
import com.cjym.videoweb.model.dto.RoleDto;
import com.cjym.videoweb.model.entity.Role;
import com.cjym.videoweb.model.entity.RoleMenuBind;
import com.cjym.videoweb.model.entity.UserInfo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.RoleService;
import com.cjym.videoweb.service.WebConfigService;
import com.cjym.videoweb.utils.JWTUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import com.cjym.videoweb.model.vo.RoleOptionVo;
import org.springframework.transaction.annotation.Transactional;

import static com.cjym.videoweb.constant.BaseConstant.WEB_CONFIG_CATEGORY_DEFAULT;
import static com.cjym.videoweb.constant.BaseConstant.WEB_CONFIG_KEY_DEFAULT_ROLE;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Resource
    private RoleMenuBindMapper roleMenuBindMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private WebConfigService webConfigService;

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
    public R<String> updateRole(Role role, HttpServletRequest request) {
        if (role.getId() == null) {
            throw new GlobeException("角色id不能为空");
        }
        Role existRole = baseMapper.selectById(role.getId());
        if (existRole == null) {
            throw new GlobeException("角色不存在");
        }
        // 等级校验：操作者等级大于被操作角色等级则不允许
        Integer operatorLevel = getOperatorLevel(request);
        if (operatorLevel != null && existRole.getLevel() != null && operatorLevel > existRole.getLevel()) {
            return R.error("无权操作该角色");
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
    @Transactional
    public R<String> deleteRole(Long id, HttpServletRequest request) {
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
        // 等级校验：操作者等级大于被操作角色等级则不允许
        Integer operatorLevel = getOperatorLevel(request);
        if (operatorLevel != null && role.getLevel() != null && operatorLevel > role.getLevel()) {
            return R.error("无权操作该角色");
        }
        // 将该角色下的所有用户角色设置为解析出的默认角色
        Long defaultRoleId = resolveDefaultRoleId();
        LambdaUpdateWrapper<UserInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserInfo::getRoleId, id)
                     .set(UserInfo::getRoleId, defaultRoleId);
        userInfoMapper.update(null, updateWrapper);
        // 删除该角色的菜单绑定
        LambdaQueryWrapper<RoleMenuBind> bindWrapper = new LambdaQueryWrapper<>();
        bindWrapper.eq(RoleMenuBind::getRoleId, id);
        roleMenuBindMapper.delete(bindWrapper);
        // 删除角色
        baseMapper.deleteById(id);
        return R.success("删除角色成功");
    }

    /**
     * 解析默认角色ID
     * 查找顺序：
     * 1. 从web_config配置（Redis缓存优先，未命中回源数据库）读取 category=default, key=default_role 的value
     * 2. 若value为空或未找到，查询Role表中未被删除且sys_def=true的角色，取level最大的
     * 3. 若仍未找到，返回null
     */
    private Long resolveDefaultRoleId() {
        // Step 1: 从web_config配置读取默认角色ID
        String defaultValue = webConfigService.getConfigValue(WEB_CONFIG_CATEGORY_DEFAULT, WEB_CONFIG_KEY_DEFAULT_ROLE);
        if (StringUtils.isNotEmpty(defaultValue)) {
            try {
                return Long.valueOf(defaultValue);
            } catch (NumberFormatException ignored) {
                // 配置值非数字，继续后续兜底查找
            }
        }

        // Step 2: 查找未被删除且sys_def=true的角色，取level最大的
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getSysDef, true)
               .orderByDesc(Role::getLevel)
               .last("LIMIT 1");
        Role sysDefRole = baseMapper.selectOne(wrapper);
        if (sysDefRole != null) {
            return sysDefRole.getId();
        }

        // Step 3: 未找到任何默认角色
        return null;
    }

    /**
     * 从请求中获取当前操作者的角色等级
     */
    private Integer getOperatorLevel(HttpServletRequest request) {
        String token = request.getHeader("authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = JWTUtil.getUserId(token);
        UserInfo userInfo = userInfoMapper.selectById(userId);
        if (userInfo == null || userInfo.getRoleId() == null) {
            return null;
        }
        Role operatorRole = baseMapper.selectById(userInfo.getRoleId());
        return operatorRole != null ? operatorRole.getLevel() : null;
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

    @Override
    public R<List<RoleOptionVo>> getAllRoleOptions() {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Role::getId, Role::getName);
        wrapper.orderByAsc(Role::getId);
        List<Role> roles = baseMapper.selectList(wrapper);
        List<RoleOptionVo> optionList = roles.stream()
                .map(r -> new RoleOptionVo(r.getId(), r.getName()))
                .collect(Collectors.toList());
        return R.success(optionList);
    }
}