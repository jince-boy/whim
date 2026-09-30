package com.whim.mybatisplus.permission;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.whim.core.exception.DataAccessDeniedException;
import com.whim.core.permission.DataPermissionScope;
import com.whim.mybatisplus.annotation.DataPermissionTable;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 按真实 SQL 别名追加部门或归属人条件，嵌套权限范围取交集。
 */
public class DataPermissionSqlHandler implements MultiDataPermissionHandler {
    /** 返回额外条件，保留原查询条件；空授权生成恒假条件，解析错误直接拒绝。 */
    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        if (!DataPermissionContext.active()) {
            return null;
        }
        String alias = table.getAlias() == null ? table.getName() : table.getAlias().getName();
        List<String> constraints = new ArrayList<>();
        for (DataPermissionContext.Frame frame : DataPermissionContext.FRAMES.get()) {
            for (DataPermissionTable mapping : frame.getTables()) {
                if (!mapping.name().equalsIgnoreCase(unquote(table.getName()))
                        || (!mapping.alias().isEmpty() && !mapping.alias().equalsIgnoreCase(unquote(alias)))) {
                    continue;
                }
                DataPermissionScope scope = frame.getScope();
                if (scope.isAll()) {
                    continue;
                }
                List<String> branches = new ArrayList<>();
                if (!mapping.departmentColumn().isEmpty() && !scope.getDepartmentIds().isEmpty()) {
                    String ids = scope.getDepartmentIds().stream().map(String::valueOf)
                            .collect(Collectors.joining(","));
                    branches.add(alias + "." + mapping.departmentColumn() + " IN (" + ids + ")");
                }
                if (scope.isSelf() && !mapping.userColumn().isEmpty()) {
                    branches.add(alias + "." + mapping.userColumn() + " = " + scope.getUserId());
                }
                constraints.add(branches.isEmpty() ? "(1 = 0)" : "(" + String.join(" OR ", branches) + ")");
            }
        }
        if (constraints.isEmpty()) {
            return null;
        }
        try {
            return CCJSqlParserUtil.parseCondExpression(String.join(" AND ", constraints));
        } catch (Exception exception) {
            throw new DataAccessDeniedException("数据权限条件解析失败，已拒绝执行");
        }
    }

    /** 兼容 SQL 使用反引号或双引号包裹的标识符。 */
    private String unquote(String identifier) {
        if (identifier.length() > 1 && ((identifier.startsWith("`") && identifier.endsWith("`"))
                || (identifier.startsWith("\"") && identifier.endsWith("\"")))) {
            return identifier.substring(1, identifier.length() - 1);
        }
        return identifier;
    }
}
