package com.whim.mybatis.context;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

/**
 * @author jince
 * date: 2025/7/9 15:13
 * description: 数据权限上下文
 */
public class DataPermissionContext {
    public static final ThreadLocal<Deque<DataPermissionHolder>> CONTEXT = ThreadLocal.withInitial(ArrayDeque::new);

    public static void push(DataPermissionHolder holder) {
        CONTEXT.get().push(holder);
    }

    public static void pop() {
        Deque<DataPermissionHolder> stack = CONTEXT.get();
        stack.pop();
        if (stack.isEmpty()) {
            CONTEXT.remove();
        }
    }

    public static DataPermissionHolder current() {
        return CONTEXT.get().peek();
    }

    public static <T> T ignore(Supplier<T> supplier) {
        // 1. 完全清空当前线程的权限栈
        Deque<DataPermissionHolder> originalStack = CONTEXT.get();
        CONTEXT.remove();  // 彻底移除线程绑定
        try {
            // 2. 执行需要忽略权限的操作
            return supplier.get();
        } finally {
            // 3. 恢复原始权限栈（如果存在）
            if (originalStack != null && !originalStack.isEmpty()) {
                CONTEXT.set(originalStack);
            }
        }
    }
}
