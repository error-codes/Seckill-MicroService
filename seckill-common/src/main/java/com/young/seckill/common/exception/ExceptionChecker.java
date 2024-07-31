package com.young.seckill.common.exception;

import com.young.seckill.common.response.RespCode;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.Objects;

public class ExceptionChecker {

    public static void throwAssertIfTrue(Boolean express, RespCode resp) {
        if (express) {
            throw new AssertException(resp);
        }
    }

    public static void throwAssertIfFalse(Boolean express, RespCode resp) {
        if (!express) {
            throw new AssertException(resp);
        }
    }

    public static <T extends Number> void throwAssertIfZeroOrNegative(T number, RespCode resp) {
        if (number == null || number.doubleValue() <= 0) {
            throw new AssertException(resp);
        }
    }

    public static void throwAssertIfZeroOrNegative(BigDecimal number, RespCode resp) {
        if (number == null || number.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AssertException(resp);
        }
    }

    public static void throwAssertIfNullOrEmpty(Object obj, RespCode resp) {
        if (ObjectUtils.isEmpty(obj)) {
            throw new AssertException(resp);
        }
    }

    public static void throwAssertIfBlank(String text, RespCode resp) {
        assert text != null;
        if (text.isBlank()) {
            throw new AssertException(resp);
        }
    }

    public static void throwAssertIfEqual(Object s1, Object s2, RespCode resp) {
        if (Objects.equals(s1, s2)) {
            throw new AssertException(resp);
        }
    }

    public static <T> void throwAssertIfNotEqual(T s1, T s2, RespCode resp) {
        if (!Objects.equals(s1, s2)) {
            throw new AssertException(resp);
        }
    }

    public static <T extends Number & Comparable<T>> void throwAssertIfLessThan(T num1, T num2, RespCode resp) {
        if (num1 == null || num1.compareTo(num2) < 0) {
            throw new AssertException(resp);
        }
    }
}
