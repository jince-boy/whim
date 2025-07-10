package com.whim.mybatis.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jince
 * date: 2025/7/4 12:37
 * description: 数据权限字段
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface DataColumn {
    String[] key() default "deptName";        // SpEL表达式中用的变量名，如 deptId、userId

    String[] value() default "dept_id";      // 对应变量的值，如 #userInfo.deptId
}
