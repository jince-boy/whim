package com.whim.mybatisplus.permission;

import com.whim.core.exception.DataAccessDeniedException;
import com.whim.mybatisplus.annotation.DataPermissionTable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 线程内数据权限栈，嵌套方法的范围同时生效并在退出时恢复。
 */
public final class DataPermissionContext {
    static final ThreadLocal<Deque<Frame>> FRAMES = new ThreadLocal<>();
    static final ThreadLocal<Boolean> METADATA_READ = new ThreadLocal<>();

    /** 禁止实例化上下文。 */
    private DataPermissionContext() {
    }

    /**
     * 执行不受可见范围限制的内部完整性查询，查询期间禁止任何数据库写入。
     *
     * @param query 立即执行的只读查询，不得返回延迟执行的查询构造器
     * @param <T> 查询结果类型
     * @return 完整的内部查询结果，不用于向业务接口返回越权数据
     */
    public static <T> T getMetadata(Supplier<T> query) {
        Boolean previous = METADATA_READ.get();
        METADATA_READ.set(Boolean.TRUE);
        try {
            return query.get();
        } finally {
            // 嵌套查询恢复外层标记，最外层退出时清理线程变量，原有权限范围始终保留。
            if (previous == null) {
                METADATA_READ.remove();
            } else {
                METADATA_READ.set(previous);
            }
        }
    }

    /** 按表映射校验新增或新归属，同时遵守所有嵌套调用的范围。 */
    public static void checkOwnership(String tableName, Long ownerUserId, Long departmentId) {
        if (!active()) {
            throw new DataAccessDeniedException("当前操作缺少数据权限上下文");
        }
        boolean mapped = false;
        for (Frame frame : FRAMES.get()) {
            for (DataPermissionTable table : frame.getTables()) {
                if (!table.name().equalsIgnoreCase(tableName)) {
                    continue;
                }
                mapped = true;
                boolean allowed = frame.isAll()
                        || (!table.departmentColumn().isEmpty() && frame.getDepartmentIds().contains(departmentId))
                        || (!table.userColumn().isEmpty() && frame.isSelf()
                        && Objects.equals(frame.getUserId(), ownerUserId));
                if (!allowed) {
                    throw new DataAccessDeniedException("新增或变更归属超出本次操作的授权范围");
                }
            }
        }
        if (!mapped) {
            throw new DataAccessDeniedException("当前数据权限注解未声明目标业务表");
        }
    }

    /** 判断当前线程是否存在受保护业务操作。 */
    static boolean active() {
        Deque<Frame> frames = FRAMES.get();
        return frames != null && !frames.isEmpty();
    }

    /** 获取当前线程的范围栈，首次进入时创建。 */
    static Deque<Frame> frames() {
        Deque<Frame> frames = FRAMES.get();
        if (frames == null) {
            frames = new ArrayDeque<>();
            FRAMES.set(frames);
        }
        return frames;
    }

    /**
     * @author Jince
     * @date 2026/09/30
     * @description 单个公开服务方法的数据权限声明。
     */
    @Getter
    @RequiredArgsConstructor
    static class Frame {
        private final Long userId;
        private final boolean all;
        private final boolean self;
        private final Set<Long> departmentIds;
        private final DataPermissionTable[] tables;
    }
}
