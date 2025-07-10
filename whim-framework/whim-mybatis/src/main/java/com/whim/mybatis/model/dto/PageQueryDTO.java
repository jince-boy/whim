package com.whim.mybatis.model.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.Objects;

/**
 * @author jince
 * date: 2025/6/27 17:39
 * description: 分页查询参数
 */
@Data
public class PageQueryDTO {
    private Integer pageNum;
    private Integer pageSize;

    public <T> Page<T> getPage() {
        Page<T> page = new Page<>(Objects.requireNonNullElse(pageNum, 1), Objects.requireNonNullElse(pageSize, 10));
        return page;
    }
}
