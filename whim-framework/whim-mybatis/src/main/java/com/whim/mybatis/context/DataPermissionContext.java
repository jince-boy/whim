package com.whim.mybatis.context;

import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.whim.mybatis.annotation.DataPermission;
import lombok.Getter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author jince
 * @date 2025/7/9 15:13
 * @description 数据权限上下文
 */
public class DataPermissionContext {
    public static final ThreadLocal<Deque<DataPermissionHolder>> CONTEXT = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Integer> ignoreDepth = ThreadLocal.withInitial(() -> 0);


    public static void pushPermissionHolder(DataPermissionHolder holder) {
        CONTEXT.get().push(holder);
    }

    public static void popPermissionHolder() {
        Deque<DataPermissionHolder> stack = CONTEXT.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
        if (stack.isEmpty()) {
            CONTEXT.remove();
        }
    }

    public static DataPermissionHolder currentPermissionHolder() {
        return CONTEXT.get().peek();
    }

    public static <T> T runWithIgnoreDataPermission(Supplier<T> supplier) {
        ignoreDepth.set(ignoreDepth.get() + 1);
        try {
            // 1.设置忽略数据权限插件
            if (ignoreDepth.get() == 1) {
                InterceptorIgnoreHelper.handle(IgnoreStrategy.builder().dataPermission(true).build());
            }
            // 2. 执行需要忽略权限的操作
            return supplier.get();
        } finally {
            if (ignoreDepth.get() == 1) {
                // 3.关闭忽略策略
                InterceptorIgnoreHelper.clearIgnoreStrategy();
            }
            ignoreDepth.set(ignoreDepth.get() - 1);
        }
    }
    public static class DataPermissionHolder {
        @Getter
        private final DataPermission dataPermission;
        private final Map<String, Object> params = new HashMap<>();

        public DataPermissionHolder(DataPermission dataPermission) {
            this.dataPermission = dataPermission;
        }

        public void addAttribute(String key, Object value) {
            params.put(key, value);
        }

        public <T> T getAttribute(String key, Class<T> type) {
            Object value = params.get(key);
            return type.isInstance(value) ? type.cast(value) : null;
        }
    }
}
