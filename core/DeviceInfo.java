package core;

public record DeviceInfo(String serial, String manufacturer, String model, String androidVersion) {
    public String displayName() {
        String mfr = manufacturer == null || manufacturer.isBlank() ? "Unknown" : capitalize(manufacturer);
        String mdl = model == null || model.isBlank() ? "Device" : model;
        return mfr + " " + mdl + " (" + serial + ")";
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }
}