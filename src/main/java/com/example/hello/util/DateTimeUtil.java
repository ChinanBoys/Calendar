package com.example.hello.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * ISO8601 时间字符串解析工具。
 */
public final class DateTimeUtil {

    private static final DateTimeFormatter LOCAL = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private DateTimeUtil() {
    }

    public static LocalDateTime parse(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String trimmed = text.trim();
        try {
            return OffsetDateTime.parse(trimmed).toLocalDateTime();
        } catch (DateTimeParseException ignore) {
            try {
                return LocalDateTime.parse(trimmed, LOCAL);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("无法解析时间: " + text);
            }
        }
    }
}
