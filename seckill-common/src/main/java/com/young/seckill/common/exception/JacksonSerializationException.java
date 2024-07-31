package com.young.seckill.common.exception;

import lombok.Getter;

@Getter
public class JacksonSerializationException extends RuntimeException {

    private final Class<?> targetClass;

    public JacksonSerializationException(Class<?> targetClass) {
        super(String.format("Jackson serialize for class [%s] failed.", targetClass.getName()));
        this.targetClass = targetClass;
    }

    public JacksonSerializationException(Class<?> targetClass, Throwable throwable) {
        super(String.format("Jackson serialize for class [%s] failed.", targetClass.getName()), throwable);
        this.targetClass = targetClass;
    }
}
