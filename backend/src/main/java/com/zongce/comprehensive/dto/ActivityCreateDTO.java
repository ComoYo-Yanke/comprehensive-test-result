package com.zongce.comprehensive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生申请创建活动入参
 */
@Data
public class ActivityCreateDTO {

    /** 活动名称 */
    @NotBlank(message = "活动名称不能为空")
    private String name;

    /** 类型：1 校级思想 2 校级文体 3 院级思想 4 院级文体 */
    @NotNull(message = "活动类型不能为空")
    private Integer type;

    /** 创办学院 id（校级活动不填） */
    private Long schoolId;

    /** 限制人数 */
    @NotNull(message = "限制人数不能为空")
    private Integer limitNum;

    /** 开始时间 */
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    /** 结束时间 */
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    /** 描述信息 */
    private String description;

    /** 负责老师（员工）id 列表 */
    private List<Long> empInChargeIds;

    /** 负责学生 id 列表 */
    private List<Long> studentInChargeIds;
}
