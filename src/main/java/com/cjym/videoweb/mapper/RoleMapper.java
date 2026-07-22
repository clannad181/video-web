package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.model.dto.RoleDto;
import com.cjym.videoweb.model.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 按角色名称查询
     */
    Role selectByName(@Param("name") String name);

    /**
     * 多条件分页查询（含创建人昵称）
     */
    IPage<RoleDto> selectPageList(Page<RoleDto> page,
                                  @Param("name") String name,
                                  @Param("status") Boolean status,
                                  @Param("createUserName") String createUserName);
}
