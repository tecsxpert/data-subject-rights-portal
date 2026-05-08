package com.internship.tool.service;

import com.internship.tool.entity.DsrRequest;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CsvExportService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String[] HEADERS = {
        "ID", "UUID", "Subject Name", "Subject Email",
        "Request Type", "Status", "Priority",
        "Description", "AI Description",
        "Assigned To", "Deadline", "Resolved At",
        "Has File", "Is Overdue", "Created At"
    };

    public byte[] exportDsr(List<DsrRequest> records) throws IOException {
        StringWriter sw = new StringWriter();
        sw.write("\uFEFF"); // UTF-8 BOM for Excel compatibility

        // Header row
        sw.write(String.join(",", HEADERS));
        sw.write("\n");

        // Data rows
        for (DsrRequest r : records) {
            sw.write(csv(r.getId().toString()));
            sw.write(csv(r.getUuid().toString()));
            sw.write(csv(r.getSubjectName()));
            sw.write(csv(r.getSubjectEmail()));
            sw.write(csv(r.getRequestType().name()));
            sw.write(csv(r.getStatus().name()));
            sw.write(csv(r.getPriority().name()));
            sw.write(csv(r.getDescription()));
            sw.write(csv(r.getAiDescription()));
            sw.write(csv(r.getAssignedTo() != null ? r.getAssignedTo().getUsername() : ""));
            sw.write(csv(r.getDeadlineDate() != null ? r.getDeadlineDate().toString() : ""));
            sw.write(csv(r.getResolvedAt()  != null ? r.getResolvedAt().format(FMT)  : ""));
            sw.write(csv(r.getFilePath() != null ? "Yes" : "No"));
            sw.write(csv(r.isOverdue() ? "Yes" : "No"));
            // Last field — no trailing comma
            String created = r.getCreatedAt() != null ? r.getCreatedAt().format(FMT) : "";
            sw.write("\"" + escape(created) + "\"\n");
        }

        return sw.toString().getBytes(StandardCharsets.UTF_8);
    }

    /** Wrap a value in quotes and escape internal quotes. Always adds trailing comma. */
    private String csv(String value) {
        if (value == null) value = "";
        return "\"" + escape(value) + "\",";
    }

    private String escape(String value) {
        return value.replace("\"", "\"\"").replace("\n", " ").replace("\r", "");
    }
}
