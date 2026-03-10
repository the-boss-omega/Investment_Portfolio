package com.example.portfolio;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class LogFileNamer {
    public static String fileName;

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");

    private LogFileNamer() {}

    public static String initFileName(String prefix) {
        try {
            String safePrefix = (prefix == null || prefix.isBlank()) ? "log" : prefix.trim();
            fileName = safePrefix + "_" + LocalDateTime.now().format(TS) + ".log";
            return fileName;
        } catch (Exception e) {
            fileName = "log_fallback.log";
            Logger.log(LogLevel.ERROR, "LogFileNamer.initFileName failed, using fallback. err=" + e.getMessage());
            return fileName;
        }
    }
}