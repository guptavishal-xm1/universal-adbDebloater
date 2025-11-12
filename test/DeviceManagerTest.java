import core.AdbRunner;
import core.CommandResult;
import core.DeviceManager;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Basic parsing test without invoking real adb. */
public class DeviceManagerTest {

    @Test
    void parsesDeviceSerials() throws Exception {
        // Create a fake AdbRunner via anonymous subclass overriding run
        AdbRunner fake = new AdbRunner("adb") {
            @Override
            public CommandResult run(String... args) {
                String sample = "List of devices attached\n" +
                        "emulator-5554 device product:sdk_gphone64_x86_64 model:Android_SDK_built_for_x86_64 device:emulator\n" +
                        "ABCD1234 device usb:123456789 transport_id:1\n";
                return new CommandResult(0, sample, "");
            }
        };
        DeviceManager dm = new DeviceManager(fake);
        List<String> serials = dm.listDeviceSerials();
        assertEquals(2, serials.size());
        assertTrue(serials.contains("emulator-5554"));
        assertTrue(serials.contains("ABCD1234"));
    }
}
