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
}
