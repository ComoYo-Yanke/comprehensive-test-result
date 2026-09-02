package com.zongce.comprehensive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 学生添加其他综测加分项入参
 */
@Data
public class ExtraItemAddDTO {

    /** 加分项名称 */
    @NotBlank(message = "加分项名称不能为空")
    private String name;

    /** 佐证材料（图片 URL） */
    private String evidence;

    /** 加分分数 */
    @NotNull(message = "分数不能为空")
    private BigDecimal score;

    /** 描述信息 */
    private String description;
}
