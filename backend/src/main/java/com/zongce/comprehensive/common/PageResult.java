package com.zongce.comprehensive.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页查询结果封装
 */
@Data
public class PageResult<T> implements Serializable {

    /** 总记录数 */
    private long total;
    /** 当前页码 */
    private long current;
    /** 每页条数 */
    private long size;
    /** 数据列表 */
    private List<T> records;

    public PageResult() {
    }

    public PageResult(long total, long current, long size, List<T> records) {
        this.total = total;
        this.current = current;
        this.size = size;
        this.records = records;
    }

    /** 将 MyBatis-Plus 分页对象转为统一分页结果 */
    public static <T> PageResult<T> of(Page<T> page) {
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }
}
