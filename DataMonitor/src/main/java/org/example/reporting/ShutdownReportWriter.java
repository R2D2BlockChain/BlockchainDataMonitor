package org.example.reporting;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ShutdownReportWriter {

    private static final DateTimeFormatter FILE_STAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private ShutdownReportWriter() {
    }

    public static Path resolveReportsDirectory() {
        String env = System.getenv("REPORTS_DIR");
        if (env != null && !env.trim().isEmpty()) {
            return Paths.get(env.trim()).toAbsolutePath().normalize();
        }
        String prop = System.getProperty("reports.dir");
        if (prop != null && !prop.trim().isEmpty()) {
            return Paths.get(prop.trim()).toAbsolutePath().normalize();
        }
        Path cwd = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        Path localReports = cwd.resolve("reports");
        if (Files.isDirectory(localReports)) {
            return localReports;
        }
        Path cwdName = cwd.getFileName();
        if (cwdName != null && "DataMonitor".equalsIgnoreCase(cwdName.toString())) {
            Path parent = cwd.getParent();
            if (parent != null) {
                Path repoReports = parent.resolve("reports");
                if (Files.isDirectory(repoReports)) {
                    return repoReports;
                }
            }
        }
        return localReports;
    }

    public static Path writeReport(Path reportsDir, SessionMetrics metrics) throws IOException {
        Files.createDirectories(reportsDir);
        String stamp = LocalDateTime.now().format(FILE_STAMP);
        Path file = reportsDir.resolve(stamp + "_report.txt");
        String body = buildReportBody(metrics);
        Files.writeString(file, body, StandardCharsets.UTF_8);
        return file;
    }

    static String buildReportBody(SessionMetrics metrics) {
        String writtenAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return ""
                + "Raport sesji DataMonitor\n"
                + "Data zapisu: " + writtenAt + "\n"
                + "\n"
                + "Łączna liczba przetworzonych bloków: " + metrics.getBlocksProcessed() + "\n"
                + "Łączna liczba przetworzonych transakcji: " + metrics.getTransactionsProcessed() + "\n"
                + "Łączne zużycie gazu (suma gasUsed, bloki podstawowe): "
                + metrics.getGasUsedTotal() + "\n";
    }
}
