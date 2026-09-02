package com.zongce.comprehensive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zongce.comprehensive.entity.Student;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 学生 Mapper
 */
public interface StudentMapper extends BaseMapper<Student> {

    /**
     * 查询某个学院下的学生 id 列表（用于统计）
     */
    @Select("SELECT id FROM student WHERE school_id = #{schoolId}")
    List<Long> selectIdsBySchool(@Param("schoolId") Long schoolId);

    /**
     * 查询某个班级下的学生 id 列表（用于统计）
     */
    @Select("SELECT id FROM student WHERE clazz_id = #{clazzId}")
    List<Long> selectIdsByClazz(@Param("clazzId") Long clazzId);
}
