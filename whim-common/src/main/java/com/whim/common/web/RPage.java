package com.whim.common.web;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author Jince
 * date: 2024/10/4 00:36
 * description: 分页对象
 */
@Data
@AllArgsConstructor
public class RPage<T> {
    /**
     * 当前页
     */
    private Long currentPage;
    /**
     * 分页数据
     */
    private List<T> data;
    /**
     * 总页数
     */
    private Long pages;
    /**
     * 每页数量
     */
    private Long size;
    /**
     * 总数量
     */
    private Long total;
}

