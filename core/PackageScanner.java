package core;

import java.util.ArrayList;
import java.util.List;

/**
 * Scans installed packages using `pm list packages -f` and parses basic info.
 */
public class PackageScanner {
    private final AdbRunner adbRunner;

    public PackageScanner(AdbRunner adbRunner) {
        this.adbRunner = adbRunner;
    }

    public List<PackageInfo> listPackages(String serial) {
        List<PackageInfo> list = new ArrayList<>();
        CommandResult res = adbRunner.run("-s", serial, "shell", "pm", "list", "packages", "-f");
        if (!res.isSuccess()) {
            System.err.println("PackageScanner: Failed to list packages (exit code " + res.exitCode() + ")");
            System.err.println("Output: " + res.stdout());
            System.err.println("Error: " + res.stderr());
            return list;
        }
        String output = res.stdout();
        if (output == null || output.isBlank()) {
            System.err.println("PackageScanner: Empty output from pm list packages");
            return list;
        }
        for (String line : output.split("\n")) {
            line = line.trim();
            // Typical: package:/system/app/Whatever/whatever.apk=com.vendor.app
            if (line.startsWith("package:")) {
                int eq = line.indexOf('=');
                if (eq > 0 && eq + 1 < line.length()) {
                    String left = line.substring(8, eq); // path
                    String pkg = line.substring(eq + 1).trim();
                    boolean system = left.contains("/system/") || left.contains("/product/") || left.contains("/system_ext/");
                    String label = getSimpleLabel(pkg);
                    list.add(PackageInfo.of(pkg).withSystem(system).withLabel(label));
                }
            }
        }
        System.out.println("PackageScanner: Parsed " + list.size() + " packages");
        return list;
    }

    private String getSimpleLabel(String pkg) {
        // Quick heuristic: use last part of package name
        // Example: com.oneplus.weather -> Weather
        String[] parts = pkg.split("\\.");
        if (parts.length > 0) {
            String last = parts[parts.length - 1];
            // Capitalize first letter
            return last.substring(0, 1).toUpperCase() + last.substring(1);
        }
        return pkg;
    }
}
