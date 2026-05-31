# HatchMate - Aplikasi ERP Penetasan Telur & Pembesaran Unggas

## 🐣 Deskripsi Proyek

HatchMate adalah aplikasi Android ERP profesional yang dirancang khusus untuk mengelola:
- **Siklus Inkubasi Telur**: Monitoring suhu, kelembapan, pemutaran telur otomatis
- **Manajemen Pembesaran Unggas**: Tracking kesehatan, pakan, vaksinasi
- **Inventori Gudang Real-time**: Stok DOC, pakan, telur, peralatan
- **Sistem Keuangan Terintegrasi**: Aliran Dana, Transaksi Masuk/Keluar, Harga Terkunci
- **Analisa AI Hasil Penetasan**: Rekomendasi otomatis untuk batch berikutnya

## 🏗️ Arsitektur Closed-Loop

### Prinsip Utama
1. **Aliran Dana = Single Source of Truth** → Semua harga terkunci dari master harga
2. **Analisa AI Otomatis** → Hasil tetas langsung mengupdate gudang (Telur Infertil masuk inventori)
3. **Log Kematian Atomik** → Potong stok DOC + update efisiensi AI secara transaksional

### Layer Arsitektur
```
┌─────────────────────────────────────┐
│   UI Layer (Jetpack Compose)        │  MainDashboardScreen
│   - Inkubator, Unggas, AI, Penjualan│
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   ViewModel Layer (StateFlow)       │  HatchMateViewModel
│   - HPP Calculator, Transaksi Logic │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   DAO Layer (@Transaction)          │  HatchMateDao
│   - Closed-Loop Operations          │
│   - Atomik & Konsisten              │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Room Database (SQLite)            │  AppDatabase
│   - 6 Tables (Master, Batch, Stok)  │
└─────────────────────────────────────┘
```

## 📊 Tabel Database

| Tabel | Fungsi | Primary Key |
|-------|--------|-------------|
| `master_harga` | Aliran Dana (SSOT Harga) | itemName |
| `batch_inkubator` | Siklus Penetasan | id (auto) |
| `batch_unggas` | Pembesaran Unggas | id (auto) |
| `gudang_stok` | Inventori Real-time | itemName |
| `transaksi_keuangan` | Log Transaksi Masuk/Keluar | id (auto) |
| `analisa_ai_tetas` | Hasil Tetas & Rekomendasi | batchId |

## 🔄 Logika Closed-Loop

### Transaksi 1: Input Analisa AI Tetas
```kotlin
simpanHasilAnalisaAITetas(analisa) {
    insertAnalisaAI(analisa)           // Simpan hasil tetas
    tambahStokGudang("Telur Infertil", jumlahInfertil)  // AUTO: Masuk Gudang
}
```

### Transaksi 2: Proses Penjualan
```kotlin
eksekusiTransaksiPenjualan(itemName, jumlah, harga, tanggal) {
    kurangiStokGudang(itemName, jumlah)  // Potong Stok
    insertTransaksiKeuangan(...)         // Catat Aliran Dana MASUK
}
```

### Transaksi 3: Log Kematian Unggas
```kotlin
catatKematianUnggas(batchId, jumlahMati, itemGudang, batchTetas) {
    kurangiJumlahUngggas(batchId, jumlahMati)      // Potong Batch Unggas
    kurangiStokGudang(itemGudang, jumlahMati)      // Potong DOC/DOD
    updatePenurunanEfisiensiAI(batchTetas, ...)    // Update Efisiensi -2% per ekor
}
```

## 🛠️ Tech Stack

- **Language**: Kotlin 1.9.20
- **UI**: Jetpack Compose (Material 3)
- **Database**: Room 2.6.1 + SQLite
- **State Management**: StateFlow + MVVM
- **Coroutines**: kotlinx-coroutines 1.7.3
- **DI**: Hilt 2.48 (prepared)
- **Min SDK**: API 31 | **Target SDK**: API 34

## 🚀 Quick Start

1. **Clone Repository**
   ```bash
   git clone https://github.com/dhannilow-rgb/HatchMate-Android.git
   cd HatchMate-Android
   ```

2. **Build & Run**
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

3. **Open di Android Studio**
   - File > Open > pilih folder HatchMate-Android

## 📋 Fitur Utama

### Tab Inkubator
- ✅ Input batch telur (tanggal masuk, jenis unggas, suhu, kelembapan)
- ✅ Monitoring siklus tetas (mulai putar, stop putar, tanggal menetas)
- ✅ Alarm otomatis pemutaran telur (hanya aktif dalam rentang tanggal)

### Tab Unggas
- ✅ Tracking usia, jumlah, status kesehatan
- ✅ Jadwal pakan & vaksin
- ✅ Tombol "Catat Kematian" → otomatis potong stok + update AI

### Tab AI Analisa
- ✅ Hasil tetas (total, berhasil, gagal, infertil)
- ✅ Efisiensi pembesaran (100% - penurunan dari kematian)
- ✅ Rekomendasi otomatis (suhu, kelembapan, sanitasi)

### Tab Penjualan
- ✅ Mesin kasir digital
- ✅ Potong stok otomatis per penjualan
- ✅ Catat aliran dana masuk

## 📈 Kalkulasi HPP Real Cost

```
HPP = HargaTelur + BiayaListrik + BiayaPenyusutan + BiayaPakan
BiayaListrik = (Watt / 1000) × JamInkubasi × TarifPLN
HargaJual = HPP × (1 + PersentaseKeuntungan) [Dibulatkan ke atas]
```

## 🗺️ Future Roadmap

### v1.1.0 - "Core Hardening" (Q3 2026)
- Hilt Dependency Injection
- Data validation & constraints
- Firebase Analytics integration
- Expanded unit tests (80%+ coverage)
- Enhanced error handling
- Offline-first architecture

### v1.2.0 - "Cloud Ready" (Q4 2026)
- Firebase Realtime Database sync
- User authentication & multi-user support
- Backup & restore functionality
- Multi-device synchronization
- PDF/Excel reporting & export

### v2.0.0 - "Enterprise Grade" (Q2 2027)
- Advanced analytics dashboard
- Predictive AI & ML models
- Advanced inventory optimization
- Quality assurance tracking
- Customer relationship management (CRM)

### v3.0.0 - "IoT & AI Integration" (Q4 2027)
- IoT sensor integration (Bluetooth BLE)
- Automated climate control
- ML optimization engine
- REST API & third-party integrations
- Web admin dashboard (Next.js)

**👉 See detailed roadmap**: [ROADMAP.md](ROADMAP.md)

## 🔐 Keamanan & Integritas Data

- ✅ **@Transaction**: Semua operasi closed-loop bersifat atomik
- ✅ **REPLACE Strategy**: Cegah duplikasi master harga
- ✅ **Room Validation**: Type-safe queries
- ✅ **ProGuard Rules**: Perlindungan kode release

## 📚 Documentation

| Document | Purpose |
|----------|---------|
| [SETUP.md](SETUP.md) | Installation & setup guide |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Complete API reference with examples |
| [SAMPLE_USAGE.md](SAMPLE_USAGE.md) | 8 real-world workflows |
| [TESTING.md](TESTING.md) | Unit test examples & coverage |
| [ROADMAP.md](ROADMAP.md) | **Detailed feature roadmap (v1.1 to v3.0)** ⭐ NEW |
| [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) | File organization |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Contribution guidelines |
| [CHANGELOG.md](CHANGELOG.md) | Version history |

## 📞 Kontribusi

Interested dalam contributing ke HatchMate? 🤝

1. **Check [ROADMAP.md](ROADMAP.md)** untuk upcoming features
2. **Read [CONTRIBUTING.md](CONTRIBUTING.md)** untuk guidelines
3. **Fork & create feature branch** untuk development
4. **Submit PR** dengan comprehensive tests
5. **Get merged** dan di-credit di changelog!

---

**Dibuat oleh**: Senior Android Developer & System Architect  
**Current Version**: 1.0.0 (31 May 2026)  
**Next Release**: 1.1.0 (Q3 2026)  
**Repository**: https://github.com/dhannilow-rgb/HatchMate-Android  
**Lisensi**: MIT
