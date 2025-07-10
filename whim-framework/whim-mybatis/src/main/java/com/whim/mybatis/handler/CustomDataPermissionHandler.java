package com.whim.mybatis.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.SpringUtils;
import com.whim.mybatis.annotation.DataColumn;
import com.whim.mybatis.annotation.DataPermission;
import com.whim.mybatis.context.DataPermissionContext;
import com.whim.mybatis.context.DataPermissionHolder;
import com.whim.mybatis.enums.DataScopeType;
import com.whim.mybatis.factory.NullSafeEvaluationContextFactory;
import com.whim.satoken.core.model.RoleInfo;
import com.whim.satoken.core.model.UserInfo;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * @author jince
 * date: 2025/7/3 15:42
 * description: 自定义数据权限处理器
 */
@Slf4j
public class CustomDataPermissionHandler implements MultiDataPermissionHandler {
    private final SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
    BeanFactoryResolver beanFactoryResolver = new BeanFactoryResolver(SpringUtils.getBeanFactory());
    private final ParserContext parserContext = new TemplateParserContext();

    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        DataPermissionHolder permissionHolder = DataPermissionContext.current();
        if (permissionHolder == null || !permissionHolder.hasPermission()) {
            return null;
        }
        DataPermission dataPermission = permissionHolder.getPermission();
        UserInfo userInfo = permissionHolder.getValue("userInfo", UserInfo.class);
        if (userInfo == null) {
            log.warn("数据权限:用户信息为空");
            return null;
        }
        StandardEvaluationContext context = NullSafeEvaluationContextFactory.create("-1");
        context.setVariable("userInfo", userInfo);
        context.setBeanResolver(beanFactoryResolver);

        DataColumn[] dataColumns = dataPermission.value();
        for (DataColumn column : dataColumns) {
            if (column.key().length != column.value().length) {
                throw new ServiceException("数据权限的key和value长度不一致");
            }
            for (int i = 0; i < column.key().length; i++) {
                context.setVariable(column.key()[i], column.value()[i]);
            }
        }

        for (RoleInfo role : userInfo.getRoleInfoList()) {
            DataScopeType scopeType = DataScopeType.findByCode(role.getDataScope());
            if (scopeType == null) continue;
            if (scopeType == DataScopeType.ALL) {
                return null;
            }
            context.setVariable("roleId", role.getId());

            String sql = DataPermissionContext.ignore(() ->
                    spelExpressionParser.parseExpression(
                            scopeType.getSqlTemplate(), parserContext).getValue(context, String.class)
            );
            log.info(sql);
        }
        return null;
    }

}
