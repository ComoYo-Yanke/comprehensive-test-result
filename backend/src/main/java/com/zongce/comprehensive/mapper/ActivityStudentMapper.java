package com.zongce.comprehensive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zongce.comprehensive.entity.ActivityStudent;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 活动-参加学生 关联 Mapper
 */
public interface ActivityStudentMapper extends BaseMapper<ActivityStudent> {

    /**
     * 统计某活动当前已参加人数
     */
    @Select("SELECT COUNT(*) FROM activity_student WHERE activity_id = #{activityId}")
    long countByActivity(@Param("activityId") Long activityId);
}
