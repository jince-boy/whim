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
 * Marks request input that should be sanitized before entering business logic.
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Xss {

    /**
     * Selects the sanitize policy for the annotated input.
     */
    Policy policy() default Policy.RICH_TEXT;

    /**
     * Built-in sanitize policies.
     */
    enum Policy {
        /**
         * Removes all HTML tags and keeps plain text only.
         */
        PLAIN_TEXT(new HtmlPolicyBuilder().toFactory()),

        /**
         * Allows common rich text tags while stripping dangerous content.
         */
        RICH_TEXT(
                Sanitizers.FORMATTING
                        .and(Sanitizers.BLOCKS)
                        .and(Sanitizers.LINKS)
                        .and(Sanitizers.IMAGES)
                        .and(Sanitizers.TABLES)
        );

        private final PolicyFactory policyFactory;

        Policy(PolicyFactory policyFactory) {
            this.policyFactory = policyFactory;
        }

        public String sanitize(String value) {
            if (value == null) {
                return null;
            }
            return policyFactory.sanitize(value);
        }
    }
}
