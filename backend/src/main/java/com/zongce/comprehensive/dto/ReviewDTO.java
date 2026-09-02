package com.zongce.comprehensive.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审核通用入参
 */
@Data
public class ReviewDTO {

    /** 是否通过 */
    @NotNull(message = "审核结果不能为空")
    private Boolean approve;

    /** 审核不通过原因（不通过时必填） */
    private String reason;
}
