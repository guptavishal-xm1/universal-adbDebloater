package core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple OEM pack loader using minimal regex-based parsing.
 */
public class SimpleOemPackLoader {
    private final Path packsDir;
    // In-memory cache: manufacturer (normalized) -> map(pkg -> [action,risk,reason])
    private final Map<String, Map<String, String[]>> cache = new HashMap<>();

    public SimpleOemPackLoader(Path packsDir) {
        this.packsDir = packsDir;
    }

    private static final Pattern ITEM_PATTERN = Pattern.compile("\\{[^}]*\\}", Pattern.DOTALL);
    private static final Pattern PKG_PATTERN = Pattern.compile("\\\"pkg\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern ACTION_PATTERN = Pattern.compile("\\\"recommendedAction\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"");
    private static final Pattern RISK_PATTERN = Pattern.compile("\\\"risk\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"");
    private static final Pattern REASON_PATTERN = Pattern.compile("\\\"reason\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"");

    /** Returns map of pkg -> [action, risk, reason] */
    public Map<String, String[]> loadForManufacturer(String manufacturer, String defaultSuffix) {
        Map<String, String[]> map = new HashMap<>();
        if (manufacturer == null || manufacturer.isBlank()) return map;
        
        // Normalize and sanitize manufacturer name for filesystem
        String norm = manufacturer.toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9-]", ""); // Remove any non-alphanumeric except dash
        
        if (norm.isBlank()) {
            System.err.println("SimpleOemPackLoader: Manufacturer name sanitized to empty string, skipping: " + manufacturer);
            return map;
        }
        
        if (cache.containsKey(norm)) {
            System.out.println("SimpleOemPackLoader: Cache hit for '" + norm + "'");
            return cache.get(norm);
        }
        
        String fileName = norm + (defaultSuffix == null ? "" : defaultSuffix);
        Path file = packsDir.resolve(fileName);
        System.out.println("SimpleOemPackLoader: Looking for OEM pack: " + file);
        
        if (!Files.exists(file)) {
            System.out.println("SimpleOemPackLoader: No OEM pack found for '" + norm + "' at " + file);
            return map;
        }
        
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            Map<String, String[]> loaded = extractPackages(json);
            cache.put(norm, loaded);
            System.out.println("SimpleOemPackLoader: Loaded " + loaded.size() + " entries from " + fileName);
            return loaded;
        } catch (IOException e) {
            System.err.println("SimpleOemPackLoader: Failed to load " + fileName + ": " + e.getMessage());
            return map;
        }
    }

    public static Map<String, String[]> extractPackages(String json) {
        Map<String, String[]> map = new HashMap<>();
        int arrIdx = json.indexOf("\"packages\"");
        if (arrIdx < 0) return map;
        int bracket = json.indexOf('[', arrIdx);
        if (bracket < 0) return map;
        int depth = 0; int end = -1;
        for (int i = bracket; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') { depth--; if (depth == 0) { end = i; break; } }
        }
        if (end < 0) return map;
        String arr = json.substring(bracket + 1, end);
        Matcher m = ITEM_PATTERN.matcher(arr);
        while (m.find()) {
            String obj = m.group();
            String pkg = group(PKG_PATTERN, obj);
            if (pkg == null || pkg.isBlank()) continue;
            String action = def(group(ACTION_PATTERN, obj));
            String risk = def(group(RISK_PATTERN, obj));
            String reason = def(group(REASON_PATTERN, obj));
            map.put(pkg, new String[]{action, risk, reason});
        }
        return map;
    }

    /**
     * Clears the in-memory cache (e.g. if packs updated on disk).
     */
    public void clearCache() {
        cache.clear();
    }

    private static String group(Pattern p, String s) { Matcher m = p.matcher(s); return m.find() ? m.group(1) : null; }
    private static String def(String s) { return s == null ? "" : s; }
}
