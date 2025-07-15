package com.whim.mybatis.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jince
 * date: 2025/7/4 12:37
 * description: 数据权限字段规则
 * key      表别名+字段名（如 "u.dept_id"），或SpEL上下文变量名（如 "deptId"）
 * value   对应数据库字段或SpEL表达式（如 "#user.deptId"）
 * 注意：当包含特殊字符（如'.'）时视为直接字段引用，否则解析为SpEL
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface DataColumn {
    String[] key() default "deptName";        // SpEL表达式中用的变量名，如 deptId、userId
    String[] value() default "dept_id";      // 对应变量的值，如 #userInfo.deptId
}
