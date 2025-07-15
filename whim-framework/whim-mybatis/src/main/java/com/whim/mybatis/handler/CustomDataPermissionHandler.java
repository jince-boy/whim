package com.whim.mybatis.handler;

import com.whim.core.exception.ServiceException;
import com.whim.core.utils.SpringUtils;
import com.whim.mybatis.annotation.DataColumn;
import com.whim.mybatis.annotation.DataPermission;
import com.whim.mybatis.context.DataPermissionContext;
import com.whim.mybatis.core.enums.DataScopeType;
import com.whim.satoken.core.model.RoleInfo;
import com.whim.satoken.core.model.UserInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author jince
 * date: 2025/7/3 15:42
 * description: 自定义数据权限处理器
 */
@Slf4j
public class CustomDataPermissionHandler {
    private final Expression DENY_ALL = new EqualsTo(new LongValue(1), new LongValue(0));
    private final BeanFactoryResolver beanFactoryResolver = new BeanFactoryResolver(SpringUtils.getBeanFactory());
    // ✅ SpEL Expression 缓存
    private static final Map<DataScopeType, org.springframework.expression.Expression> EXPRESSION_CACHE;

    static {
        SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
        EXPRESSION_CACHE = Arrays.stream(DataScopeType.values())
                .filter(type -> StringUtils.isNotBlank(type.getSqlTemplate()))
                .collect(Collectors.toMap(
                        Function.identity(),
                        type -> {
                            try {
                                return spelExpressionParser.parseExpression(type.getSqlTemplate(), new TemplateParserContext());
                            } catch (Exception e) {
                                log.error("解析SpEL失败, scopeType={}, template={}", type.name(), type.getSqlTemplate(), e);
                                throw e;
                            }
                        }
                ));
    }

    public Expression getSqlSegment(Expression where, String mappedStatementId, Boolean isSelect) {
        DataPermissionContext.DataPermissionHolder permissionHolder = DataPermissionContext.currentPermissionHolder();
        DataPermission dataPermission = permissionHolder.getDataPermission();
        UserInfo userInfo = permissionHolder.getAttribute("userInfo", UserInfo.class);
        if (userInfo == null) {
            log.warn("数据权限:用户信息为空");
            return null;
        }
        StandardEvaluationContext context = new NullSafeStandardEvaluationContext("-1");
        context.setVariable("userInfo", userInfo);
        context.setBeanResolver(beanFactoryResolver);

        DataColumn[] dataColumns = dataPermission.value();
        Arrays.stream(dataColumns).forEach(column -> {
            if (column.key().length != column.value().length) {
                throw new ServiceException("数据权限的key和value长度不一致");
            }
            IntStream.range(0, column.key().length)
                    .forEach(i -> context.setVariable(column.key()[i], column.value()[i]));
        });

        Set<String> sqlConditions = new HashSet<>();
        for (RoleInfo role : userInfo.getRoleInfoList()) {
            DataScopeType scopeType = DataScopeType.findByCode(role.getDataScope());
            if (scopeType == null) continue;
            if (scopeType == DataScopeType.ALL) {
                return null;
            }
            context.setVariable("roleId", role.getId());

            String sqlSegment = DataPermissionContext.runWithIgnoreDataPermission(() -> {
                        org.springframework.expression.Expression expression = EXPRESSION_CACHE.get(scopeType);
                        if (expression == null) {
                            log.error("找不到缓存的 SpEL 表达式 scopeType={}", scopeType.name());
                            throw new RuntimeException("SpEL Expression 缓存丢失");
                        }
                        return expression.getValue(context, String.class);
                    }
            );
            if (StringUtils.isNotBlank(sqlSegment)) {
                sqlConditions.add("(" + sqlSegment + ")");
            }
        }
        if (sqlConditions.isEmpty()) {
            return where == null ? DENY_ALL : new AndExpression(where, DENY_ALL);
        }

        String joinStr = isSelect ? " OR " : " AND ";
        if (StringUtils.isNotBlank(dataPermission.joinStr())) {
            joinStr = " " + dataPermission.joinStr() + " ";
        }
        String combinedSql = String.join(joinStr, sqlConditions);
        Expression permissionExpression;
        try {
            permissionExpression = CCJSqlParserUtil.parseCondExpression(combinedSql);
        } catch (JSQLParserException e) {
            throw new RuntimeException(e);
        }

        return where == null ? permissionExpression : new AndExpression(where, permissionExpression);
    }

    /**
     * 自定义 EvaluationContext，变量不存在时返回默认值
     */
    @RequiredArgsConstructor
    private static final class NullSafeStandardEvaluationContext extends StandardEvaluationContext {
        private final Object defaultValue;

        @Override
        public Object lookupVariable(@NonNull String name) {
            Object obj = super.lookupVariable(name);
            return obj != null ? obj : defaultValue;
        }
    }
}
