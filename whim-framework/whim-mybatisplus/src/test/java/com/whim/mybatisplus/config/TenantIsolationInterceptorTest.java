package com.whim.mybatisplus.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.model.UserInfo;
import com.whim.core.exception.TenantAccessDeniedException;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Jince
 * @date 2026/09/29
 * @description 验证租户 SQL 拦截器在无上下文和跨线程场景下默认拒绝。
 */
class TenantIsolationInterceptorTest {

    /** 验证拦截器顺序以及受保护和非受保护表的不同处理。 */
    @Test
    void protectsConfiguredTableBeforeOtherInterceptors() {
        TestAuthenticationContext context = new TestAuthenticationContext();
        TenantIsolationProperties properties = new TenantIsolationProperties();
        properties.setProtectedTables(Set.of("sys_post"));
        MybatisPlusInterceptor configured = new MybatisPlusConfiguration()
                .mybatisPlusInterceptor(context, properties);
        assertEquals(4, configured.getInterceptors().size());
        assertInstanceOf(TenantLineInnerInterceptor.class, configured.getInterceptors().get(0));
        assertInstanceOf(PaginationInnerInterceptor.class, configured.getInterceptors().get(1));
        assertInstanceOf(OptimisticLockerInnerInterceptor.class, configured.getInterceptors().get(2));
        assertInstanceOf(BlockAttackInnerInterceptor.class, configured.getInterceptors().get(3));

        TenantLineInnerInterceptor tenant = (TenantLineInnerInterceptor) configured.getInterceptors().get(0);
        assertThrows(TenantAccessDeniedException.class,
                () -> tenant.parserSingle("SELECT id FROM sys_post WHERE deleted = 0", null));
        assertThrows(TenantAccessDeniedException.class,
                () -> tenant.parserSingle("UPDATE sys_post SET status = 1 WHERE id = 1", null));
        assertThrows(TenantAccessDeniedException.class,
                () -> tenant.parserSingle("DELETE FROM sys_post WHERE id = 1", null));
        assertEquals("SELECT id FROM sys_dept WHERE deleted = 0",
                tenant.parserSingle("SELECT id FROM sys_dept WHERE deleted = 0", null));
    }

    /** 验证嵌套调用、租户切换和虚拟线程均按当前线程的实际认证状态执行。 */
    @Test
    void doesNotReusePreviousTenantAcrossNestedAndVirtualCalls() throws InterruptedException {
        TestAuthenticationContext context = new TestAuthenticationContext();
        TenantIsolationProperties properties = new TenantIsolationProperties();
        properties.setProtectedTables(Set.of("sys_post"));
        TenantLineInnerInterceptor tenant = (TenantLineInnerInterceptor) new MybatisPlusConfiguration()
                .mybatisPlusInterceptor(context, properties).getInterceptors().get(0);

        context.bind(12L);
        String first = nestedSelect(tenant);
        assertTrue(first.contains("tenant_id = 12"), first);
        String joined = tenant.parserSingle("SELECT post.id FROM sys_post post "
                + "LEFT JOIN sys_user_post binding ON binding.post_id = post.id WHERE post.deleted = 0", null);
        assertTrue(joined.contains("post.tenant_id = 12"), joined);
        String count = tenant.parserSingle("SELECT COUNT(*) FROM sys_post WHERE deleted = 0", null);
        assertTrue(count.contains("tenant_id = 12"), count);
        context.bind(13L);
        String second = nestedSelect(tenant);
        assertTrue(second.contains("tenant_id = 13"), second);

        AtomicReference<Throwable> virtualFailure = new AtomicReference<>();
        Thread virtualThread = Thread.startVirtualThread(() -> {
            try {
                tenant.parserSingle("SELECT id FROM sys_post", null);
            } catch (Throwable exception) {
                virtualFailure.set(exception);
            }
        });
        virtualThread.join(5000);
        assertTrue(!virtualThread.isAlive(), "虚拟线程必须及时结束");
        assertInstanceOf(TenantAccessDeniedException.class, virtualFailure.get());
        context.clear();
        assertThrows(TenantAccessDeniedException.class,
                () -> tenant.parserSingle("SELECT id FROM sys_post", null));
    }

    /** 验证显式 tenant_id 的插入也不能在缺少认证上下文时放行。 */
    @Test
    void deniesExplicitTenantInsertWithoutContext() {
        TestAuthenticationContext context = new TestAuthenticationContext();
        TenantIsolationProperties properties = new TenantIsolationProperties();
        properties.setProtectedTables(Set.of("sys_post"));
        TenantLineInnerInterceptor tenant = (TenantLineInnerInterceptor) new MybatisPlusConfiguration()
                .mybatisPlusInterceptor(context, properties).getInterceptors().get(0);
        assertThrows(TenantAccessDeniedException.class,
                () -> tenant.parserSingle("INSERT INTO sys_post (id, tenant_id) VALUES (1, 12)", null));
        assertThrows(TenantAccessDeniedException.class,
                () -> tenant.parserSingle("INSERT INTO sys_post (id, post_name) VALUES (1, '岗位')", null));
        context.bind(12L);
        assertThrows(TenantAccessDeniedException.class,
                () -> tenant.parserSingle("INSERT INTO sys_post (id, tenant_id) VALUES (1, 13)", null));
        assertThrows(TenantAccessDeniedException.class,
                () -> tenant.parserSingle("UPDATE sys_post SET tenant_id = 13 WHERE id = 1", null));
        String safeInsert = tenant.parserSingle("INSERT INTO sys_post (id, post_name) VALUES (1, '岗位')", null);
        assertTrue(safeInsert.contains("tenant_id"), safeInsert);
        assertTrue(safeInsert.contains("12"), safeInsert);
        context.clear();
    }

    /** 通过一层普通业务调用验证嵌套执行不会改变租户条件。 */
    private String nestedSelect(TenantLineInnerInterceptor tenant) {
        return tenant.parserSingle("SELECT id FROM sys_post WHERE deleted = 0", null);
    }

    /** 为拦截器提供仅限测试线程持有的认证上下文。 */
    private static class TestAuthenticationContext implements AuthenticationContext {
        private final ThreadLocal<UserInfo> currentUser = new ThreadLocal<>();

        /** 为当前线程设置测试租户。 */
        void bind(Long tenantId) {
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(1L);
            userInfo.setCurrentTenantId(tenantId);
            currentUser.set(userInfo);
        }

        /** 清除当前线程的测试身份。 */
        void clear() {
            currentUser.remove();
        }

        /** 返回当前线程的测试用户。 */
        @Override
        public UserInfo getCurrentUserInfo() {
            return currentUser.get();
        }

        /** 当前线程有用户时视为已登录。 */
        @Override
        public boolean isLogin() {
            return currentUser.get() != null;
        }

        /** 测试场景不使用超级管理员特权。 */
        @Override
        public boolean isSuperAdministrator() {
            return false;
        }
    }
}
