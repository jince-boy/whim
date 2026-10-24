package com.whim.excel.core;

import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.IntFunction;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 按需拉取分页数据，只保留当前页，页码从一开始，空页结束。
 */
@RequiredArgsConstructor
public final class ExcelPageIterator<T> implements Iterator<T> {

    private final IntFunction<? extends Collection<T>> pageProvider;
    private Iterator<T> current = Collections.emptyIterator();
    private int pageNumber = 1;
    private boolean finished;

    /** 当前页耗尽时读取下一页，不预先加载后续数据。 */
    @Override
    public boolean hasNext() {
        if (!current.hasNext() && !finished) {
            Collection<T> page = pageProvider.apply(pageNumber++);
            Assert.notNull(page, "Excel 分页数据提供者必须返回集合，结束时返回空集合");
            current = page.iterator();
            finished = !current.hasNext();
        }
        return !finished;
    }

    /** 获取下一条数据，无剩余数据时按 Iterator 契约抛出异常。 */
    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException("Excel 分页数据已读取完毕");
        }
        return current.next();
    }
}
