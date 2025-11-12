# Contributing to Universal ADB Mobile Debloater

Thanks for your interest in contributing! This project aims to make Android device debloating safer, faster, and accessible.

## 🧾 Table of Contents
- Getting Started
- Development Setup
- Branching & Workflow
- Commit Message Guidelines
- Pull Request Checklist
- Issue Reporting
- Style & Conventions
- OEM Pack Contributions
- Security Policy

---

## 🚀 Getting Started
1. Fork the repository.
2. Clone your fork:
   ```bash
   cd universal-adb-mobile-debloater
   ```
3. Ensure Java 21+ is installed.
4. Build and run:
   ```bash
   ./gradlew build
   ./gradlew run
   ```

---

## 🛠 Development Setup
Optional helpful commands:
```bash
# Run tests
./gradlew test

# Continuous build
./gradlew build --continuous
```

### IDE Recommendations
- IntelliJ IDEA (import Gradle project)
- Enable annotations processing if introduced later

---

## 🌳 Branching & Workflow
We use a lightweight GitHub Flow:
- `main` — stable, releasable branch
- Feature branches: `feature/<short-description>`
- Fix branches: `fix/<short-description>`

Example:
```bash
git checkout -b feature/oem-motorola-pack
```

---

## ✍️ Commit Message Guidelines
Format:
```
<type>: <short summary>

[optional body]
```
Types:
- `feat`: new feature
- `fix`: bug fix
- `docs`: documentation changes
- `refactor`: code change without behavior change
- `perf`: performance improvement
- `test`: adding/updating tests
- `build`: build system changes

Example:
```
feat: add Samsung OEM pack with risk levels
```

---

## ✅ Pull Request Checklist
Before submitting:
- Code builds (`./gradlew build`)
- No obvious UI regressions
- Added tests if logic changed
- Updated README or docs if feature user-facing
- Linked issue in description (e.g., Closes #42)

Template suggestion:
```
### Summary
Short explanation of the change.

### Changes
- List major points

### Screenshots (if UI)
<attach images>

### Checklist
- [ ] Build passes
- [ ] Tests added/updated
- [ ] Docs updated
```

---

## 🐛 Issue Reporting
Create a GitHub Issue with:
- Clear title
- Environment (OS, Java version, device OEM/Android version if relevant)
- Steps to reproduce
- Expected vs actual behavior
- Logs (if available)

Labels:
- `bug`, `feature`, `enhancement`, `help wanted`, `good first issue`

---

## 🧩 Style & Conventions
- Java naming standards (PascalCase types, camelCase methods/vars)
- Avoid long methods (>60 lines) — split into helpers
- Keep UI thread responsive — use `Task` for long-running operations
- Favor immutability for data model classes (records where appropriate)
- Log meaningful warnings/errors (SLF4J)

---

## 📦 OEM Pack Contributions
To add a new OEM pack:
1. Create a file `oem-packs/<oem-name>.json`.
2. Use lowercase and hyphen-free OEM key if possible.
3. Follow structure:
```json
{
  "oem": "Motorola",
  "packages": [
    {
      "pkg": "com.motorola.bloat",
      "label": "Example App",
      "recommendedAction": "disable",
      "risk": "low",
      "reason": "Optional feature"
    }
  ]
}
```
4. Keep risk values: `low | medium | high`.
5. Use actionable reasons.

---

## 🔐 Security Policy
If you discover a vulnerability:
- Do NOT open a public issue immediately.
- Email: security@example.org (replace with maintainer email)
- Provide reproduction steps & potential impact.

We will acknowledge within 72 hours.

---

## ❤️ Thank You
Your contributions help make Android cleanup safer and easier for everyone.
