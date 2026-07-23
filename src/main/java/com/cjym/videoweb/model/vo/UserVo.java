package com.cjym.videoweb.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.cjym.videoweb.model.dto.MenuDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
public class UserVo {
    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "用户邮箱")
    private String email;

    @Schema(description = "用户角色Id")
    private String roleId;
    
    @Schema(description = "用户角色名称")
    private String roleName;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "用户昵称")
    private String nikeName;

    @Schema(description = "角色等级")
    private Integer roleLevel;

    @Schema(description = "用户菜单列表")
    private List<MenuDto> menuList;
}
