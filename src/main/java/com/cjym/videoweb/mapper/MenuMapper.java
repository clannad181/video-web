package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.model.dto.MenuDto;
import com.cjym.videoweb.model.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 按菜单名称查询
     */
    Menu selectByMenuName(@Param("menuName") String menuName);

    /**
     * 多条件分页查询（含父菜单名称）
     */
    IPage<MenuDto> selectPageList(Page<MenuDto> page,
                                  @Param("menuName") String menuName,
                                  @Param("status") Boolean status);
}
