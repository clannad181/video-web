package com.cjym.videoweb.controller.client;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.WatchHistorySaveDto;
import com.cjym.videoweb.model.dto.WatchHistorySearchDto;
import com.cjym.videoweb.model.vo.WatchHistoryMonthVo;
import com.cjym.videoweb.model.vo.WatchHistoryVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.WatchHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @Operation(summary = "记录观看进度", description = "记录用户观看视频的进度（精确到秒），每次观看新增一条历史记录")
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

    @PostMapping("/search")
    @Operation(summary = "搜索观看历史（按月份分组）", description = "按月份分组分页查询当前用户的观看历史，每页返回 size 个月且月内数据完整（月内按日期分组倒序），支持关键字聚合模糊匹配视频/剧集标题与UP主昵称、起止时间范围过滤；记录含UP主昵称与头像、不返回内部ID；仅支持查询2年内的数据（未传时间默认最近2年）")
    public R<IPage<WatchHistoryMonthVo>> search(@RequestBody WatchHistorySearchDto dto) {
        return R.success(watchHistoryService.pageHistoryByMonth(dto));
    }

    @GetMapping("/delete")
    @Operation(summary = "删除观看历史", description = "按视频avid删除当前用户的观看历史；传剧集avid时仅删除该剧集的记录，不传则删除该视频的全部历史记录")
    public R<String> delete(
            @Parameter(description = "视频avid") @RequestParam String videoUid,
            @Parameter(description = "剧集avid（可选，不传则删除该视频全部历史）") @RequestParam(required = false) String episodeUid) {
        watchHistoryService.deleteHistory(videoUid, episodeUid);
        return R.success("删除成功");
    }

    @PostMapping("/clear")
    @Operation(summary = "清空观看历史", description = "清空当前用户的全部观看历史记录")
    public R<String> clear() {
        watchHistoryService.clearHistory();
        return R.success("清空成功");
    }
}
