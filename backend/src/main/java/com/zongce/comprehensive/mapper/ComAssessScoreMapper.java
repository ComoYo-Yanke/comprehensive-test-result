package com.zongce.comprehensive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zongce.comprehensive.entity.ComAssessScore;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * 综测成绩 Mapper
 */
public interface ComAssessScoreMapper extends BaseMapper<ComAssessScore> {

    /**
     * 统计某学院已审核通过的综测平均分
     */
    @Select("SELECT AVG(s.score) FROM com_assess_score s " +
            "JOIN student st ON s.student_id = st.id " +
            "WHERE st.school_id = #{schoolId} AND s.status = 2")
    BigDecimal avgBySchool(@Param("schoolId") Long schoolId);

    /**
     * 统计某专业已审核通过的综测平均分
     */
    @Select("SELECT AVG(s.score) FROM com_assess_score s " +
            "JOIN student st ON s.student_id = st.id " +
            "JOIN student_major sm ON st.id = sm.student_id " +
            "WHERE sm.major_id = #{majorId} AND s.status = 2")
    BigDecimal avgByMajor(@Param("majorId") Long majorId);

    /**
     * 统计某班级已审核通过的综测平均分
     */
    @Select("SELECT AVG(s.score) FROM com_assess_score s " +
            "JOIN student st ON s.student_id = st.id " +
            "WHERE st.clazz_id = #{clazzId} AND s.status = 2")
    BigDecimal avgByClazz(@Param("clazzId") Long clazzId);
}
