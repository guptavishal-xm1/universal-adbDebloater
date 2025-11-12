package core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages connected devices via ADB.
 */
public class DeviceManager {
    private final AdbRunner adbRunner;

    public DeviceManager(AdbRunner adbRunner) {
        this.adbRunner = adbRunner;
    }

    private static final Pattern DEVICE_LINE = Pattern.compile("^(\\S+)\\s+device(.*)$");

    public List<String> listDeviceSerials() {
        CommandResult result = adbRunner.run("devices", "-l");
        List<String> serials = new ArrayList<>();
        if (!result.isSuccess()) {
            return serials; // empty if failed
        }
        for (String line : result.stdout().split("\n")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("List of devices")) continue;
            Matcher m = DEVICE_LINE.matcher(line);
            if (m.find()) {
                serials.add(m.group(1));
            }
        }
        return serials;
    }

    public DeviceInfo getDeviceInfo(String serial) {
        // Use individual getprop calls - batching doesn't work reliably across all devices
        String manufacturer = getProp(serial, "ro.product.manufacturer");
        String model = getProp(serial, "ro.product.model");
        String androidVersion = getProp(serial, "ro.build.version.release");
        
        System.out.println("DeviceManager: Device info - Manufacturer: '" + manufacturer + "', Model: '" + model + "', Android: '" + androidVersion + "'");
        return new DeviceInfo(serial, manufacturer, model, androidVersion);
    }

    public String getProp(String serial, String prop) {
        CommandResult res = adbRunner.run("-s", serial, "shell", "getprop", prop);
        if (!res.isSuccess()) return "";
        return res.stdout().trim();
    }
}
