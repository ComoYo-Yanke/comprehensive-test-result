package com.zongce.comprehensive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 员工添加违规记录入参
 */
@Data
public class PenaltyAddDTO {

    /** 学生 id */
    @NotNull(message = "学生不能为空")
    private Long studentId;

    /** 违规名称 */
    @NotBlank(message = "违规名称不能为空")
    private String name;

    /** 违规原因 */
    private String reason;

    /** 扣分分数（正数） */
    @NotNull(message = "扣分分数不能为空")
    private BigDecimal score;

    /** 处分内容：无 / 通报批评 / 其他 */
    private String punishment;
}
