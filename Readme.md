# Universal ADB Mobile Debloater# Universal ADB Mobile Debloater



> **Safely remove bloatware from Android devices using ADB.** Fast, reversible, and OEM‑aware.> Cross‑platform Java desktop app to safely debloat Android devices with ADB. Fast, reversible, and OEM‑aware.



A modern desktop tool to debloat Android phones and tablets. Works on **Windows, macOS, and Linux** with curated recommendations for popular brands.A modern JavaFX application that helps you find and disable/uninstall bloatware apps on Android devices using ADB. It includes curated OEM packs, bulk actions, restore scripts, action history, and an optional high‑contrast UI for accessibility.



![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk) ![JavaFX](https://img.shields.io/badge/JavaFX-21-blue) ![License](https://img.shields.io/badge/License-MIT-green)---



---## 🧭 Overview



## 🚀 Quick StartUniversal ADB Mobile Debloater (UADB) lets you:



### 1️⃣ Prerequisites- Detect connected Android devices via ADB

- List and search installed apps (system + user)

- **Java 21** or higher ([Download](https://adoptium.net/))- See risk labels and OEM‑recommended actions

- **Android device** with USB debugging enabled- Batch disable/uninstall/enable selected packages

- **ADB tools** ([Download](https://developer.android.com/tools/releases/platform-tools)) or let the app configure it for you- Generate restore scripts before changes (Windows .bat + Unix .sh)

- Review action history and export app lists

### 2️⃣ Enable USB Debugging on Your Device- Optionally auto‑update platform‑tools (adb/fastboot)

- Run long operations on background threads (no UI freezes)

1. Go to **Settings → About Phone**- Toggle a high‑contrast theme for better visibility

2. Tap **Build Number** 7 times to enable Developer Options

3. Go to **Settings → System → Developer Options**Built with Java 21 and JavaFX 21 for Windows, macOS, and Linux.

4. Enable **USB Debugging**

5. Connect your device via USB and authorize the computer when prompted---



### 3️⃣ Run the App## ✨ Features



**Windows (PowerShell):**- Multi‑device detection via ADB

```powershell- Fast package scanning (no per‑app dumpsys)

./gradlew.bat run- Risk labels and OEM recommendations

```- Batch actions: Disable, Uninstall, Enable

- Restore script generation (revert commands)

**macOS/Linux:**- Action History + export

```bash- Live search, sort, and filtering

./gradlew run- Background Tasks with progress

```- Settings for ADB/platform‑tools path

- High‑contrast UI toggle

### 4️⃣ First-Time Setup- OEM packs included: OnePlus, Nothing, OPPO, vivo, iQOO, Samsung, Xiaomi, Realme



1. When the app launches, click **⚙ Settings** (or press `Ctrl+S`)---

2. Browse to your **platform-tools** folder (where `adb.exe` or `adb` is located)

3. Click **Save**## 🧰 Tech Stack

4. Click **🔄 Refresh** (or press `F5`) to detect your device

- Language: Java 21

### 5️⃣ Debloat Your Device- UI: JavaFX 21

- Build: Gradle (Kotlin DSL) + Wrapper

1. **Select your device** from the left panel- Logging: SLF4J + Logback

2. Wait for the package list to load- JSON: Lightweight custom parsing (no heavy dependencies)

3. Click **⭐ Select Recommended** to auto-select bloatware

4. Review the selected apps (check the **Risk** column)---

5. Click **💾 Create Restore Script** (recommended!)

6. Choose an action:## 🚀 Getting Started

   - **⏸ Disable** — Safe, reversible (recommended for beginners)

   - **🗑 Uninstall** — Removes the app for the current user### Requirements

   - **▶ Enable** — Re-enable previously disabled apps

- Java 21 (LTS)

---- Android device with USB debugging enabled

- Android Platform‑Tools (adb). You can point the app to your platform‑tools folder in Settings.

## ✨ Key Features

### Build and Run (Windows PowerShell)

✅ **Smart Recommendations** — Curated lists for OnePlus, Samsung, Xiaomi, OPPO, vivo, iQOO, Nothing, Realme  

✅ **Risk Assessment** — See which apps are safe to remove (Low/Medium/High risk)  ```powershell

✅ **Restore Scripts** — Auto-generated `.bat`/`.sh` files to undo changes  # From project root

✅ **Action History** — Track every change with timestamps  ./gradlew.bat build ; ./gradlew.bat run

✅ **Fast Search** — Filter packages instantly as you type  ```

✅ **Batch Operations** — Disable multiple apps at once  

✅ **High Contrast Mode** — Accessibility-friendly UI  ### Build and Run (macOS/Linux)

✅ **No Root Required** — Works with standard ADB

```bash

---./gradlew build && ./gradlew run

```

## ⌨️ Keyboard Shortcuts

If you prefer a system Gradle, `gradle build` and `gradle run` also work.

| Shortcut | Action |

|----------|--------|---

| `Ctrl+R` / `F5` | Refresh device list |

| `Ctrl+A` | Select all packages |## 🧭 Usage

| `Ctrl+Shift+A` | Deselect all |

| `Ctrl+D` | Disable selected |1. Launch the app.

| `Ctrl+E` | Enable selected |2. Open Settings (Ctrl+S) and set your platform‑tools folder (where `adb`/`adb.exe` lives) if needed.

| `Delete` | Uninstall selected |3. Connect your Android device with USB debugging enabled.

| `Ctrl+S` | Open settings |4. Refresh devices (F5) to populate the list.

| `Ctrl+Shift+E` | Export package list |5. Select a device and wait for packages to load.

6. Optionally click “Select Recommended” to auto‑select OEM‑recommended bloatware.

---7. Choose Disable/Uninstall/Enable for selected items.

8. Create a Restore Script before removing apps (recommended).

## 🛡️ Safety Features9. Review the History tab to verify actions.



- **Risk Warnings** — High-risk packages require manual confirmationRestore scripts are saved in a timestamped folder and include both Windows (.bat) and Unix (.sh) variants.

- **Restore Scripts** — Auto-generated before any action

- **Non-Destructive Default** — Uses `pm disable-user` instead of full uninstall---

- **Action History** — Every change is logged locally

- **No Root** — Works without rooting your device## ⌨️ Keyboard Shortcuts



---- Ctrl+R / F5 — Refresh devices

- Ctrl+A — Select all

## 📦 Supported Devices- Ctrl+Shift+A — Deselect all

- Ctrl+D — Disable selected

Pre-configured bloatware lists for:- Ctrl+E — Enable selected

- Delete — Uninstall selected

- **OnePlus** (OxygenOS)- Ctrl+S — Settings

- **Samsung** (One UI)This file moved. Please see the canonical documentation in `README.md`.

- **Xiaomi** (MIUI / HyperOS)If both files appear on your system, delete `Readme.md` and keep only `README.md`.

- **OPPO** (ColorOS)    "windows": "...zip",

- **vivo** (Funtouch OS)    "macos": "...zip",

- **iQOO** (OriginOS)    "linux": "...zip"

- **Nothing** (Nothing OS)  },

- **Realme** (Realme UI)  "sha256": { "windows": "...", "macos": "...", "linux": "..." }

}

Works with any Android device — generic recommendations applied if your brand isn't listed.```



---2. Compare to local version (`adb version`).

3. Download & verify checksum.

## 🔧 Troubleshooting4. Replace binaries safely and restart adb.



### Device not detected?---



1. Check USB debugging is enabled on your device## 🪶 Configuration

2. Authorize the computer when prompted on device screen

3. Run `adb devices` in terminal to verify connection`config.json` stored under:

4. Try `adb kill-server` then `adb start-server`

* Windows: `%APPDATA%/UniversalADB/config.json`

### "Failed to load packages"?* macOS/Linux: `~/.config/universal-adb-debloater/config.json`



1. Check Settings → platform-tools path points to correct folderExample:

2. Ensure device is authorized (check device screen)

3. Look at terminal output for detailed error messages```json

4. Try disconnecting and reconnecting USB{

  "adbPath": "./platform-tools/windows/adb.exe",

### No packages appear?  "autoUpdatePlatformTools": true,

  "oemPackSource": "local",

1. Verify Settings → ADB path is correct  "telemetryOptIn": false

2. On Linux/macOS, ensure `adb` is executable: `chmod +x adb`}

3. Check terminal output for permission errors```



------



## 📂 Project Structure## 🧩 UI Layout (JavaFX)



``````

universal-adb-debloater/+------------------------------------------------------------+

├─ app/                # JavaFX UI (Main.java, SettingsDialog)| Toolbar: [Refresh] [Update Tools] [Settings] [Search 🔍]   |

├─ core/               # ADB runner, device/package management, OEM packs+------------------------------------------------------------+

├─ persistence/        # Config storage (JSON-based)| Devices Pane |          Apps Table          |  Details Pane |

├─ scripts/            # Restore script generation|--------------|------------------------------|----------------|

├─ updater/            # Platform-tools auto-update (download & verify)| • Device #1  | [☑] App Name   | Risk | Action | Info |      |

├─ oem-packs/          # Brand-specific bloatware JSON files| • Device #2  | [☐] com.pkg... | Low  | Disable| ...  |      |

├─ resources/          # CSS styles, high-contrast theme, logging config|--------------|------------------------------|----------------|

└─ test/               # Unit tests| Bottom: Status + progress + logs                               |

```+------------------------------------------------------------+

```

---

Dialogs:

## 🧪 For Developers

* **Bulk Action Preview**: table + confirm

### Build* **Restore Script Created**: show file path

* **High-Risk Confirmation**: serial type-in

```bash

./gradlew build---

```

## 🔒 Security & Ethics

### Run

* App runs locally, no data sent unless telemetry is **opt-in**.

```bash* Warns user for system-critical packages.

./gradlew run* Shows “Do not disable system UI” warning on startup.

```* Never executes root-only commands unless user enables “Expert Mode”.



### Test---



```bash## 🧪 Testing

./gradlew test

```### Unit Tests



### Package Native Installer* `AdbRunnerTest`: mocks ProcessBuilder, checks parsing

* `OemPackLoaderTest`: validates schema loading

```bash* `RestoreScriptBuilderTest`: ensures revert scripts correct

./gradlew jpackage

```### Integration Tests



Outputs `.exe` (Windows), `.dmg` (macOS), or `.deb`/`.rpm` (Linux) to `build/distributions/`.* Use Android Emulator in CI:



---  * `adb devices`

  * `pm list packages`

## 🤝 Contributing  * Test disable/enable commands



Contributions welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) and [Code of Conduct](CODE_OF_CONDUCT.md).### Run Tests



**Good first issues:**```bash

gradle test

- Add more OEM packs (Motorola, Google Pixel, ASUS, etc.)```

- Wire "Update Tools" button to auto-download platform-tools

- Add table filters for Risk level (High/Medium/Low) or Type (System/User)---

- Improve package labels by querying actual app names

## ⚙️ CI/CD (GitHub Actions)

---

Pipeline:

## 🔒 Security

1. Build on Windows/macOS/Linux

- **Local-only** — No data sent to external servers2. Run unit + integration tests

- **Open Source** — Review all code before running3. Package native apps via `jpackage`

- **Checksum Verification** — Platform-tools updates verified via SHA2564. Upload artifacts to GitHub Releases

- **Non-destructive** — Default action is `disable-user`, not permanent removal

Example snippet:

Report security issues: See [SECURITY.md](SECURITY.md)

```yaml

---jobs:

  build:

## 📜 License    runs-on: ${{ matrix.os }}

    strategy:

MIT License — See [LICENSE](LICENSE)      matrix:

        os: [ubuntu-latest, windows-latest, macos-latest]

Free for personal and educational use.    steps:

      - uses: actions/checkout@v4

---      - uses: actions/setup-java@v4

        with:

## ❤️ Acknowledgements          java-version: 21

          distribution: temurin

- Built with [JavaFX](https://openjfx.io/)      - run: gradle build

- Uses official [Android Platform Tools](https://developer.android.com/tools/releases/platform-tools) (ADB/Fastboot)      - run: gradle test

- OEM bloatware data curated from community sources and verified manually      - run: gradle jpackage

```

---

---

## 📢 FAQ

## 🌐 Future Roadmap

**Q: Will this brick my phone?**  

A: No. The default "Disable" action is fully reversible. We don't touch system-critical apps by default.* Plugin system for community OEM packs

* AI assistant for safe/unnecessary app detection

**Q: Do I need root?**  * Driver installer UI for Windows users

A: No. Standard ADB access is sufficient.* Root-only advanced features

* Cloud sync for debloat history

**Q: Can I undo changes?**  

A: Yes! Use the auto-generated restore scripts, or manually re-enable apps in the app.---



**Q: What's the difference between Disable and Uninstall?**  ## 🪙 License

A: **Disable** keeps the app on device but stops it from running. **Uninstall** removes it for the current user (can still be restored from `/system` on factory reset).

MIT License — Free for personal and educational use.

**Q: Is it safe to remove all recommended apps?**  

A: Most recommended apps are safe to remove. Always check the **Risk** column — avoid disabling "High" risk apps unless you know what you're doing.---



**Q: My language/region apps are missing after debloat?**  ## 🧩 Credits

A: Some system apps provide regional features. Check the restore script and re-enable specific apps if needed.

Built with ❤️ in JavaFX

---Uses official Google platform-tools (adb, fastboot).

OEM pack data curated from open community sources and verified manually.

**Ready to debloat?** Launch the app and enjoy a cleaner Android experience! 🚀

---

## 📢 Developer Tasks Summary (for Copilot AI)

| Task                                                 | Output        |
| ---------------------------------------------------- | ------------- |
| Create JavaFX main window (three-pane layout)        | FXML + CSS    |
| Implement `AdbRunner` (ProcessBuilder wrapper)       | core/adb      |
| Build `DeviceManager` and `PackageScanner`           | core/device   |
| Implement `OemPackLoader` and JSON mapping           | oem-packs     |
| Integrate OEM pack detection by manufacturer         | core/device   |
| Add bulk action modal with restore script generation | ui            |
| Add platform-tools updater                           | updater       |
| Add configuration and settings                       | persistence   |
| Implement non-destructive default (disable-user)     | core/packages |
| Package app via `jpackage`                           | build.gradle  |
| Write unit tests for core classes                    | test          |
| Verify builds on Windows/macOS/Linux                 | CI            |

---

## ✅ Acceptance Criteria

* App launches successfully on Windows, macOS, Linux
* Detects Android devices via `adb devices`
* Displays installed apps with labels, type, size
* Loads correct OEM pack and marks recommended packages
* Executes disable/uninstall commands and shows results
* Generates restore script with valid revert commands
* Updates platform-tools via secure channel with checksum
* Provides Settings page with all toggles
* Successfully packages installer via `jpackage`
* Passes unit & integration tests

---

**End of README**
* Updates platform-tools via secure channel with checksum

* Provides Settings page with all toggles
