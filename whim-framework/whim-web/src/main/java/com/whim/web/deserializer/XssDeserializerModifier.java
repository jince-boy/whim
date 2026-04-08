package com.whim.web.deserializer;

import com.whim.web.annotation.Xss;
import com.whim.web.xss.XssSanitizer;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.BeanDeserializerBuilder;
import tools.jackson.databind.deser.SettableBeanProperty;
import tools.jackson.databind.deser.ValueDeserializerModifier;
import tools.jackson.databind.deser.std.StdScalarDeserializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Injects XSS sanitizing logic into Jackson deserialization for annotated fields.
 */
public final class XssDeserializerModifier extends ValueDeserializerModifier {

    private final XssSanitizer xssSanitizer;

    public XssDeserializerModifier(XssSanitizer xssSanitizer) {
        this.xssSanitizer = Objects.requireNonNull(xssSanitizer, "Parameter [xssSanitizer] must not be null");
    }

    @Override
    public BeanDeserializerBuilder updateBuilder(
            DeserializationConfig config,
            BeanDescription beanDescription,
            BeanDeserializerBuilder builder) {
        List<SettableBeanProperty> replacements = new ArrayList<>();
        var properties = builder.getProperties();
        while (properties.hasNext()) {
            var property = properties.next();
            var annotation = property.getAnnotation(Xss.class);
            if (annotation == null || !String.class.equals(property.getType().getRawClass())) {
                continue;
            }
            replacements.add(property.withValueDeserializer(
                    new XssValueDeserializer(annotation, property.getValueDeserializer(), xssSanitizer)
            ));
        }
        replacements.forEach(property -> builder.addOrReplaceProperty(property, true));
        return builder;
    }

    private static final class XssValueDeserializer extends StdScalarDeserializer<String> {

        private final Xss xss;
        private final ValueDeserializer<?> delegate;
        private final XssSanitizer xssSanitizer;

        private XssValueDeserializer(Xss xss, ValueDeserializer<?> delegate, XssSanitizer xssSanitizer) {
            super(String.class);
            this.xss = Objects.requireNonNull(xss, "Parameter [xss] must not be null");
            this.delegate = delegate;
            this.xssSanitizer = Objects.requireNonNull(xssSanitizer, "Parameter [xssSanitizer] must not be null");
        }

        @Override
        public String deserialize(JsonParser jsonParser, DeserializationContext context) throws JacksonException {
            var rawValue = deserializeRawValue(jsonParser, context);
            return xssSanitizer.sanitize(rawValue, xss);
        }

        private String deserializeRawValue(JsonParser jsonParser, DeserializationContext context) throws JacksonException {
            if (delegate != null && !(delegate instanceof XssValueDeserializer)) {
                return (String) delegate.deserialize(jsonParser, context);
            }
            return jsonParser.getValueAsString();
        }
    }
}
