package com.cjym.videoweb.controller.client;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.WatchHistorySaveDto;
import com.cjym.videoweb.model.vo.WatchHistoryVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.WatchHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 观看历史记录（客户端）
 */
@RestController
@RequestMapping("/client/watch-history")
@Tag(name = "观看历史记录", description = "用户视频观看历史记录")
public class WatchHistoryController {

    @Resource
    private WatchHistoryService watchHistoryService;

    @PostMapping("/record")
    @Operation(summary = "记录/更新观看进度", description = "记录用户观看视频的进度（精确到秒），同一用户+视频+剧集存在则更新进度")
    public R<String> record(@RequestBody WatchHistorySaveDto dto) {
        watchHistoryService.record(dto);
        return R.success("记录观看进度成功");
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询观看历史", description = "分页查询当前用户的观看历史列表（含视频和剧集信息），每个视频仅返回最新观看的一条剧集记录，按最近观看时间倒序")
    public R<IPage<WatchHistoryVo>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return R.success(watchHistoryService.pageHistory(page, size));
    }
}
