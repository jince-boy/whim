package com.whim.mybatis.factory;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.AccessException;

import java.util.List;

/**
 * @author jince
 * date: 2025/7/10 22:57
 * description: 工具类：用于创建支持 null 安全的 Spring EvaluationContext（用于 SpEL 表达式）
 * 当表达式中访问到 null 或未定义的变量/属性时，会返回指定的默认值，避免抛出异常。
 * 典型用途：SpEL 表达式中的容错处理，如：权限控制、动态配置解析、模板渲染等
 */
public class NullSafeEvaluationContextFactory {
    /**
     * 创建一个带 null 安全保护的 EvaluationContext 实例
     *
     * @param defaultValue 当变量或属性为 null 时返回的默认值
     * @return 增强的 StandardEvaluationContext 实例
     */
    public static StandardEvaluationContext create(Object defaultValue) {
        NullSafeStandardEvaluationContext context = new NullSafeStandardEvaluationContext(defaultValue);
        // 包装默认的 PropertyAccessor，为其添加 null 保护
        List<PropertyAccessor> accessors = context.getPropertyAccessors();
        if (!accessors.isEmpty()) {
            PropertyAccessor first = accessors.getFirst();
            context.addPropertyAccessor(new NullSafePropertyAccessor(first, defaultValue));
        }
        return context;
    }

    /**
     * 自定义的 EvaluationContext，增强变量查找逻辑：
     * 当变量为 null 或未定义时，返回默认值
     */
    @AllArgsConstructor
    private static class NullSafeStandardEvaluationContext extends StandardEvaluationContext {

        private final Object defaultValue;

        @Override
        public Object lookupVariable(@NonNull String name) {
            Object obj = super.lookupVariable(name);
            return obj != null ? obj : defaultValue;
        }
    }

    /**
     * 自定义 PropertyAccessor 实现，增强属性访问逻辑：
     * 当属性值为 null 时，返回默认值
     */
    @RequiredArgsConstructor
    private static class NullSafePropertyAccessor implements PropertyAccessor {

        private final PropertyAccessor delegate;
        private final Object defaultValue;

        @Override
        public Class<?>[] getSpecificTargetClasses() {
            return delegate.getSpecificTargetClasses();
        }

        @Override
        public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
            return delegate.canRead(context, target, name);
        }

        @Override
        public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
            TypedValue value = delegate.read(context, target, name);
            if (value.getValue() == null) {
                return new TypedValue(defaultValue);
            }
            return value;
        }

        @Override
        public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
            return delegate.canWrite(context, target, name);
        }

        @Override
        public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
            delegate.write(context, target, name, newValue);
        }
    }
}
