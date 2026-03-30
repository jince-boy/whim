package com.whim.web.annotation;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Jince
 * @date 2026/03/27
 * @description XSS 防护注解，仅对明确标注的字符串字段启用安全清洗。
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Xss {

    /**
     * 指定当前字段使用的清洗策略。
     *
     * @return XSS 清洗策略
     */
    Policy policy() default Policy.RICH_TEXT;

    /**
     * @author Jince
     * @date 2026/03/27
     * @description XSS 清洗策略。
     */
    enum Policy {
        /**
         * 纯文本策略，不保留任何 HTML 标签。
         */
        PLAIN_TEXT(new HtmlPolicyBuilder().toFactory()),

        /**
         * 富文本策略，保留常见排版、区块、链接、图片与表格标签。
         */
        RICH_TEXT(
                Sanitizers.FORMATTING
                        .and(Sanitizers.BLOCKS)
                        .and(Sanitizers.LINKS)
                        .and(Sanitizers.IMAGES)
                        .and(Sanitizers.TABLES)
        );

        private final PolicyFactory policyFactory;

        /**
         * 创建 XSS 清洗策略。
         *
         * @param policyFactory OWASP 策略工厂
         */
        Policy(PolicyFactory policyFactory) {
            this.policyFactory = policyFactory;
        }

        /**
         * 清洗输入内容中的危险 HTML。
         *
         * @param value 原始输入内容
         * @return 清洗后的安全内容
         */
        public String sanitize(String value) {
            if (value == null) {
                return null;
            }
            return policyFactory.sanitize(value);
        }
    }
}
