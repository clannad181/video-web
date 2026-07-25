package com.cjym.videoweb.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "运行日志趋势（单日）")
public class OperationTrendVo {

    @Schema(description = "日期")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;

    @Schema(description = "正常次数")
    private Long normalCount;

    @Schema(description = "业务异常次数")
    private Long businessException;

    @Schema(description = "系统异常次数")
    private Long systemException;
}
