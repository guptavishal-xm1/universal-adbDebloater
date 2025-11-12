package persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple config store using JSON file (no external libs).
 * Stores key-value pairs for app settings.
 */
public class ConfigStore {
    private final Path configFile;
    private Map<String, String> data;

    public ConfigStore(Path configFile) {
        this.configFile = configFile;
        this.data = new HashMap<>();
        load();
    }

    public static ConfigStore getDefault() {
        Path configPath = getDefaultConfigPath();
        return new ConfigStore(configPath);
    }

    private static Path getDefaultConfigPath() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            String appdata = System.getenv("APPDATA");
            if (appdata != null && !appdata.isBlank()) {
                return Path.of(appdata, "UniversalADB", "config.json");
            }
        }
        return Path.of(System.getProperty("user.home"), ".config", "universal-adb-debloater", "config.json");
    }

    public void load() {
        if (!Files.exists(configFile)) {
            data = getDefaults();
            return;
        }
        try {
            String json = Files.readString(configFile, StandardCharsets.UTF_8);
            data = parseSimpleJson(json);
        } catch (IOException e) {
            data = getDefaults();
        }
    }

    public void save() throws IOException {
        Files.createDirectories(configFile.getParent());
        String json = toSimpleJson(data);
        Files.writeString(configFile, json, StandardCharsets.UTF_8);
    }

    public String get(String key) {
        return data.getOrDefault(key, "");
    }

    public void set(String key, String value) {
        data.put(key, value);
    }

    public boolean getBoolean(String key) {
        return "true".equalsIgnoreCase(get(key));
    }

    public void setBoolean(String key, boolean value) {
        set(key, String.valueOf(value));
    }

    private Map<String, String> getDefaults() {
        Map<String, String> defaults = new HashMap<>();
        defaults.put("platformToolsPath", "");
        defaults.put("adbPath", "adb");
        defaults.put("autoUpdatePlatformTools", "true");
        defaults.put("oemPackSource", "local");
        defaults.put("telemetryOptIn", "false");
        return defaults;
    }

    // Minimal JSON parsing for string key-value pairs
    private static final Pattern PAIR_PATTERN = Pattern.compile("\\\"([^\\\"]+)\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"");

    private Map<String, String> parseSimpleJson(String json) {
        Map<String, String> result = new HashMap<>();
        Matcher m = PAIR_PATTERN.matcher(json);
        while (m.find()) {
            result.put(m.group(1), m.group(2));
        }
        return result.isEmpty() ? getDefaults() : result;
    }

    private String toSimpleJson(Map<String, String> map) {
        StringBuilder sb = new StringBuilder("{\n");
        int i = 0;
        for (Map.Entry<String, String> e : map.entrySet()) {
            sb.append("  \"").append(e.getKey()).append("\": \"").append(e.getValue()).append("\"");
            if (i < map.size() - 1) sb.append(",");
            sb.append("\n");
            i++;
        }
        sb.append("}\n");
        return sb.toString();
    }
}
