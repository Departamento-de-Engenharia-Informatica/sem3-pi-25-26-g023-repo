package pt.ipp.isep.dei.domain;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditLog {
    private final String logFilePath;

    public AuditLog(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public void writeLog(Return r, String action, int qty) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String line = String.format("%s | returnId=%s | sku=%s | action=%s | qty=%d%n",
                timestamp, r.getReturnId(), r.getSku(), action, qty);
        try (FileWriter writer = new FileWriter(logFilePath, true)) {
            writer.write(line);
        } catch (IOException e) {
            System.err.println("Error writing audit log: " + e.getMessage());
        }
    }
}

