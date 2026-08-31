package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.model.entity.Follow;
import com.cjym.videoweb.model.vo.FansListVo;
import com.cjym.videoweb.model.vo.FollowListVo;
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

    /**
     * 分页查询用户关注的UP主列表（含UP主公开信息与统计，按关注时间倒序）
     *
     * @param page    分页参数
     * @param userId  当前用户ID
     * @param keyword 模糊关键词（可空，匹配UP主昵称或uid）
     * @return 关注列表分页
     */
    IPage<FollowListVo> selectFollowPage(Page<FollowListVo> page, @Param("userId") Long userId, @Param("keyword") String keyword);

    /**
     * 分页查询用户粉丝列表（含粉丝公开信息、统计与是否已回关，按关注时间倒序，支持昵称/uid模糊筛选）
     *
     * @param page    分页参数
     * @param userId  当前用户ID
     * @param keyword 模糊关键词（可空，匹配粉丝昵称或uid）
     * @return 粉丝列表分页
     */
    IPage<FansListVo> selectFansPage(Page<FansListVo> page, @Param("userId") Long userId,
                                      @Param("keyword") String keyword);
}
