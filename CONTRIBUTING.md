# Contributing Guidelines

Sama-sama kita tingkatkan HatchMate! 🐣

## How to Contribute

### 1. Fork Repository
```bash
git clone https://github.com/YOUR_USERNAME/HatchMate-Android.git
cd HatchMate-Android
```

### 2. Create Feature Branch
```bash
git checkout -b feature/amazing-feature
```

### 3. Make Your Changes
- Follow Kotlin style guide
- Add tests untuk new features
- Update documentation jika perlu

### 4. Commit dengan Pesan yang Jelas
```bash
git commit -m "feat: Add amazing feature"
# atau
git commit -m "fix: Resolve bug in transaction logic"
```

Ketentuan commit messages:
- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation changes
- `refactor:` - Code refactoring tanpa mengubah functionality
- `test:` - Adding/updating tests
- `chore:` - Build, dependency updates

### 5. Push ke Fork
```bash
git push origin feature/amazing-feature
```

### 6. Open Pull Request
- Describe perubahan Anda secara detail
- Reference any related issues
- Screenshot/GIF untuk UI changes

## Code Style

### Kotlin
```kotlin
// DO: Descriptive names, proper formatting
fun processSaleTransaction(itemName: String, quantity: Int) {
    viewModelScope.launch {
        dao.eksekusiTransaksiPenjualan(itemName, quantity, harga, tanggal)
    }
}

// DON'T: Abbreviations, unclear logic
fun pst(i: String, q: Int) { ... }
```

### Compose
```kotlin
// DO: Readable structure
@Composable
fun SalesScreen(viewModel: HatchMateViewModel) {
    val stok by viewModel.gudangStokList.collectAsState()
    
    Column { ... }
}

// DON'T: Nested hell
@Composable
fun S(vm: VM) { Column { Row { Text(...) } } }
```

## Testing Requirements

- Semua transactions harus ada test case
- Unit tests minimal 80% code coverage
- Instrumented tests untuk database operations

```bash
./gradlew test                  # Unit tests
./gradlew connectedAndroidTest  # Device tests
```

## Reporting Issues

### Bug Report Template
```markdown
## Description
Jelas jelaskan bug yang Anda alami

## Steps to Reproduce
1. Buka Tab Penjualan
2. Klik tombol Checkout
3. Bug terjadi

## Expected Behavior
Yang seharusnya terjadi

## Actual Behavior
Yang benar-benar terjadi

## Device Info
- Android Version: API 33
- Device: Pixel 6
- App Version: 1.0.0

## Screenshots
[Attach screenshot]
```

### Feature Request Template
```markdown
## Feature Description
Gambarkan feature yang Anda minta

## Use Case
Why ini penting?

## Proposed Solution
Bagaimana seharusnya feature ini bekerja?

## Alternatives Considered
Apakah ada cara lain?
```

## Review Process

1. Maintainer akan review PR Anda
2. Mungkin ada request untuk changes
3. Setelah approved, PR akan di-merge
4. Kontribusi Anda akan di-credit di CHANGELOG

## Questions?

Buat issue atau diskusi di repository!

Terima kasih sudah berkontribusi! 🙋
