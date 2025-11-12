package updater;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;

/**
 * Handles auto-update of Google platform-tools (adb/fastboot).
 * Downloads, verifies SHA256, and extracts to local directory.
 */
public class PlatformToolsUpdater {
    private static final String METADATA_URL = "https://developer.android.com/studio/releases/platform-tools";
    private final Path toolsDir;
    private final HttpClient httpClient;

    public PlatformToolsUpdater(Path toolsDir) {
        this.toolsDir = toolsDir;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * Checks if an update is available by comparing local version to remote.
     * @return true if update available
     */
    public boolean checkForUpdate() {
        // Placeholder: would parse metadata.json or scrape release page
        // Compare to `adb version` output
        return false;
    }

    /**
     * Downloads platform-tools for current OS and verifies checksum.
     * @param downloadUrl URL to platform-tools zip
     * @param expectedSha256 Expected SHA256 hash
     * @return Path to downloaded file
     */
    public Path download(String downloadUrl, String expectedSha256) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(downloadUrl))
                .timeout(Duration.ofMinutes(10))
                .GET()
                .build();

        Path tempFile = Files.createTempFile("platform-tools-", ".zip");
        HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(tempFile));
        
        if (response.statusCode() != 200) {
            Files.deleteIfExists(tempFile);
            throw new IOException("Download failed with status: " + response.statusCode());
        }

        // Verify SHA256
        String actualHash = calculateSHA256(tempFile);
        if (!actualHash.equalsIgnoreCase(expectedSha256)) {
            Files.deleteIfExists(tempFile);
            throw new IOException("SHA256 mismatch! Expected: " + expectedSha256 + ", Got: " + actualHash);
        }

        return tempFile;
    }

    /**
     * Extracts platform-tools zip to toolsDir.
     */
    public void extract(Path zipFile) throws IOException {
        Files.createDirectories(toolsDir);
        
        try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(
                Files.newInputStream(zipFile))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path targetPath = toolsDir.resolve(entry.getName());
                
                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.createDirectories(targetPath.getParent());
                    Files.copy(zis, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    
                    // Make executable on Unix-like systems
                    if (!isWindows() && (entry.getName().contains("adb") || entry.getName().contains("fastboot"))) {
                        targetPath.toFile().setExecutable(true);
                    }
                }
                zis.closeEntry();
            }
        }
    }
    
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    /**
     * Gets current installed adb version by running `adb version`.
     */
    public String getCurrentVersion(String adbPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(adbPath, "version");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            String output = new String(p.getInputStream().readAllBytes());
            p.waitFor();
            // Parse version from output
            // Example: "Android Debug Bridge version 1.0.41"
            if (output.contains("version")) {
                String[] parts = output.split("version");
                if (parts.length > 1) {
                    return parts[1].trim().split("\\s+")[0];
                }
            }
            return "unknown";
        } catch (Exception e) {
            return "error";
        }
    }

    private String calculateSHA256(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = Files.readAllBytes(file);
            byte[] hash = digest.digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IOException("Failed to calculate SHA256", e);
        }
    }

    /**
     * Platform-specific download URL helper.
     */
    public static String getDownloadUrlForOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "https://dl.google.com/android/repository/platform-tools-latest-windows.zip";
        } else if (os.contains("mac")) {
            return "https://dl.google.com/android/repository/platform-tools-latest-darwin.zip";
        } else {
            return "https://dl.google.com/android/repository/platform-tools-latest-linux.zip";
        }
    }
}
