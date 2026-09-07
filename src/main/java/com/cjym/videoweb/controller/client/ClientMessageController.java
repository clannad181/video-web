package com.cjym.videoweb.controller.client;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.MessageReadDto;
import com.cjym.videoweb.model.vo.MessageVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台消息通知
 */
@RestController
@RequestMapping("/client/message")
@Tag(name = "消息通知（前台）", description = "前台用户消息通知接口")
public class ClientMessageController {

    @Resource
    private MessageService messageService;

    /**
     * 分页查询我的消息
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询我的消息", description = "分页查询当前登录用户的消息，支持按类型（1-视频发布成功，2-稿件审核通知，3-举报下架通知，4-服务器状态通知）与已读状态（0-未读，1-已读）筛选，按创建时间倒序")
    public R<IPage<MessageVo>> list(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size,
                                    @RequestParam(required = false) Integer type,
                                    @RequestParam(required = false) Integer isRead) {
        return R.success(messageService.pageMyMessages(page, size, type, isRead));
    }

    /**
     * 未读消息数
     */
    @GetMapping("/unreadCount")
    @Operation(summary = "未读消息数", description = "查询当前登录用户的未读消息数量")
    public R<Long> unreadCount() {
        return R.success(messageService.unreadCount());
    }

    /**
     * 标记单条消息已读
     */
    @PostMapping("/read")
    @Operation(summary = "标记单条消息已读", description = "按消息avid标记单条消息已读（仅接收者可操作）")
    public R<String> read(@RequestBody MessageReadDto dto) {
        messageService.markRead(dto.getMessageUid());
        return R.success("标记已读成功");
    }

    /**
     * 全部标记已读
     */
    @PostMapping("/readAll")
    @Operation(summary = "全部标记已读", description = "将当前登录用户的全部未读消息标记为已读")
    public R<String> readAll() {
        messageService.markAllRead();
        return R.success("全部标记已读成功");
    }
}
