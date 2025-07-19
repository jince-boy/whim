package com.whim.mybatis.core.model.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

/**
 * @author jince
 * @date 2025/6/27 17:52
 * @description 分页返回对象
 */
@Data
public class PageDataVO<T> {
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

    public PageDataVO(IPage<T> page) {
        this.currentPage = page.getCurrent();
        this.data = page.getRecords();
        this.pages = page.getPages();
        this.size = page.getSize();
        this.total = page.getTotal();
    }
}
