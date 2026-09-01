package com.cjym.videoweb.controller.client;

import com.cjym.videoweb.model.vo.DataCenterVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据中心（客户端）
 */
@RestController
@RequestMapping("/client/stats")
@Tag(name = "数据中心", description = "前台个人投稿数据统计")
public class ClientStatsController {

    @Resource
    private VideoService videoService;

    @GetMapping("/data-center")
    @Operation(summary = "个人数据中心", description = "统计当前登录用户所有投稿的总观看数、总收藏数、总投币数、总点赞数，以及最热视频TOP5（观看数最多）与最受欢迎视频TOP5（收藏数最多），需登录")
    public R<DataCenterVo> dataCenter() {
        return R.success(videoService.getDataCenter());
    }
}
