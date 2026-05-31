# HatchMate Android - Project Structure

```
HatchMate-Android/
├── app/                              # Main application module
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/hatchmate/app/
│   │   │   │   ├── core/                      # Core business logic
│   │   │   │   │   ├── database/               # Room Database
│   │   │   │   │   │   ├── Entities.kt           # 6 Entity classes
│   │   │   │   │   │   ├── HatchMateDao.kt       # DAO with @Transactions
│   │   │   │   │   │   └── AppDatabase.kt        # Room Database class
│   │   │   │   │   └── notification/
│   │   │   │   │       ├── AlarmReceiver.kt      # Closed-loop alarm trigger
│   │   │   │   │       └── NotificationHelper.kt # Push notifications
│   │   │   │   ├── features/
│   │   │   │   │   └── HatchMateViewModel.kt # MVVM ViewModel
│   │   │   │   └── ui/
│   │   │   │       ├── MainActivity.kt       # Entry point
│   │   │   │       ├── MainDashboardScreen.kt # Compose screens
│   │   │   │       └── theme/
│   │   │   │           └── Theme.kt
│   │   │   ├── AndroidManifest.xml
│   │   │   └── res/
│   │   │       ├── values/
│   │   │       │   ├── strings.xml
│   │   │       │   └── themes.xml
│   │   │       └── [drawable, color, etc]
│   │   ├── test/
│   │   │   └── java/.../HatchMateViewModelTest.kt
│   │   └── androidTest/
│   │       └── java/.../HatchMateDaoTest.kt
│   ├── build.gradle.kts         # App-level build config
│   └── proguard-rules.pro       # ProGuard config
├── build.gradle.kts           # Project-level build
├── settings.gradle.kts        # Project settings
├── gradle.properties          # Gradle config
├── gradlew / gradlew.bat      # Gradle wrapper
├── README.md                  # Project overview
├── SETUP.md                   # Setup guide
├── ARCHITECTURE.md            # API reference
├── SAMPLE_USAGE.md            # Real-world examples
├── TESTING.md                 # Test guide
├── CONTRIBUTING.md            # Contribution guide
├── CHANGELOG.md               # Version history
├── PROJECT_STRUCTURE.md       # This file
└── .gitignore
```

## Layer Breakdown

### 1. **Data Layer** (`core/database/`)
- **Entities.kt**: 6 Room entities
  - MasterHarga (SSOT for pricing)
  - BatchInkubator (Incubation cycles)
  - BatchUnggas (Poultry rearing)
  - GudangStok (Inventory)
  - TransaksiKeuangan (Financial logs)
  - AnalisaAITetas (Hatch analysis)

- **HatchMateDao.kt**: Data Access Object
  - Basic CRUD operations
  - 3 Closed-Loop @Transaction methods:
    - `simpanHasilAnalisaAITetas()`
    - `eksekusiTransaksiPenjualan()`
    - `catatKematianUnggas()`

- **AppDatabase.kt**: Room Database singleton
  - Database initialization
  - Version management

### 2. **Notification Layer** (`core/notification/`)
- **AlarmReceiver.kt**: BroadcastReceiver
  - Triggers daily egg rotation reminders
  - Lock mechanism (only during active rotation dates)

- **NotificationHelper.kt**: Utility
  - Channel creation (API 26+)
  - Push notification display

### 3. **Business Layer** (`features/`)
- **HatchMateViewModel.kt**: MVVM ViewModel
  - StateFlow for reactive UI updates
  - HPP Real Cost calculator
  - Transaction triggering methods
  - Coroutine management

### 4. **Presentation Layer** (`ui/`)
- **MainActivity.kt**: Entry point
  - Database initialization
  - ViewModel creation
  - Compose setContent

- **MainDashboardScreen.kt**: Composable screens
  - Tab navigation
  - 4 main tabs: Inkubator, Unggas, AI Analisa, Penjualan
  - Reactive data binding

- **theme/Theme.kt**: Material 3 theming
  - Dark/Light mode support
  - Custom colors

## Data Flow

```
UI Events
    ↓
 ViewModel
    ↓
 DAO (@Transaction)
    ↓
 Room Database (SQLite)
    ↑
 StateFlow (reactive)
    ↑
 UI re-render
```

## Technology Mapping

| Layer | Technology |
|-------|-------------|
| UI | Jetpack Compose, Material 3 |
| State | StateFlow, MVVM |
| Business | ViewModel, suspend fun |
| Data | Room DAO, SQLite |
| Async | Coroutines, Flow |
| DI (Future) | Hilt |

## Build Configuration Hierarchy

```
gradle.properties (Global settings)
    ↓
build.gradle.kts (Project-level)
    ↓
app/build.gradle.kts (App-level)
    ↓
    ├── dependencies
    │   ├── AndroidX
    │   ├── Compose
    │   ├── Room
    │   ├── Coroutines
    │   └── Hilt
    └── android block
        ├── compileSdk = 34
        ├── minSdk = 31
        ├── buildFeatures { compose = true }
        └── kotlinOptions
```

## Testing Structure

```
test/                    (JVM Unit Tests)
└── HatchMateViewModelTest.kt
    - Mock DAO
    - Test business logic
    - Fast execution

androidTest/             (Instrumented Tests)
└── HatchMateDaoTest.kt
    - Real Room database (in-memory)
    - Test @Transactions
    - Test database constraints
```

---

**Total LOC**: ~1,500 lines  
**Build Time**: ~45 seconds (first), ~10 seconds (incremental)  
**APK Size**: ~8-12 MB (debug), ~4-6 MB (release with R8)
