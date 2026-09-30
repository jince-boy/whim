package com.whim.mybatisplus.permission;

import com.whim.core.exception.DataAccessDeniedException;
import com.whim.core.permission.DataPermissionScope;
import com.whim.mybatisplus.annotation.DataPermissionTable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 线程内数据权限栈，嵌套方法的范围同时生效并在退出时恢复。
 */
public final class DataPermissionContext {
    static final ThreadLocal<Deque<Frame>> FRAMES = new ThreadLocal<>();

    /** 禁止实例化上下文。 */
    private DataPermissionContext() {
    }

    /** 读取当前注解解析的范围，供新增及归属变更校验使用。 */
    public static DataPermissionScope requiredScope() {
        Deque<Frame> frames = FRAMES.get();
        if (frames == null || frames.isEmpty()) {
            throw new DataAccessDeniedException("当前操作缺少数据权限上下文");
        }
        return frames.peek().getScope();
    }

    /** 按表映射校验新增或新归属，同时遵守所有嵌套调用的范围。 */
    public static void checkOwnership(String tableName, Long ownerUserId, Long departmentId) {
        requiredScope();
        boolean mapped = false;
        for (Frame frame : FRAMES.get()) {
            for (DataPermissionTable table : frame.getTables()) {
                if (!table.name().equalsIgnoreCase(tableName)) {
                    continue;
                }
                mapped = true;
                DataPermissionScope scope = frame.getScope();
                boolean allowed = scope.isAll()
                        || (!table.departmentColumn().isEmpty() && scope.getDepartmentIds().contains(departmentId))
                        || (!table.userColumn().isEmpty() && scope.isSelf()
                        && Objects.equals(scope.getUserId(), ownerUserId));
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
        private final DataPermissionScope scope;
        private final DataPermissionTable[] tables;
    }
}
