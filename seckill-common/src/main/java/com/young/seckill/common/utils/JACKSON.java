package com.young.seckill.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.young.seckill.common.exception.JacksonDeserializationException;
import com.young.seckill.common.exception.JacksonSerializationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Component
public class JACKSON {

    private static ObjectMapper JACKSON_INSTANCE;

    public JACKSON(ObjectMapper objectMapper) {
        JACKSON_INSTANCE = objectMapper;
    }

    public static String toJson(Object obj) {
        try {
            if (obj == null) {
                return null;
            }
            return JACKSON_INSTANCE.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new JacksonSerializationException(obj.getClass(), e);
        }
    }

    public static <T> T toObj(String json, Class<T> cls) {
        try {
            if (!StringUtils.hasText(json)) {
                return null;
            }
            return JACKSON_INSTANCE.readValue(json, cls);
        } catch (IOException e) {
            throw new JacksonDeserializationException(cls, e);
        }
    }


    public static <T> T toObj(String json, TypeReference<T> cls) {
        try {
            if (!StringUtils.hasText(json)) {
                return null;
            }
            return JACKSON_INSTANCE.readValue(json, cls);
        } catch (IOException e) {
            throw new JacksonDeserializationException(cls.getClass(), e);
        }
    }
}