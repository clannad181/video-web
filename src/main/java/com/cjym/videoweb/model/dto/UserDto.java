package com.cjym.videoweb.model.dto;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Data
public class UserDto implements UserDetails {
    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "用户密码")
    private String password;

    @Schema(description = "用户邮箱")
    private String email;

    @Schema(description = "用户角色Id")
    private String roleId;

    @Schema(description = "用户状态 0-正常，1-禁用")
    private Integer status;

    @Schema(description = "盐值")
    private String salt;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "用户昵称")
    private String nikeName;

    private String roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(roles.split(","))
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    public String getUsername() {
        return this.userName;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return this.status==0; }
}
