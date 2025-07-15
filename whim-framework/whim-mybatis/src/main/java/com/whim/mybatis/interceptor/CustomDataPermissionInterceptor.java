package com.whim.mybatis.interceptor;

import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.BaseMultiTableInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.whim.mybatis.context.DataPermissionContext;
import com.whim.mybatis.handler.CustomDataPermissionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Jince
 * date: 2025/7/15 11:54
 * description: 自定义数据权限拦截器
 */
@Slf4j
@RequiredArgsConstructor
public class CustomDataPermissionInterceptor extends BaseMultiTableInnerInterceptor implements InnerInterceptor {
    private final CustomDataPermissionHandler customDataPermissionHandler;

    @Override
    public Expression buildTableExpression(Table table, Expression where, String whereSegment) {
        if (this.customDataPermissionHandler == null) {
            return null;
        } else {
            MultiDataPermissionHandler handler = (MultiDataPermissionHandler) this.customDataPermissionHandler;
            return handler.getSqlSegment(table, where, whereSegment);
        }
    }

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        if (InterceptorIgnoreHelper.willIgnoreDataPermission(ms.getId())) {
            return;
        }
        if (DataPermissionContext.currentPermissionHolder() == null) {
            return;
        }
        PluginUtils.MPBoundSql mpBs = PluginUtils.mpBoundSql(boundSql);
        mpBs.sql(this.parserSingle(mpBs.sql(), ms.getId()));
    }

    @Override
    public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
        PluginUtils.MPStatementHandler mpSh = PluginUtils.mpStatementHandler(sh);
        MappedStatement ms = mpSh.mappedStatement();
        SqlCommandType sct = ms.getSqlCommandType();
        if (sct == SqlCommandType.UPDATE || sct == SqlCommandType.DELETE) {
            if (InterceptorIgnoreHelper.willIgnoreDataPermission(ms.getId())) {
                return;
            }
            if (DataPermissionContext.currentPermissionHolder() == null) {
                return;
            }
            PluginUtils.MPBoundSql mpBs = mpSh.mPBoundSql();
            mpBs.sql(this.parserMulti(mpBs.sql(), ms.getId()));
        }
    }

    @Override
    protected void processSelect(Select select, int index, String sql, Object obj) {
        if (select instanceof PlainSelect) {
            this.setWhere((PlainSelect) select, (String) obj);
        } else if (select instanceof SetOperationList setOperationList) {
            List<Select> selectBodyList = setOperationList.getSelects();
            selectBodyList.forEach((s) -> this.setWhere((PlainSelect) s, (String) obj));
        }
    }

    @Override
    protected void processUpdate(Update update, int index, String sql, Object obj) {
        Expression sqlSegment = this.customDataPermissionHandler.getSqlSegment(update.getWhere(), (String) obj, false);
        if (null != sqlSegment) {
            update.setWhere(sqlSegment);
        }
    }

    @Override
    protected void processDelete(Delete delete, int index, String sql, Object obj) {
        Expression sqlSegment = this.customDataPermissionHandler.getSqlSegment(delete.getWhere(), (String) obj, false);
        if (null != sqlSegment) {
            delete.setWhere(sqlSegment);
        }
    }

    protected void setWhere(PlainSelect plainSelect, String whereSegment) {
        if (this.customDataPermissionHandler != null) {
            Expression sqlSegment = this.customDataPermissionHandler.getSqlSegment(plainSelect.getWhere(), whereSegment, true);
            if (null != sqlSegment) {
                plainSelect.setWhere(sqlSegment);
            }
        }
    }
}
