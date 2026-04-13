package com.whim.mybatisplus.model.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.Objects;

/**
 * @author jince
 * @date 2026/4/13
 * @description 分页查询参数
 */
@Data
public class PageQueryDTO {
    private Integer pageNum;
    private Integer pageSize;

    public <T> Page<T> getPage() {
        return new Page<>(Objects.requireNonNullElse(pageNum, 1), Objects.requireNonNullElse(pageSize, 10));
    }
}
