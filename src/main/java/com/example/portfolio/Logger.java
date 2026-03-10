package com.example.portfolio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;

public final class Logger {
    private static Logger instance;

    private static String fileName;

    private static final String logDirectory = "com\\example\\portfolio\\logs";

    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    private Logger() {
        try {
            File dir = new File(logDirectory);
            if (!dir.exists()) {
                boolean ok = dir.mkdirs();
                if (!ok) {
                    System.err.println("Logger: Could not create log directory: " + dir.getAbsolutePath());
                }
            }

            fileName = LogFileNamer.initFileName("log");
            if (fileName == null || fileName.isBlank()) {
                fileName = "log_fallback.log";
            }

            File file = new File(logDirectory, fileName);
            System.out.println("Creating log file: " + file.getAbsolutePath());
            file.createNewFile();

        } catch (Exception e) {
            System.err.println("Logger: error during initialization: " + e.getMessage());
            e.printStackTrace();
            fileName = "log_fallback.log";
        }
    }

    public static void log(LogLevel level, String message) {
        String caller;
        try {
            caller = getCaller();
        } catch (Exception e) {
            caller = "unknown";
        }

        if (level == null) level = LogLevel.INFO;
        if (message == null) message = "";

        try {
            File dir = new File(logDirectory);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(dir, fileName);

            try (Writer output = new BufferedWriter(new FileWriter(file, true))) {
                String logEntry = String.format(
                        "%s [%s] %s: %s%n",
                        LocalDateTime.now(),
                        level,
                        caller,
                        message
                );
                output.append(logEntry);
            }
        } catch (Exception e) {
            System.err.println("Logger: failed to write log entry: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String getCaller() {
        return StackWalker.getInstance()
                .walk(s -> s
                        .skip(2)
                        .findFirst()
                        .map(f -> f.getClassName() + "." + f.getMethodName())
                        .orElse("unknown"));
    }
}