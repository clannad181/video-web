package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.dto.FollowListDto;
import com.cjym.videoweb.model.entity.Follow;
import com.cjym.videoweb.model.vo.FansListVo;
import com.cjym.videoweb.model.vo.FollowListVo;

public interface FollowService extends IService<Follow> {

    /**
     * 关注UP主（幂等，已关注则直接返回成功），入参为UP主uid
     */
    void follow(String followUserUid);

    /**
     * 取消关注UP主（幂等，未关注时不做任何操作），入参为UP主uid
     */
    void unfollow(String followUserUid);

    /**
     * 判断当前用户是否已关注该UP主（未登录或参数为空返回 false），入参为UP主uid
     */
    boolean isFollowed(String followUserUid);

    /**
     * 分页查询当前用户关注的UP主列表（含UP主公开信息与统计，按关注时间倒序）
     *
     * @param dto 查询请求体，page/size 缺省为 1/10
     * @return 关注列表分页
     */
    IPage<FollowListVo> pageList(FollowListDto dto);

    /**
     * 模糊搜索当前用户已关注的UP主（按昵称或uid匹配，keyword 为空时返回全部，按关注时间倒序）
     *
     * @param keyword 模糊关键词（可空）
     * @param page    页码，从1开始（默认1）
     * @param size    每页数量（默认10）
     * @return 关注列表分页
     */
    IPage<FollowListVo> pageSearch(String keyword, Integer page, Integer size);

    /**
     * 模糊搜索当前用户粉丝列表（按昵称或uid匹配，keyword 为空时返回全部，按关注时间倒序，含是否已回关）
     *
     * @param keyword 模糊关键词（可空）
     * @param page    页码，从1开始（默认1）
     * @param size    每页数量（默认10）
     * @return 粉丝列表分页
     */
    IPage<FansListVo> pageFans(String keyword, Integer page, Integer size);
}
