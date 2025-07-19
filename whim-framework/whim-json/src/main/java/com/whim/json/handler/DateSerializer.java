package com.whim.json.handler;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.whim.core.utils.DateUtils;

import java.io.IOException;
import java.util.Date;

/**
 * @author jince
 * @date 2025/6/17 21:29
 * @description 时间序列化处理
 */
public class DateSerializer extends JsonDeserializer<Date> {

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        return DateUtils.parse(jsonParser.getText());
    }
}
