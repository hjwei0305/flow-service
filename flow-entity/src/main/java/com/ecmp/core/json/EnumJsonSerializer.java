package com.ecmp.core.json;

import com.ecmp.util.EnumUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * @author 马超(Vision.Mac)
 * @version 1.0.1 2017/9/9 22:39
 */
public class EnumJsonSerializer extends JsonSerializer<Enum> {

    @Override
    public void serialize(Enum value, JsonGenerator generator, SerializerProvider serializers)
            throws IOException {
//        generator.writeStartObject();
        //自身的值
        generator.writeString(value.name());
        //新增属性：枚举类型+Remark
        generator.writeFieldName(StringUtils.uncapitalize(value.getClass().getSimpleName()) + "Remark");
        //新增属性值
        generator.writeString(EnumUtils.getEnumItemRemark(value.getClass(), value));
//        generator.writeEndObject();
    }
}
