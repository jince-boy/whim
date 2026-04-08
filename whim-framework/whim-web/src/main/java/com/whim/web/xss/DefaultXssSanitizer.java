package com.whim.web.xss;

import com.whim.web.annotation.Xss;

import java.util.Objects;

/**
 * Default sanitizer implementation backed by the policy declared on {@link Xss}.
 */
public final class DefaultXssSanitizer implements XssSanitizer {

    @Override
    public String sanitize(String value, Xss xss) {
        if (xss == null) {
            return value;
        }
        return sanitize(value, xss.policy());
    }

    @Override
    public String sanitize(String value, Xss.Policy policy) {
        if (value == null) {
            return null;
        }
        return Objects.requireNonNull(policy, "Parameter [policy] must not be null").sanitize(value);
    }
}
