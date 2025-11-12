package core;

public record PackageInfo(
        String pkg,
        String label,
        String recommendedAction, // e.g., disable, uninstall, keep
        String risk,              // e.g., low, medium, high
        String reason,
        boolean systemApp
) {
    public static PackageInfo of(String pkg) {
        return new PackageInfo(pkg, "", "", "", "", false);
    }

    public PackageInfo withRecommendation(String action, String risk, String reason) {
        return new PackageInfo(pkg, label, action == null ? "" : action, risk == null ? "" : risk, reason == null ? "" : reason, systemApp);
    }

    public PackageInfo withSystem(boolean system) {
        return new PackageInfo(pkg, label, recommendedAction, risk, reason, system);
    }

    public PackageInfo withLabel(String lbl) {
        return new PackageInfo(pkg, lbl == null ? "" : lbl, recommendedAction, risk, reason, systemApp);
    }
}