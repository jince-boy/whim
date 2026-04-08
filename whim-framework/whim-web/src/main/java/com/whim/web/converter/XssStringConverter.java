package com.whim.web.converter;

import com.whim.web.annotation.Xss;
import com.whim.web.xss.XssSanitizer;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.converter.GenericConverter;

import java.util.Objects;
import java.util.Set;

/**
 * Sanitizes annotated string inputs during Spring MVC type conversion.
 */
public final class XssStringConverter implements ConditionalGenericConverter {

    private static final Set<GenericConverter.ConvertiblePair> CONVERTIBLE_TYPES =
            Set.of(new GenericConverter.ConvertiblePair(String.class, String.class));

    private final XssSanitizer xssSanitizer;

    public XssStringConverter(XssSanitizer xssSanitizer) {
        this.xssSanitizer = Objects.requireNonNull(xssSanitizer, "Parameter [xssSanitizer] must not be null");
    }

    @Override
    public Set<GenericConverter.ConvertiblePair> getConvertibleTypes() {
        return CONVERTIBLE_TYPES;
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return sourceType != null
                && targetType != null
                && sourceType.getType() == String.class
                && targetType.getType() == String.class
                && targetType.getAnnotation(Xss.class) != null;
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (!(source instanceof String value) || targetType == null) {
            return source;
        }
        return xssSanitizer.sanitize(value, targetType.getAnnotation(Xss.class));
    }
}
