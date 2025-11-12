package scripts;

import core.PackageInfo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RestoreScriptBuilder {
    public static Path createScripts(String serial, List<PackageInfo> pkgs) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path base = getRestoreDir().resolve(timestamp + "-" + serial);
        Files.createDirectories(base);
        Path sh = base.resolve("restore.sh");
        Path bat = base.resolve("restore.bat");

        StringBuilder sbSh = new StringBuilder();
        sbSh.append("#!/usr/bin/env bash\n");
        sbSh.append("# Restore script for device: ").append(serial).append("\n");
        for (PackageInfo p : pkgs) {
            sbSh.append("adb -s ").append(serial).append(" shell pm enable ").append(p.pkg()).append("\n");
        }

        StringBuilder sbBat = new StringBuilder();
        sbBat.append("@echo off\n");
        sbBat.append("REM Restore script for device: ").append(serial).append("\n");
        for (PackageInfo p : pkgs) {
            sbBat.append("adb -s ").append(serial).append(" shell pm enable ").append(p.pkg()).append("\r\n");
        }

        Files.writeString(sh, sbSh.toString(), StandardCharsets.UTF_8);
        Files.writeString(bat, sbBat.toString(), StandardCharsets.UTF_8);
        return base;
    }

    private static Path getRestoreDir() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            String appdata = System.getenv("APPDATA");
            if (appdata != null && !appdata.isBlank()) {
                return Path.of(appdata, "UniversalADB", "restore");
            }
        }
        return Path.of(System.getProperty("user.home"), ".config", "universal-adb-debloater", "restore");
    }
}
