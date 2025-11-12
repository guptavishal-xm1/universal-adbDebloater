package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal ADB command runner. Assumes 'adb' is on PATH for now.
 * Later we can inject a configurable adbPath.
 */
public class AdbRunner {
    private final String adbExecutable;

    public AdbRunner() {
        this("adb");
    }

    public AdbRunner(String adbExecutable) {
        this.adbExecutable = adbExecutable;
    }

    public CommandResult run(String... args) {
        List<String> cmd = new ArrayList<>();
        cmd.add(adbExecutable);
        for (String a : args) cmd.add(a);
        
        // Log command being executed
        System.out.println("AdbRunner: Executing: " + String.join(" ", cmd));
        
        ProcessBuilder pb = new ProcessBuilder(cmd);
        // Merge stderr into stdout to avoid potential deadlocks when outputs are large
        pb.redirectErrorStream(true);
        try {
            Process p = pb.start();
            String stdout = readStream(p.getInputStream());
            int code = p.waitFor();
            
            if (code != 0) {
                System.err.println("AdbRunner: Command failed with exit code " + code);
                System.err.println("AdbRunner: Output: " + stdout.substring(0, Math.min(500, stdout.length())));
            }
            
            return new CommandResult(code, stdout, "");
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return new CommandResult(-1, "", "Interrupted");
        } catch (IOException ioe) {
            System.err.println("AdbRunner: IOException - " + ioe.getMessage());
            ioe.printStackTrace();
            return new CommandResult(-1, "", ioe.getMessage());
        }
    }

    private String readStream(java.io.InputStream in) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        }
    }
}
