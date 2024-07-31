package com.young.seckill.common.exception;

import lombok.Getter;

@Getter
public class JacksonDeserializationException extends RuntimeException {

    private final Class<?> targetClass;

    public JacksonDeserializationException(Class<?> targetClass) {
        super(String.format("Jackson deserialize for class [%s] failed.", targetClass.getName()));
        this.targetClass = targetClass;
    }

    public JacksonDeserializationException(Class<?> targetClass, Throwable throwable) {
        super(String.format("Jackson deserialize for class [%s] failed. cause: [%s]", targetClass.getName(), throwable.getMessage()),
              throwable);
        this.targetClass = targetClass;
    }
}
