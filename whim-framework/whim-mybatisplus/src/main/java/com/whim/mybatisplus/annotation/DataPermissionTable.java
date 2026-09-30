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
    /** 数据库表名，不接受 SQL 表达式。 */
    String name();

    /** 指定 SQL 别名；为空时匹配该表的所有出现位置。 */
    String alias() default "";

    /** 部门归属列，部门表本身可以映射为 id。 */
    String departmentColumn() default "dept_id";

    /** 业务归属人列，用户表本身可以映射为 id。 */
    String userColumn() default "owner_user_id";
}
