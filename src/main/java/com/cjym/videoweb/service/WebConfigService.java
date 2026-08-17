package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.entity.WebConfig;
import com.cjym.videoweb.model.vo.WebConfigVo;
import com.cjym.videoweb.result.R;

import java.util.List;

public interface WebConfigService extends IService<WebConfig> {

    /**
     * 查询配置列表（可按分类筛选）
     */
    R<List<WebConfigVo>> list(String category);

    /**
     * 批量更新配置
     */
    R<String> update(List<WebConfigVo> configList);

    /**
     * 根据分类和键查询配置值（Redis缓存优先，未命中回源数据库）
     *
     * @param category 配置分类
     * @param key      配置键
     * @return 配置值，未找到返回null
     */
    String getConfigValue(String category, String key);
}
