package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjym.videoweb.model.entity.UserProfile;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户画像 Mapper
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {

    /**
     * 物理删除指定用户的全部画像数据（画像为派生数据，重建时物理清理避免数据膨胀）
     */
    @Delete("DELETE FROM public.user_profile WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 物理删除时间窗内无观看记录用户的画像（保持画像表与观看历史一致）
     */
    @Delete("DELETE FROM public.user_profile WHERE user_id NOT IN "
            + "(SELECT DISTINCT user_id FROM public.watch_history WHERE is_delete = 0 AND watch_time >= #{since})")
    int deleteInactiveProfiles(@Param("since") java.time.LocalDateTime since);
}
