package com.whim.mybatisplus.model.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Objects;

/**
 * @author jince
 * @date 2026/4/13
 * @description 分页查询参数
 */
@Data
public class PageQueryDTO {
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;
    @NotNull(message = "每页数量不能为空")
    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 10;

    /** 按默认或已校验分页参数创建分页对象。 */
    public <T> Page<T> getPage() {
        return new Page<>(Objects.requireNonNullElse(pageNum, 1), Objects.requireNonNullElse(pageSize, 10));
    }
}
