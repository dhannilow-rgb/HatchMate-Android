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
- **DI**: Hilt 2.48
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

```kotlin
HPP = HargaTelur + BiayaListrik + BiayaPenyusutan + BiayaPakan
BiayaListrik = (Watt / 1000) × JamInkubasi × TarifPLN
HargaJual = HPP × (1 + PersentaseKeuntungan) [Dibulatkan ke atas]
```

## 🔐 Keamanan & Integritas Data

- ✅ **@Transaction**: Semua operasi closed-loop bersifat atomik
- ✅ **REPLACE Strategy**: Cegah duplikasi master harga
- ✅ **Room Validation**: Type-safe queries
- ✅ **ProGuard Rules**: Perlindungan kode release

## 📞 Kontribusi

Jika ada saran atau bug report, silakan buat issue atau pull request!

---

**Dibuat oleh**: Senior Android Developer & System Architect  
**Lisensi**: MIT
