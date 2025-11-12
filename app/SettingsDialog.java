package app;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import persistence.ConfigStore;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class SettingsDialog {
    private final ConfigStore config;
    private final Stage stage;

    public SettingsDialog(ConfigStore config) {
        this.config = config;
        this.stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Settings");
        buildUI();
    }

    private void buildUI() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        // Platform Tools Path
        Label platformToolsLabel = new Label("Platform Tools Folder:");
        TextField platformToolsField = new TextField(config.get("platformToolsPath"));
        platformToolsField.setPromptText("Leave empty to use system PATH");
        Button browsePlatformBtn = new Button("Browse Folder...");
        browsePlatformBtn.setOnAction(e -> {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Select Platform Tools Folder");
            File f = dc.showDialog(stage);
            if (f != null) {
                platformToolsField.setText(f.getAbsolutePath());
                // Validate that adb.exe exists in this folder
                Path adbPath = f.toPath().resolve(isWindows() ? "adb.exe" : "adb");
                if (!Files.exists(adbPath)) {
                    Alert warn = new Alert(Alert.AlertType.WARNING);
                    warn.setTitle("Warning");
                    warn.setHeaderText("ADB not found");
                    warn.setContentText("Could not find 'adb' in the selected folder.\nMake sure you selected the correct platform-tools directory.");
                    warn.showAndWait();
                }
            }
        });

        Label adbPathLabel = new Label("Or ADB Executable:");
        TextField adbPathField = new TextField(config.get("adbPath"));
        Button browseBtn = new Button("Browse File...");
        browseBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select ADB Executable");
            File f = fc.showOpenDialog(stage);
            if (f != null) adbPathField.setText(f.getAbsolutePath());
        });

        CheckBox autoUpdateCheck = new CheckBox("Auto-update platform-tools");
        autoUpdateCheck.setSelected(config.getBoolean("autoUpdatePlatformTools"));

        CheckBox telemetryCheck = new CheckBox("Send anonymous telemetry (opt-in)");
        telemetryCheck.setSelected(config.getBoolean("telemetryOptIn"));

        Label oemSourceLabel = new Label("OEM Pack Source:");
        ComboBox<String> oemSourceCombo = new ComboBox<>();
        oemSourceCombo.getItems().addAll("local", "remote");
        oemSourceCombo.setValue(config.get("oemPackSource"));

        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(e -> {
            // Priority: platform tools folder > specific adb path > system PATH
            String platformTools = platformToolsField.getText().trim();
            if (!platformTools.isBlank()) {
                config.set("platformToolsPath", platformTools);
                Path adbExe = Path.of(platformTools).resolve(isWindows() ? "adb.exe" : "adb");
                config.set("adbPath", adbExe.toString());
            } else {
                config.set("platformToolsPath", "");
                config.set("adbPath", adbPathField.getText().trim());
            }
            config.setBoolean("autoUpdatePlatformTools", autoUpdateCheck.isSelected());
            config.setBoolean("telemetryOptIn", telemetryCheck.isSelected());
            config.set("oemPackSource", oemSourceCombo.getValue());
            try {
                config.save();
                stage.close();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save config: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> stage.close());

        grid.add(platformToolsLabel, 0, 0);
        grid.add(platformToolsField, 1, 0);
        grid.add(browsePlatformBtn, 2, 0);
        grid.add(adbPathLabel, 0, 1);
        grid.add(adbPathField, 1, 1);
        grid.add(browseBtn, 2, 1);
        grid.add(autoUpdateCheck, 0, 2, 3, 1);
        grid.add(telemetryCheck, 0, 3, 3, 1);
        grid.add(oemSourceLabel, 0, 4);
        grid.add(oemSourceCombo, 1, 4, 2, 1);
        grid.add(saveBtn, 1, 5);
        grid.add(cancelBtn, 2, 5);

        Scene scene = new Scene(grid, 600, 300);
        stage.setScene(scene);
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public void show() {
        stage.showAndWait();
    }
}
