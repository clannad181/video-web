package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjym.videoweb.model.dto.UserDto;
import com.cjym.videoweb.model.entity.UserInfo;
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
    UserInfo selectByEmail(@Param("email") String email);

    /**
     * 条件分页查询
     */
    List<UserInfo> selectPageList(@Param("userName") String userName,
                                  @Param("email") String email,
                                  @Param("status") Integer status,
                                  @Param("type") Boolean type,
                                  @Param("isVip") Boolean isVip);

    Optional<UserDto> findByUsername(@Param("username") String username);
}
