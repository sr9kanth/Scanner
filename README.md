# CleanGuard AI

**AI Phone Health Checkup** — An AI-powered Android security and phone health assistant.

CleanGuard AI helps parents, grandparents, and non-technical users identify and remove malware, adware, scam apps, and privacy risks from their Android phones — all explained in plain, friendly language.

## Features

- **AI-Powered Scanning** — Dual AI consensus engine (Gemini 2.5 Flash + DeepSeek Chat)
- **App Risk Scoring** — Configurable scoring based on permissions, installer source, accessibility abuse, and threat intelligence
- **Accessibility Service Detection** — Identifies apps that can see and control your screen
- **Overlay/Pop-up Detection** — Finds apps that can show pop-ups over other apps
- **Notification Abuse Monitor** — Tracks notification frequency and flags spammy apps
- **Chrome Cleanup Wizard** — Step-by-step guide to fix browser popup and notification abuse
- **Screenshot AI Analysis** — Upload any screenshot for AI threat analysis
- **Parent Mode + Grandparent Mode** — Simplified language and larger text for non-technical users
- **Health Report** — Exportable phone health report with actionable recommendations

## Technology Stack

| Layer | Technology |
|-------|------------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Database | Room |
| Networking | Retrofit + OkHttp |
| Async | Coroutines + Flow |
| Navigation | Compose Navigation |
| Background | WorkManager |
| AI | Gemini 2.5 Flash, DeepSeek Chat |

## Getting Started

### Prerequisites

- Android Studio Iguana (2023.2.1) or newer
- JDK 17
- Android SDK API 30+

### Setup

1. Clone the repository and open `CleanGuardAI/` in Android Studio
2. Create a `local.properties` file in the `CleanGuardAI/` root:

```properties
sdk.dir=/path/to/your/android/sdk
GEMINI_API_KEY=your_gemini_api_key_here
DEEPSEEK_API_KEY=your_deepseek_api_key_here
VIRUSTOTAL_API_KEY=your_virustotal_api_key_here
```

3. Get your API keys:
   - **Gemini**: https://aistudio.google.com/app/apikey
   - **DeepSeek**: https://platform.deepseek.com/api-keys
   - **VirusTotal**: https://www.virustotal.com/gui/my-apikey

4. Sync Gradle and run on a device or emulator

### Building

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test

# Run lint
./gradlew lintDebug
```

## Architecture

```
app/
├── data/               # Data layer
│   ├── local/           # Room database, DAOs, entities
│   ├── remote/          # Retrofit APIs, DTOs
│   └── repository/      # Repository implementations
├── domain/             # Business logic
│   ├── model/           # Domain models
│   ├── repository/      # Repository interfaces
│   └── usecase/         # Use cases
├── engine/             # Risk scoring + AI consensus
├── presentation/       # UI layer
│   ├── navigation/
│   ├── theme/
│   ├── components/
│   ├── dashboard/
│   ├── scanner/
│   ├── apps/
│   ├── accessibility/
│   ├── overlay/
│   ├── notifications/
│   ├── chrome/
│   ├── screenshot/
│   ├── report/
│   └── settings/
├── service/            # NotificationListenerService
├── worker/             # WorkManager background scan
└── di/                 # Hilt DI modules
```

## Risk Scoring

| Signal | Score |
|--------|-------|
| Accessibility Service Active | +40 |
| Overlay Permission | +30 |
| Not from Play Store | +30 |
| Unknown Installer | +30 |
| Installed < 30 days ago | +15 |
| 20+ permissions | +15 |
| Notification permission | +10 |
| Known Adware | +80 |
| Known Scam App | +100 |

| Score | Risk Level |
|-------|------------|
| 0–29 | Safe |
| 30–59 | Review |
| 60–99 | Suspicious |
| 100+ | Remove Immediately |

## CI/CD

GitHub Actions pipeline at `.github/workflows/ci.yml` runs:
1. Unit tests
2. Lint checks
3. Debug APK build
4. Release APK build (on main branch only)

## Privacy

- No personal data is collected or uploaded
- No APK files are uploaded (only SHA256 hashes for VirusTotal)
- All AI requests use only technical app metadata
- Local-only mode available (disable API calls in Settings)
- See `PRIVACY_POLICY.md` for full details

## Future Roadmap

- [ ] On-device Gemma 3n AI (offline analysis)
- [ ] Family Dashboard
- [ ] Remote Assistance
- [ ] Weekly Health Report emails
- [ ] Wear OS companion app
- [ ] Cloud backup & multi-device management
