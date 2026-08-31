package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.model.dto.UserDto;
import com.cjym.videoweb.model.dto.UserPageDto;
import com.cjym.videoweb.model.entity.UserInfo;
import com.cjym.videoweb.model.vo.UserStatsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {

    /**
     * 按用户名查询
     */
    UserInfo selectByUserName(@Param("userName") String userName);

    /**
     * 按邮箱查询
     */
    UserDto selectDtoByEmail(@Param("email") String email);

    /**
     * 多条件分页查询用户列表（含角色名称）
     */
    IPage<UserPageDto> selectUserPage(Page<UserPageDto> page,
                                      @Param("userName") String userName,
                                      @Param("nikeName") String nikeName,
                                      @Param("email") String email,
                                      @Param("status") Integer status,
                                      @Param("roleId") Long roleId,
                                      @Param("type") Integer type,
                                      @Param("isVip") Boolean isVip);

    Optional<UserDto> findByUsername(@Param("username") String username);


    UserDto selectDtoByUsername(@Param("username") String username);

    /**
     * 按邮箱查询前台用户（type=0）
     */
    UserDto selectFrontDtoByEmail(@Param("email") String email);

    /**
     * 按用户名查询前台用户（type=0）
     */
    UserDto selectFrontDtoByUsername(@Param("username") String username);

    /**
     * 按ID查询后台用户（type=1）
     */
    UserDto selectDtoById(@Param("id") Long id);

    /**
     * 按ID查询前台用户（type=0）
     */
    UserDto selectFrontDtoById(@Param("id") Long id);

    /**
     * 用户统计总览：总数/大会员/管理员 + 今日/昨日新增 + 今日/昨日活跃
     */
    UserStatsVo selectUserStats();
}
