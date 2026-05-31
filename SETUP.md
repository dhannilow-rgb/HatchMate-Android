# Setup & Installation Guide

## Prerequisites
- Android Studio Hedgehog (2023.1.1) atau lebih baru
- JDK 11+
- Kotlin Plugin 1.9.20+
- SDK API 31+ (untuk Build & Device Testing)

## Step 1: Import Project

1. Buka Android Studio
2. **File → Open** → Pilih folder `HatchMate-Android`
3. Tunggu Gradle sync selesai (~2-5 menit)

## Step 2: Konfigurasi Build

### Local Properties (opsional)
```properties
# ~/.android/local.properties
sdk.dir=/path/to/android/sdk
ndk.dir=/path/to/android/ndk
```

### Build Variants
- **Debug**: Untuk development dengan R8 disabled
- **Release**: Dengan ProGuard minification

## Step 3: Run Project

### Via Android Studio
1. Pilih emulator atau device fisik
2. Klik **Run** (Shift + F10)
3. Tunggu build & installation selesai

### Via Command Line
```bash
# Build APK Debug
./gradlew assembleDebug

# Build APK Release
./gradlew assembleRelease

# Install ke device terhubung
./gradlew installDebug

# Run langsung (Build + Install)
./gradlew runDebug
```

## Step 4: First Time Setup di App

### Persiapan Data Awal
Setelah app terbuka, Anda perlu menginput master data:

#### 1. Setup Master Harga (Tab Penjualan - Masuki Setting)
```kotlin
// Data yang perlu di-input
MasterHarga(
    itemName = "Telur Segar Ayam",
    hargaBeli = 1500.0,
    hargaJual = 2000.0
)

MasterHarga(
    itemName = "Telur Segar Bebek",
    hargaBeli = 2000.0,
    hargaJual = 2500.0
)

MasterHarga(
    itemName = "DOC",
    hargaBeli = 3000.0,
    hargaJual = 4000.0
)

MasterHarga(
    itemName = "DOD",
    hargaBeli = 4000.0,
    hargaJual = 5500.0
)
```

#### 2. Setup Gudang Stok (Tab Penjualan - Inventory)
```kotlin
GudangStok(
    itemName = "DOC",
    kuantitas = 0,
    kategori = "LOGISTIK"
)

GudangStok(
    itemName = "Telur Infertil",
    kuantitas = 0,
    kategori = "LOGISTIK"
)

GudangStok(
    itemName = "Pakan 20kg",
    kuantitas = 5,
    kategori = "PERALATAN"
)
```

#### 3. Input Batch Inkubator (Tab Inkubator - Add Button)
```kotlin
BatchInkubator(
    id = 1,
    tanggalMasuk = "2026-05-31",
    mulaiPutar = "2026-06-01",
    teropong = "2026-06-10",
    stopPutar = "2026-06-20",
    tanggalMenetas = "2026-06-21",
    jenisUnggas = "Ayam",
    suhu = 37.8,
    kelembapan = 60.0,
    perlakuanTambahan = "Spray 2x sehari mulai hari 10",
    isActive = true
)
```

## Architecture Explained

### MVVM + Repository Pattern

```
UI (Compose)
    ↓ (observes)
ViewModel (StateFlow)
    ↓ (launches coroutines)
Repository (optional layer)
    ↓
DAO (@Transaction)
    ↓
Room Database (SQLite)
```

### Transaction Safety

Semua operasi database critical menggunakan `@Transaction` decorator:

```kotlin
@Transaction
suspend fun simpanHasilAnalisaAITetas(analisa: AnalisaAITetas) {
    // Kedua operasi dijamin sukses semua atau gagal semua (ACID)
    insertAnalisaAI(analisa)
    tambahStokGudang("Telur Infertil", analisa.jumlahInfertil)
}
```

## Data Flow Examples

### Contoh 1: Input Hasil Tetas Batch

**Trigger**: Peternak input di Tab AI Analisa bahwa batch telur dari tanggal 2026-05-31 sudah menetas

```kotlin
val analisa = AnalisaAITetas(
    batchId = 1,
    tglMasuk = "2026-05-31",
    menetas = "2026-06-21",
    jumlahTotal = 1000,
    jumlahInfertil = 50,    // Telur tidak subur
    jumlahMenetas = 900,    // Berhasil menetas
    gagalMenetas = 50       // Gagal (busuk, mati dalam telur)
)

viewModel.inputAnalisaAITetas(analisa)

// ✅ OTOMATIS TERJADI:
// 1. Simpan analisa ke table `analisa_ai_tetas`
// 2. Tambah gudang_stok "Telur Infertil" sebanyak 50 unit
// 3. Ini adalah transaksi atomik - jika step 2 gagal, step 1 di-rollback
```

**Database State After**:
```sql
-- analisa_ai_tetas
INSERT INTO analisa_ai_tetas VALUES (1, '2026-05-31', '2026-06-21', 1000, 50, 900, 50, 100.0);

-- gudang_stok
UPDATE gudang_stok SET kuantitas = 50 WHERE itemName = 'Telur Infertil';
```

### Contoh 2: Penjualan DOC

**Trigger**: Peternak menjual 100 DOC dengan harga master Rp 4.000/ekor

```kotlin
viewModel.prosesPenjualanDigital("DOC", 100, "2026-05-31")

// ✅ OTOMATIS TERJADI:
// 1. Kurangi stok gudang DOC sebanyak 100
// 2. Catat transaksi masuk Rp 400.000 (100 × 4.000)
```

**Database State After**:
```sql
-- gudang_stok
UPDATE gudang_stok SET kuantitas = 0 WHERE itemName = 'DOC';
-- (jika sebelumnya ada 100)

-- transaksi_keuangan
INSERT INTO transaksi_keuangan VALUES (1, 'MASUK', 'DOC', 100, 400000.0, '2026-05-31');
```

### Contoh 3: Log Kematian Unggas

**Trigger**: Di tab Unggas, peternak mencatat 5 ekor mati dari batch pembesaran #1

```kotlin
viewModel.inputKematianUnggas(
    batchUnggasId = 1,
    jumlahMati = 5,
    jenisUnggas = "Ayam",
    batchTetasId = 1  // ID batch tetas yang menghasilkan unggas ini
)

// ✅ OTOMATIS TERJADI:
// 1. Kurangi jumlah di batch_unggas #1 dari 900 menjadi 895
// 2. Kurangi stok gudang DOC sebanyak 5 (karena 5 ekor mati)
// 3. Update efisiensi pembesaran batch tetas #1: 100% - (5×2.0%) = 90%
```

**Database State After**:
```sql
-- batch_unggas
UPDATE batch_unggas SET jumlah = 895 WHERE id = 1;

-- gudang_stok
UPDATE gudang_stok SET kuantitas = -5 WHERE itemName = 'DOC';

-- analisa_ai_tetas
UPDATE analisa_ai_tetas SET efisiensiPembesaran = 90.0 WHERE batchId = 1;
```

## Testing

### Unit Tests
```bash
./gradlew testDebug
```

### Instrumented Tests (Device/Emulator)
```bash
./gradlew connectedAndroidTest
```

### Test Data Initialization
Untuk testing, data awal dapat di-insert via `DatabasePopulator` helper:

```kotlin
// src/androidTest/.../DatabasePopulator.kt
object DatabasePopulator {
    suspend fun populateTestData(db: AppDatabase) {
        val dao = db.hatchMateDao()
        
        dao.insertMasterHarga(MasterHarga("Telur Segar Ayam", 1500.0, 2000.0))
        dao.insertGudangStok(GudangStok("DOC", 100, "LOGISTIK"))
        // ... dst
    }
}
```

## Troubleshooting

### Issue 1: Gradle Sync Failed
**Solution**:
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

### Issue 2: Room Schema Error
**Cause**: Perubahan entity tanpa migration  
**Solution**: Delete app data dan rebuild
```bash
./gradlew installDebug  # Will reinstall, clearing app data
```

### Issue 3: Compose Preview Tidak Muncul
**Solution**: Pastikan Kotlin Compose Compiler version match dengan build.gradle.kts

### Issue 4: Database Locked (Multithreading)
**Solution**: Room automatically handles this - semua coroutines di-queue oleh Room

## Performance Tips

1. **StateFlow Sharing**: Gunakan `.stateIn()` untuk menghindari multiple collectors
2. **Transaction Scope**: Keep transactions tetap kecil dan cepat
3. **Indexing**: Untuk queries sering, tambahkan @Index pada entities
4. **Pagination**: Untuk gudang stok >10k items, gunakan paging

## Next Steps

- [ ] Implementasi Hilt untuk Dependency Injection
- [ ] Add unit tests untuk DAO transactions
- [ ] Integrasi Firebase Analytics
- [ ] Export PDF Laporan Keuangan
- [ ] Sinkronisasi cloud (Firebase Realtime DB)
- [ ] Push notifications untuk alerts batch

---

**Butuh bantuan?** Lihat [Issues](https://github.com/dhannilow-rgb/HatchMate-Android/issues)
