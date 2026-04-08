package com.whim.web.xss;

import com.whim.web.annotation.Xss;

/**
 * Shared XSS sanitizing entry point for different web input channels.
 */
public interface XssSanitizer {

    String sanitize(String value, Xss xss);

    String sanitize(String value, Xss.Policy policy);
}
