package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjym.videoweb.model.entity.Follow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface FollowMapper extends BaseMapper<Follow> {

    /**
     * 恢复已取关的记录（is_delete 1→0），返回影响行数
     */
    @Update("UPDATE public.user_follow SET is_delete = 0 WHERE user_id = #{userId} AND follow_user_id = #{followUserId} AND is_delete = 1")
    int restoreFollow(@Param("userId") Long userId, @Param("followUserId") Long followUserId);
}
