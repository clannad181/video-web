package com.cjym.videoweb.service.impl;

import com.cjym.videoweb.model.vo.DailyIncrementVo;
import com.cjym.videoweb.service.CommentService;
import com.cjym.videoweb.service.StatsService;
import com.cjym.videoweb.service.VideoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsServiceImpl implements StatsService {

    @Resource
    private VideoService videoService;

    @Resource
    private CommentService commentService;

    @Override
    public List<DailyIncrementVo> getDailyIncrement() {
        List<DailyIncrementVo> videoList = videoService.getDailyIncrement();
        List<DailyIncrementVo> commentList = commentService.getDailyIncrement();

        Map<LocalDate, Long> commentMap = commentList.stream()
                .collect(Collectors.toMap(DailyIncrementVo::getDate, DailyIncrementVo::getCommentCount));

        List<DailyIncrementVo> result = new ArrayList<>(7);
        for (DailyIncrementVo videoVo : videoList) {
            DailyIncrementVo vo = new DailyIncrementVo();
            vo.setDate(videoVo.getDate());
            vo.setVideoCount(videoVo.getVideoCount() != null ? videoVo.getVideoCount() : 0L);
            vo.setCommentCount(commentMap.getOrDefault(videoVo.getDate(), 0L));
            result.add(vo);
        }
        return result;
    }
}
