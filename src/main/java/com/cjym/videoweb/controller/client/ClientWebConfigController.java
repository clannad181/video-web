package com.cjym.videoweb.controller.client;

import com.cjym.videoweb.model.vo.WebConfigVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.WebConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.cjym.videoweb.constant.BaseConstant.WEB_CONFIG_CATEGORY_DISPLAY;

/**
 * 网站配置查询（客户端）
 */
@RestController
@RequestMapping("/client/config")
@Tag(name = "网站配置", description = "前台获取网站展示相关配置")
public class ClientWebConfigController {

    @Resource
    private WebConfigService webConfigService;

    @GetMapping("/display")
    @Operation(summary = "查询展示配置", description = "查询分类为 display 的网站配置项，仅返回 id/category/key/value")
    public R<List<WebConfigVo>> displayConfig() {
        return webConfigService.list(WEB_CONFIG_CATEGORY_DISPLAY);
    }
}
