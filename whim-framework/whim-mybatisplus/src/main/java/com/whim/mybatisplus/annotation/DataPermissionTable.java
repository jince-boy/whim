package com.whim.mybatisplus.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 数据权限使用的业务归属映射，空列表示资源不支持该归属维度。
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermissionTable {
    /**
     * 指定数据库表名，不接受 SQL 表达式。
     *
     * @return 数据库表名
     */
    String name();

    /**
     * 指定 SQL 别名；为空时匹配该表的所有出现位置。
     *
     * @return SQL 别名
     */
    String alias() default "";

    /**
     * 指定部门归属列，默认使用 dept_id，部门表本身可以映射为 id。
     *
     * @return 部门归属列，空字符串表示不支持部门归属
     */
    String departmentColumn() default "dept_id";

    /**
     * 指定本人数据范围的用户归属列，默认使用创建人 create_by，用户表本身可以映射为 id。
     *
     * @return 用户归属列，空字符串表示不支持用户归属
     */
    String userColumn() default "create_by";
}
