package app;

import core.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import persistence.ConfigStore;
import scripts.RestoreScriptBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main extends Application {
    private final ConfigStore config = ConfigStore.getDefault();
    private AdbRunner adb;
    private DeviceManager deviceManager;
    private PackageScanner packageScanner;
    private final SimpleOemPackLoader oemPackLoader = new SimpleOemPackLoader(Path.of("oem-packs"));
    private final ActionHistory history = new ActionHistory();

    private final ListView<String> devicesList = new ListView<>();
    private final TableView<Row> appsTable = new TableView<>();
    private final ObservableList<Row> appsData = FXCollections.observableArrayList();
    private FilteredList<Row> filteredData;
    private final Label statusBar = new Label("Ready");
    private final ProgressBar progressBar = new ProgressBar(0);
    private final VBox detailsPane = new VBox(8);
    private final TableView<ActionHistory.Entry> historyTable = new TableView<>();

    private String currentSerial = null;
    private Map<String, String[]> recommendations = new HashMap<>();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Universal ADB Mobile Debloater");

        // Initialize ADB with configured path
        String adbPath = config.get("adbPath");
        adb = new AdbRunner(adbPath.isBlank() ? "adb" : adbPath);
        deviceManager = new DeviceManager(adb);
        packageScanner = new PackageScanner(adb);

    BorderPane root = new BorderPane();
    root.setTop(buildTopBar());
        root.setCenter(buildCenter());
        root.setBottom(buildStatusBar());
        Scene scene = new Scene(root, 1200, 720);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        // Setup keyboard shortcuts
        setupKeyboardShortcuts(scene);
        
        stage.setScene(scene);
        stage.show();

        refreshDevices();
    }
    
    private void setupKeyboardShortcuts(Scene scene) {
        scene.setOnKeyPressed(event -> {
            // Ctrl+R or F5: Refresh devices
            if ((event.isControlDown() && event.getCode() == javafx.scene.input.KeyCode.R) ||
                event.getCode() == javafx.scene.input.KeyCode.F5) {
                refreshDevices();
                event.consume();
            }
            // Ctrl+A: Select all
            else if (event.isControlDown() && event.getCode() == javafx.scene.input.KeyCode.A && 
                     !appsTable.isFocused()) {
                selectAll(true);
                event.consume();
            }
            // Ctrl+Shift+A: Select none
            else if (event.isControlDown() && event.isShiftDown() && 
                     event.getCode() == javafx.scene.input.KeyCode.A) {
                selectAll(false);
                event.consume();
            }
            // Ctrl+D: Disable selected
            else if (event.isControlDown() && event.getCode() == javafx.scene.input.KeyCode.D) {
                applyToSelected("disable");
                event.consume();
            }
            // Ctrl+E: Enable selected
            else if (event.isControlDown() && event.getCode() == javafx.scene.input.KeyCode.E) {
                applyToSelected("enable");
                event.consume();
            }
            // Delete: Uninstall selected
            else if (event.getCode() == javafx.scene.input.KeyCode.DELETE) {
                applyToSelected("uninstall");
                event.consume();
            }
            // Ctrl+S: Settings
            else if (event.isControlDown() && event.getCode() == javafx.scene.input.KeyCode.S) {
                showSettings();
                event.consume();
            }
            // Ctrl+Shift+E: Export list
            else if (event.isControlDown() && event.isShiftDown() && 
                     event.getCode() == javafx.scene.input.KeyCode.E) {
                exportPackageList();
                event.consume();
            }
        });
    }

    private Node buildTopBar() {
        // Menu bar with View > High Contrast
        MenuBar menuBar = new MenuBar();
        Menu viewMenu = new Menu("View");
        CheckMenuItem highContrast = new CheckMenuItem("High Contrast mode");
        highContrast.setOnAction(e -> {
            Scene scene = menuBar.getScene();
            if (scene == null) return;
            String hc = getClass().getResource("/high-contrast.css").toExternalForm();
            if (highContrast.isSelected()) {
                if (!scene.getStylesheets().contains(hc)) scene.getStylesheets().add(hc);
            } else {
                scene.getStylesheets().remove(hc);
            }
        });
        viewMenu.getItems().add(highContrast);
        menuBar.getMenus().addAll(viewMenu);

        // Toolbar with overflow
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setOnAction(e -> refreshDevices());
        refreshBtn.setTooltip(new Tooltip("Refresh device list (Ctrl+R / F5)"));
        
        Button selectAllBtn = new Button("☑ Select All");
        selectAllBtn.setOnAction(e -> selectAll(true));
        selectAllBtn.setTooltip(new Tooltip("Select all packages (Ctrl+A)"));
        
        Button selectNoneBtn = new Button("☐ Select None");
        selectNoneBtn.setOnAction(e -> selectAll(false));
        selectNoneBtn.setTooltip(new Tooltip("Deselect all (Ctrl+Shift+A)"));
        
        Button selectRecommendedBtn = new Button("⭐ Select Recommended");
        selectRecommendedBtn.setOnAction(e -> selectRecommended());
        selectRecommendedBtn.setTooltip(new Tooltip("Auto-select bloatware based on OEM packs"));
        
        Button disableBtn = new Button("⏸ Disable");
        disableBtn.setOnAction(e -> applyToSelected("disable"));
        disableBtn.getStyleClass().add("action-button-disable");
        disableBtn.setTooltip(new Tooltip("Disable selected packages (Ctrl+D)"));
        
        Button uninstallBtn = new Button("🗑 Uninstall");
        uninstallBtn.setOnAction(e -> applyToSelected("uninstall"));
        uninstallBtn.getStyleClass().add("action-button-uninstall");
        uninstallBtn.setTooltip(new Tooltip("Uninstall selected packages (Delete)"));
        
        Button enableBtn = new Button("▶ Enable");
        enableBtn.setOnAction(e -> applyToSelected("enable"));
        enableBtn.getStyleClass().add("action-button-enable");
        enableBtn.setTooltip(new Tooltip("Re-enable selected packages (Ctrl+E)"));
        
        Button restoreBtn = new Button("💾 Create Restore Script");
        restoreBtn.setOnAction(e -> createRestoreScript());
        restoreBtn.setTooltip(new Tooltip("Generate recovery scripts for selected packages"));
        
        Button exportBtn = new Button("📤 Export List");
        exportBtn.setOnAction(e -> exportPackageList());
        exportBtn.setTooltip(new Tooltip("Export package list to CSV/TXT (Ctrl+Shift+E)"));
        
        Button helpBtn = new Button("❓");
        helpBtn.setOnAction(e -> showHelp());
        helpBtn.setTooltip(new Tooltip("Keyboard Shortcuts & Help"));
        
        Button settingsBtn = new Button("⚙ Settings");
        settingsBtn.setOnAction(e -> showSettings());
        settingsBtn.setTooltip(new Tooltip("Configure ADB and platform tools (Ctrl+S)"));

    TextField searchField = new TextField();
    searchField.setPromptText("🔍 Search packages...");
    searchField.textProperty().addListener((obs, o, n) -> curbFilter(n));
    searchField.setTooltip(new Tooltip("Filter packages by name or label"));
    searchField.setPrefWidth(260);
    searchField.setMinWidth(220);
    HBox.setHgrow(searchField, Priority.ALWAYS);

        ToolBar toolBar = new ToolBar(
            refreshBtn,
            selectAllBtn, selectNoneBtn, selectRecommendedBtn,
            new Separator(),
            disableBtn, uninstallBtn, enableBtn, restoreBtn, exportBtn,
            new Separator(),
            helpBtn, settingsBtn,
            new Separator(),
            searchField
        );
        toolBar.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1px 0;");

        VBox top = new VBox(menuBar, toolBar);
        return top;
    }

    private Node buildCenter() {
    // Left: devices list with header
    VBox deviceBox = new VBox(8);
    Label deviceHeader = new Label("📱 Connected Devices");
    deviceHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a; -fx-padding: 8px;");
    devicesList.setPrefWidth(280);
        devicesList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> onDeviceSelected(n));
    VBox.setVgrow(devicesList, Priority.ALWAYS);
    deviceBox.getChildren().addAll(deviceHeader, devicesList);
    deviceBox.setPadding(new Insets(8));

        // Center: apps table
        TableColumn<Row, Boolean> selCol = new TableColumn<>("✔");
        selCol.setCellValueFactory(param -> param.getValue().selectedProperty());
        selCol.setCellFactory(CheckBoxTableCell.forTableColumn(selCol));
        selCol.setPrefWidth(50);

        TableColumn<Row, String> pkgCol = new TableColumn<>("Package / Label");
        pkgCol.setCellValueFactory(param -> {
            Row r = param.getValue();
            String display = r.getLabel().isBlank() ? r.getPkg() : r.getLabel();
            return new javafx.beans.property.SimpleStringProperty(display);
        });
        pkgCol.setPrefWidth(420);

        TableColumn<Row, String> riskCol = new TableColumn<>("Risk");
        riskCol.setCellValueFactory(param -> param.getValue().riskProperty());
        riskCol.setPrefWidth(100);
        riskCol.setCellFactory(col -> new TableCell<Row, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("risk-high", "risk-medium", "risk-low", "risk-none");
                } else {
                    setText(item);
                    getStyleClass().removeAll("risk-high", "risk-medium", "risk-low", "risk-none");
                    getStyleClass().add("risk-" + item.toLowerCase());
                }
            }
        });

        TableColumn<Row, String> actionCol = new TableColumn<>("Recommended");
        actionCol.setCellValueFactory(param -> param.getValue().actionProperty());
        actionCol.setPrefWidth(150);

        TableColumn<Row, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(param -> param.getValue().typeProperty());
        typeCol.setPrefWidth(100);

    appsTable.getColumns().addAll(selCol, pkgCol, typeCol, riskCol, actionCol);
    filteredData = new FilteredList<>(appsData, r -> true);
    appsTable.setItems(filteredData);
    appsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        appsTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> showPackageDetails(n));
        
    // Table container with header
    VBox tableBox = new VBox(8);
    Label tableHeader = new Label("📦 Installed Packages");
    tableHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a; -fx-padding: 8px;");
    VBox.setVgrow(appsTable, Priority.ALWAYS);
    tableBox.getChildren().addAll(tableHeader, appsTable);
    tableBox.setPadding(new Insets(8));

        // Right: details pane + history log
        detailsPane.setPadding(new Insets(12));
        Label detailsTitle = new Label("Package Details");
    detailsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");
        Label noSelection = new Label("Select a package to view details");
    noSelection.setStyle("-fx-text-fill: #6b7280; -fx-font-style: italic;");
        detailsPane.getChildren().addAll(detailsTitle, noSelection);
        
    // History log table
        TableColumn<ActionHistory.Entry, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getTime()));
        timeCol.setPrefWidth(80);
        
        TableColumn<ActionHistory.Entry, String> statusCol = new TableColumn<>("✓");
        statusCol.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getStatus()));
        statusCol.setPrefWidth(40);
        
        TableColumn<ActionHistory.Entry, String> actCol = new TableColumn<>("Action");
        actCol.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getAction()));
        actCol.setPrefWidth(120);
        
        TableColumn<ActionHistory.Entry, String> detCol = new TableColumn<>("Details");
        detCol.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getDetails()));
        detCol.setPrefWidth(300);
        
    historyTable.getColumns().addAll(timeCol, statusCol, actCol, detCol);
    // Bind table to observable entries once for efficient incremental updates
    historyTable.setItems(history.getObservableEntries());
        historyTable.setPrefHeight(150);
        
        Button clearHistoryBtn = new Button("🗑 Clear History");
        clearHistoryBtn.setOnAction(e -> {
            history.clear();
            historyTable.getItems().clear();
        });
        clearHistoryBtn.setTooltip(new Tooltip("Clear all history entries"));
        
        Button exportHistoryBtn = new Button("💾 Export History");
        exportHistoryBtn.setOnAction(e -> exportHistory());
        exportHistoryBtn.setTooltip(new Tooltip("Save history log to text file"));
        
        Label historyLabel = new Label("📜 Action History");
        historyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e3a8a;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox historyToolbar = new HBox(10, historyLabel, spacer, clearHistoryBtn, exportHistoryBtn);
        historyToolbar.setPadding(new Insets(5));
        historyToolbar.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1px 0;");
        
        VBox historyBox = new VBox(5, historyToolbar, historyTable);
        VBox.setVgrow(historyTable, Priority.ALWAYS);
        
        TabPane rightTabs = new TabPane();
        Tab detailsTab = new Tab("Details", detailsPane);
        Tab historyTab = new Tab("History", historyBox);
        detailsTab.setClosable(false);
        historyTab.setClosable(false);
        rightTabs.getTabs().addAll(detailsTab, historyTab);

        SplitPane splitRight = new SplitPane(tableBox, rightTabs);
        splitRight.setDividerPositions(0.75);

        SplitPane split = new SplitPane(deviceBox, splitRight);
        split.setDividerPositions(0.25);
        return split;
    }

    private Node buildStatusBar() {
        HBox box = new HBox(10);
        box.setStyle("-fx-background-color: white; -fx-padding: 10px 16px; -fx-border-color: #e5e7eb; -fx-border-width: 1px 0 0 0;");
        progressBar.setPrefWidth(150);
        progressBar.setVisible(false);
        HBox.setHgrow(statusBar, Priority.ALWAYS);
        statusBar.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 12px;");
        box.getChildren().addAll(statusBar, progressBar);
        return box;
    }

    private void setStatus(String txt) { 
        Platform.runLater(() -> statusBar.setText(txt)); 
    }

    private void showProgress(boolean show) {
        Platform.runLater(() -> progressBar.setVisible(show));
    }

    private void refreshDevices() {
        showProgress(true);
        setStatus("Refreshing devices...");
        
        Task<List<String>> task = new Task<>() {
            @Override
            protected List<String> call() {
                return deviceManager.listDeviceSerials();
            }
        };
        
        task.setOnSucceeded(e -> {
            List<String> serials = task.getValue();
            ObservableList<String> items = FXCollections.observableArrayList(serials);
            devicesList.setItems(items);
            setStatus(serials.isEmpty() ? "No devices found" : ("Found " + serials.size() + " device(s)"));
            if (!serials.isEmpty()) devicesList.getSelectionModel().select(0);
            showProgress(false);
        });
        
        task.setOnFailed(e -> {
            setStatus("Failed to refresh devices");
            showProgress(false);
        });
        
        new Thread(task).start();
    }

    private void onDeviceSelected(String serial) {
        currentSerial = serial;
        if (serial == null) return;
        
        showProgress(true);
        setStatus("Loading device info...");
        
        Task<Void> task = new Task<>() {
            private DeviceInfo info;
            private List<PackageInfo> pkgs;
            
            @Override
            protected Void call() {
                try {
                    info = deviceManager.getDeviceInfo(serial);
                    updateMessage("Scanning packages for " + info.displayName());
                    
                    // Load recommendations for manufacturer
                    recommendations = oemPackLoader.loadForManufacturer(info.manufacturer(), "-20251113.json");
                    
                    // Scan packages
                    pkgs = packageScanner.listPackages(serial);
                    
                    if (pkgs.isEmpty()) {
                        updateMessage("Warning: No packages found. Check ADB connection and device permissions.");
                    }
                } catch (Exception e) {
                    updateMessage("Error: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
                return null;
            }
            
            @Override
            protected void succeeded() {
                appsData.clear();
                if (pkgs == null || pkgs.isEmpty()) {
                    setStatus("No packages found - check device connection and USB debugging");
                    Alert warn = new Alert(Alert.AlertType.WARNING);
                    warn.setTitle("No Packages Found");
                    warn.setHeaderText("Package scan returned empty");
                    warn.setContentText("Possible causes:\n• Device not authorized (check device screen for prompt)\n• ADB connection lost\n• Device PM service not responding\n\nCheck terminal output for detailed errors.");
                    warn.showAndWait();
                    showProgress(false);
                    return;
                }
                for (PackageInfo p : pkgs) {
                    String[] rec = recommendations.getOrDefault(p.pkg(), new String[]{"", "", ""});
                    String action = rec[0];
                    String risk = rec[1];
                    Row row = new Row(p.pkg(), p.systemApp(), action, risk);
                    row.setLabel(p.label());
                    appsData.add(row);
                }
                setStatus("Loaded " + pkgs.size() + " packages for " + info.displayName());
                showProgress(false);
            }
            
            @Override
            protected void failed() {
                Throwable ex = getException();
                String errMsg = ex != null ? ex.getMessage() : "Unknown error";
                setStatus("Failed to load packages: " + errMsg);
                System.err.println("Package load failed:");
                if (ex != null) ex.printStackTrace();
                
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Package Load Failed");
                alert.setHeaderText("Could not scan device packages");
                alert.setContentText("Error: " + errMsg + "\n\nCheck:\n• Device is authorized (screen prompt)\n• USB debugging enabled\n• ADB working (try 'adb devices' in terminal)");
                alert.showAndWait();
                showProgress(false);
            }
        };
        
        task.messageProperty().addListener((obs, oldMsg, newMsg) -> setStatus(newMsg));
        new Thread(task).start();
    }

    // Debounced filtering logic using a background thread
    private long lastFilterRequest = 0L;
    private final long FILTER_DEBOUNCE_MS = 160;
    private void curbFilter(String query) {
        lastFilterRequest = System.currentTimeMillis();
        long captured = lastFilterRequest;
        new Thread(() -> {
            try { Thread.sleep(FILTER_DEBOUNCE_MS); } catch (InterruptedException ignored) {}
            if (captured != lastFilterRequest) return; // superseded by a newer request
            Platform.runLater(() -> applyFilter(query));
        }, "filter-debounce").start();
    }

    private void applyFilter(String q) {
        String qq = (q == null ? "" : q.trim().toLowerCase(Locale.ROOT));
        filteredData.setPredicate(row -> {
            if (qq.isEmpty()) return true;
            return row.getPkg().toLowerCase(Locale.ROOT).contains(qq) || row.getLabel().toLowerCase(Locale.ROOT).contains(qq);
        });
        setStatus("Filtered: " + filteredData.size() + " / " + appsData.size());
    }

    private void selectAll(boolean selected) {
        for (Row r : filteredData) {
            r.setSelected(selected);
        }
        appsTable.refresh();
    }

    private void selectRecommended() {
        for (Row r : filteredData) {
            String action = r.getAction();
            r.setSelected(action != null && !action.isBlank() && 
                         !action.equalsIgnoreCase("keep"));
        }
        appsTable.refresh();
    }

    private void showPackageDetails(Row row) {
        detailsPane.getChildren().clear();
        if (row == null) {
            Label title = new Label("Package Details");
            title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");
            Label noSel = new Label("Select a package to view details");
            noSel.setStyle("-fx-text-fill: #6b7280; -fx-font-style: italic; -fx-padding: 8px 0;");
            detailsPane.getChildren().addAll(title, noSel);
            return;
        }

        Label title = new Label("Package Details");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a; -fx-padding: 0 0 12px 0;");

        Label pkgLabel = new Label("Package:");
        pkgLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #374151; -fx-font-size: 12px;");
        Label pkgValue = new Label(row.getPkg());
        pkgValue.setWrapText(true);
        pkgValue.setStyle("-fx-text-fill: #1f2937; -fx-padding: 4px 0 12px 0; -fx-font-family: 'Consolas', monospace;");

        Label typeLabel = new Label("Type:");
        typeLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #374151; -fx-font-size: 12px;");
        Label typeValue = new Label(row.getType());
        typeValue.setStyle("-fx-text-fill: #1f2937; -fx-padding: 4px 0 12px 0;");

        Label riskLabel = new Label("Risk Level:");
        riskLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #374151; -fx-font-size: 12px;");
        Label riskValue = new Label(row.getRisk().isBlank() ? "Not assessed" : row.getRisk());
        riskValue.setStyle("-fx-padding: 4px 0 12px 0;");
        if (!row.getRisk().isBlank()) {
            riskValue.getStyleClass().add("risk-" + row.getRisk().toLowerCase());
        }

        Label actionLabel = new Label("Recommended Action:");
        actionLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #374151; -fx-font-size: 12px;");
        Label actionValue = new Label(row.getAction().isBlank() ? "No recommendation" : row.getAction());
        actionValue.setStyle("-fx-text-fill: #1f2937; -fx-padding: 4px 0 12px 0; -fx-font-weight: 600;");

        Label reasonLabel = new Label("Reason:");
        reasonLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #374151; -fx-font-size: 12px;");
        String[] rec = recommendations.getOrDefault(row.getPkg(), new String[]{"", "", ""});
        Label reasonValue = new Label(rec[2].isBlank() ? "N/A" : rec[2]);
        reasonValue.setWrapText(true);
        reasonValue.setStyle("-fx-text-fill: #4b5563; -fx-padding: 4px 0 12px 0; -fx-line-spacing: 1.5;");

        detailsPane.getChildren().addAll(
            title,
            createStyledSeparator(),
            pkgLabel, pkgValue,
            typeLabel, typeValue,
            riskLabel, riskValue,
            actionLabel, actionValue,
            reasonLabel, reasonValue
        );
    }
    
    private Separator createStyledSeparator() {
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #e5e7eb;");
        sep.setPadding(new Insets(0, 0, 12, 0));
        return sep;
    }

    private void applyToSelected(String op) {
        if (currentSerial == null) { setStatus("No device selected"); return; }
        
        // Count selected and check for high-risk packages
        int count = 0;
        int highRisk = 0;
        List<Row> selectedRows = new ArrayList<>();
        for (Row r : filteredData) {
            if (r.isSelected()) {
                selectedRows.add(r);
                count++;
                if ("high".equalsIgnoreCase(r.getRisk())) highRisk++;
            }
        }
        
        if (count == 0) {
            setStatus("No packages selected");
            return;
        }

        // Confirmation dialog
        String message = String.format("Apply %s to %d package(s)?", op, count);
        if (highRisk > 0) {
            message += String.format("\n\nWARNING: %d high-risk package(s) selected!\nDisabling these may cause system instability.", highRisk);
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Action");
        confirm.setHeaderText("Confirm " + op.toUpperCase());
        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            setStatus("Operation cancelled");
            return;
        }

        showProgress(true);
        
        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() {
                int ok = 0;
                int current = 0;
                for (Row r : selectedRows) {
                    current++;
                    updateMessage(String.format("%s %d/%d: %s", op, current, selectedRows.size(), r.getPkg()));
                    updateProgress(current, selectedRows.size());
                    
                    CommandResult res;
                    switch (op) {
                        case "disable" -> res = adb.run("-s", currentSerial, "shell", "pm", "disable-user", "--user", "0", r.getPkg());
                        case "uninstall" -> res = adb.run("-s", currentSerial, "shell", "pm", "uninstall", "--user", "0", r.getPkg());
                        case "enable" -> res = adb.run("-s", currentSerial, "shell", "pm", "enable", r.getPkg());
                        default -> res = new CommandResult(-1, "", "Unknown op");
                    }
                    
                    boolean success = res.isSuccess();
                    if (success) ok++;
                    
                    // Log to history (append-only; table is already bound)
                    String actionName = op.toUpperCase();
                    Platform.runLater(() -> history.log(actionName, r.getPkg(), success));
                }
                return ok;
            }
        };
        
        task.setOnSucceeded(e -> {
            int ok = task.getValue();
            setStatus("Applied " + op + " to " + ok + "/" + selectedRows.size() + " selected");
            showProgress(false);
            // Unbind and reset progress bar
            if (progressBar.progressProperty().isBound()) progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
        });
        
        task.setOnFailed(e -> {
            setStatus("Failed to apply " + op);
            showProgress(false);
            if (progressBar.progressProperty().isBound()) progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
        });
        
        task.messageProperty().addListener((obs, oldMsg, newMsg) -> setStatus(newMsg));
        if (progressBar.progressProperty().isBound()) progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(task.progressProperty());
        
        new Thread(task).start();
    }

    private void createRestoreScript() {
        if (currentSerial == null) { setStatus("No device selected"); return; }
        try {
            List<PackageInfo> selected = new ArrayList<>();
            for (Row r : filteredData) {
                if (r.isSelected()) selected.add(new PackageInfo(r.getPkg(), "", "", r.getRisk(), "", r.getType().equals("System")));
            }
            if (selected.isEmpty()) {
                setStatus("No packages selected");
                return;
            }
            Path dir = RestoreScriptBuilder.createScripts(currentSerial, selected);
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Restore Scripts Created");
            info.setHeaderText("Scripts saved successfully");
            info.setContentText("Location: " + dir.toString());
            info.showAndWait();
            setStatus("Restore scripts created at " + dir);
        } catch (Exception ex) {
            setStatus("Failed to create restore scripts: " + ex.getMessage());
        }
    }

    private void showSettings() {
        SettingsDialog dialog = new SettingsDialog(config);
        dialog.show();
        // Reload ADB runner if path changed
        String newPath = config.get("adbPath");
        adb = new AdbRunner(newPath.isBlank() ? "adb" : newPath);
        deviceManager = new DeviceManager(adb);
        packageScanner = new PackageScanner(adb);
        setStatus("Settings saved. Restart may be required for some changes.");
    }
    
    private void exportPackageList() {
        if (filteredData.isEmpty()) {
            setStatus("No packages to export");
            return;
        }
        
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Package List");
        chooser.setInitialFileName("packages.csv");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        
    java.io.File outFile = chooser.showSaveDialog(appsTable.getScene().getWindow());
    if (outFile == null) return;
    Path file = outFile.toPath();
        
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("Package,Label,Type,Risk,Recommended Action\n");
            
            for (Row r : filteredData) {
                sb.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    r.getPkg(),
                    r.getLabel(),
                    r.getType(),
                    r.getRisk(),
                    r.getAction()));
            }
            
            Files.writeString(file, sb.toString());
            setStatus("Exported " + filteredData.size() + " packages to " + file.getFileName());
        } catch (IOException ex) {
            setStatus("Failed to export: " + ex.getMessage());
        }
    }
    
    private void exportHistory() {
        if (history.getEntries().isEmpty()) {
            setStatus("No history to export");
            return;
        }
        
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Action History");
        chooser.setInitialFileName("history.txt");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        
    java.io.File outFile = chooser.showSaveDialog(historyTable.getScene().getWindow());
    if (outFile == null) return;
    Path file = outFile.toPath();
        
        try {
            Files.writeString(file, history.exportToText());
            setStatus("Exported history to " + file.getFileName());
        } catch (IOException ex) {
            setStatus("Failed to export history: " + ex.getMessage());
        }
    }
    
    private void showHelp() {
        Alert help = new Alert(Alert.AlertType.INFORMATION);
        help.setTitle("Keyboard Shortcuts");
        help.setHeaderText("Universal ADB Mobile Debloater - Keyboard Shortcuts");
        
        String shortcuts = """
            Ctrl+R / F5       Refresh devices
            Ctrl+A            Select all packages
            Ctrl+Shift+A      Select none
            Ctrl+D            Disable selected packages
            Ctrl+E            Enable selected packages
            Delete            Uninstall selected packages
            Ctrl+S            Open settings
            Ctrl+Shift+E      Export package list
            
            Tips:
            • Use 'Select Recommended' to auto-select bloatware
            • High-risk packages show a warning before action
            • Always create a restore script before removing apps
            • Check the History tab to see all performed actions
            """;
        
        help.setContentText(shortcuts);
        help.getDialogPane().setMinWidth(500);
        help.showAndWait();
    }

    public static void main(String[] args) { launch(args); }

    // Row model for TableView with properties
    public static class Row {
        private final javafx.beans.property.BooleanProperty selected = new javafx.beans.property.SimpleBooleanProperty(false);
        private final javafx.beans.property.StringProperty pkg = new javafx.beans.property.SimpleStringProperty("");
        private final javafx.beans.property.StringProperty label = new javafx.beans.property.SimpleStringProperty("");
        private final javafx.beans.property.StringProperty type = new javafx.beans.property.SimpleStringProperty("");
        private final javafx.beans.property.StringProperty action = new javafx.beans.property.SimpleStringProperty("");
        private final javafx.beans.property.StringProperty risk = new javafx.beans.property.SimpleStringProperty("");

        public Row(String pkg, boolean system, String recommended, String risk) {
            this.pkg.set(pkg);
            this.type.set(system ? "System" : "User");
            this.action.set(recommended == null ? "" : recommended);
            this.risk.set(risk == null ? "" : risk);
        }

        public boolean isSelected() { return selected.get(); }
        public void setSelected(boolean v) { selected.set(v); }
        public javafx.beans.property.BooleanProperty selectedProperty() { return selected; }

        public String getPkg() { return pkg.get(); }
        public javafx.beans.property.StringProperty pkgProperty() { return pkg; }

        public String getLabel() { return label.get(); }
        public void setLabel(String v) { label.set(v); }
        public javafx.beans.property.StringProperty labelProperty() { return label; }

        public String getType() { return type.get(); }
        public javafx.beans.property.StringProperty typeProperty() { return type; }

        public String getAction() { return action.get(); }
        public javafx.beans.property.StringProperty actionProperty() { return action; }

        public String getRisk() { return risk.get(); }
        public javafx.beans.property.StringProperty riskProperty() { return risk; }
    }
}