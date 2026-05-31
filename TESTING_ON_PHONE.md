# HatchMate - Cara Testing di HP (Android Device)

## 🎯 Ada 2 Cara untuk Testing:

### **Opsi 1: Testing di Emulator (Virtual Device)** - Lebih Mudah ✅
### **Opsi 2: Testing di HP Fisik (Real Device)** - Lebih Real

---

## ✅ Opsi 1: Testing di Emulator Virtual

### Keuntungan:
- ✅ Tidak perlu HP/Tablet
- ✅ Cepat untuk development
- ✅ Mudah debug
- ✅ Bisa pause & inspect

### Kekurangan:
- ⏱️ Agak lambat (depends on laptop)
- 💾 Butuh disk space (~8GB)
- 🖥️ Need 8GB+ RAM recommended

### Langkah-Langkah:

#### **Step 1: Buka Android Studio**
```
File > Settings > Tools > Android SDK Manager
```

#### **Step 2: Install SDK API 31+ (jika belum ada)**
```
Android SDK Manager:
├── SDK Platforms
│   └── Android API 33 atau 34 ✅ (pilih salah satu)
└── SDK Tools
    └── Android Emulator
    └── Android SDK Build Tools
    └── Android SDK Platform Tools
```

#### **Step 3: Buat Virtual Device (Emulator)**
```
Tools > Device Manager > Create Device

Isi dengan:
├── Device: "Pixel 6" atau "Pixel 5"
├── System Image: "Android 14" (API 34) atau "Android 13" (API 33)
└── Settings: Default OK
```

#### **Step 4: Jalankan Emulator**
```
Device Manager > Play Button pada device yang baru dibuat
(Tunggu emulator startup ~30-60 detik)
```

#### **Step 5: Build & Run Project**
```
1. Buka project HatchMate-Android di Android Studio
2. Pilih emulator dari dropdown (atas)
3. Click Run button (Shift + F10)
4. Tunggu build & install selesai (~1-2 menit)
```

#### **Step 6: App Terbuka di Emulator!**
```
✅ App sekarang running di emulator
✅ Bisa test semua fitur
✅ Bisa lihat logs di Logcat
```

---

## 🔥 Opsi 2: Testing di HP Fisik (Real Device)

### Keuntungan:
- ⚡ Jauh lebih cepat
- 📱 Lihat real performance
- 🎯 Akurat untuk user experience
- ✅ Bisa test notifikasi real

### Kekurangan:
- 📱 Perlu HP Android
- 🔌 Perlu kabel USB
- ⚙️ Setup developer mode

### Persyaratan HP:
- Android 7.1+ (API 25+) **Recommended: API 31+**
- Storage minimal 100MB
- USB debugging enabled

### Langkah-Langkah:

#### **Step 1: Enable Developer Mode di HP**

**Android 10+** (Standard path):
```
Settings > About Phone > Build Number
└─> Tap 7 kali (sampai muncul "You are now developer")
```

**Lokasi berbeda per HP?**
```
If above not work:
├─ Settings > System > About Phone
├─ Settings > Device Info > Build Number
└─ Settings > More Settings > Developer Options
```

#### **Step 2: Enable USB Debugging**
```
Settings > Developer Options > USB Debugging
└─> Toggle ON ✅
```

#### **Step 3: Connect HP ke Laptop**
```
1. Ambil kabel USB (yang bisa transfer data)
2. Connect HP ke laptop
3. Di HP, pilih "File Transfer" (jangan "Charge Only")
4. Android Studio akan detect HP
```

#### **Step 4: Verify Device Connected**

Buka Terminal/Command Prompt:
```bash
# Windows
adb devices

# Output yang diharapkan:
# List of attached devices
# XXXXXXXX       device
```

Jika belum muncul device:
```bash
# Windows - Cek ADB location
echo %ANDROID_SDK_ROOT%\platform-tools

# macOS/Linux
echo $ANDROID_SDK_ROOT/platform-tools

# Add ke PATH jika belum
```

#### **Step 5: Build & Run ke HP**
```
1. Buka project di Android Studio
2. Pilih HP dari dropdown (atas)
3. Click Run (Shift + F10)
4. Tunggu build & install (~1-2 menit)
```

#### **Step 6: App Terbuka di HP!**
```
✅ HatchMate sekarang running di HP
✅ Performance jauh lebih cepat
✅ Bisa test di setting asli
```

---

## 📋 Quick Command Reference

### Build APK (untuk share/install manual)
```bash
cd HatchMate-Android

# Debug APK
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Release APK (dengan ProGuard)
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release-unsigned.apk
```

### Install APK Manual ke HP
```bash
# Via ADB
adb install app/build/outputs/apk/debug/app-debug.apk

# Via HP langsung
# Copy APK ke HP via USB
# Tap APK file di File Manager
# Install
```

### View Logs saat Testing
```bash
# Real-time logs
adb logcat

# Filter hanya HatchMate logs
adb logcat | grep "HatchMate\|HATCHMATE"

# Save logs to file
adb logcat > hatchmate_logs.txt
```

### Uninstall App dari HP
```bash
# Via ADB
adb uninstall com.hatchmate.app

# Via HP: Settings > Apps > HatchMate > Uninstall
```

---

## 🧪 Testing Scenarios (Setelah App Running)

### **Scenario 1: Test Tab Inkubator**
```
1. Buka app
2. Tap tab "Inkubator"
3. Tap tombol "+" (floating button)
4. Input batch telur:
   - Jenis Unggas: "Ayam"
   - Tanggal Masuk: 31-05-2026
   - Suhu: 37.8
   - Kelembapan: 60
5. Save
✅ Batch telur muncul di list
```

### **Scenario 2: Test Tab AI Analisa**
```
1. Tap tab "AI Analisa"
2. Input hasil tetas:
   - Batch ID: 1
   - Total: 1000 telur
   - Menetas: 900
   - Infertil: 50
   - Gagal: 50
3. Save
✅ Closed-loop trigger:
   - Telur Infertil auto +50 di gudang
   - Efisiensi 100% ditampilkan
```

### **Scenario 3: Test Tab Penjualan**
```
1. Tap tab "Penjualan"
2. Lihat list stok (sekarang ada Telur Infertil)
3. Tap tombol "Jual" untuk Telur Infertil
✅ Closed-loop trigger:
   - Stok berkurang
   - Transaksi tercatat (MASUK)
```

### **Scenario 4: Test Tab Unggas**
```
1. Tap tab "Unggas"
2. Tap tombol "+" untuk add batch unggas
3. Input:
   - Jenis: "Ayam"
   - Jumlah: 900
   - Status: "Sehat"
4. Save
5. Tap tombol "Catat Kematian"
✅ Closed-loop trigger:
   - Jumlah berkurang
   - Stok DOC berkurang
   - Efisiensi AI berkurang
```

---

## 🐛 Troubleshooting Testing

### **Issue 1: Emulator Tidak Mau Start**
```
❌ Error: "QEMU: Cannot allocate memory"
✅ Solution:
   - Close other apps
   - Device Manager > Stop other emulators
   - Atau buka device dengan RAM lebih kecil
```

### **Issue 2: HP Tidak Terdeteksi ADB**
```
❌ Command: adb devices -> empty list
✅ Solution:
   1. Unplug HP
   2. Enable USB Debugging (on HP)
   3. Replug dengan "File Transfer" mode
   4. Di HP: accept connection authorization
   5. adb devices (sekarang muncul)
```

### **Issue 3: App Crash saat Dibuka**
```
❌ App error
✅ Solution:
   1. adb logcat (check error message)
   2. adb uninstall com.hatchmate.app
   3. Clean build: ./gradlew clean
   4. Rebuild: ./gradlew assembleDebug
   5. Run lagi
```

### **Issue 4: Database Error"table already exists"
```
❌ Error: "table master_harga already exists"
✅ Solution:
   1. adb shell rm -r /data/data/com.hatchmate.app/
   2. Atau uninstall app
   3. Reinstall
```

### **Issue 5: Layout Jelek di HP**
```
❌ UI tidak fit di screen
✅ Solution:
   - Normal di Compose (auto-responsive)
   - Check screen size / orientation
   - Test di different devices
```

---

## 📱 Recommended Testing Setup

### **Untuk Development:**
```
✅ Gunakan Emulator (lebih cepat iterate)
├─ Setup 1: Pixel 6 + Android 14 (API 34)
└─ Setup 2: Pixel 4 + Android 13 (API 33)
```

### **Untuk QA/Testing:**
```
✅ Gunakan Real Device (lebih akurat)
├─ Smartphone Android 13+ (API 33+)
└─ Tablet Android 12+ (test different screen sizes)
```

### **Untuk Release:**
```
✅ Test di kedua-duanya
├─ Emulator: multiple configs
├─ Real devices: 2-3 different phones
└─ Cek: Performance, UI, Database integrity
```

---

## 🎯 Tips for Better Testing

### **1. Clear Cache Sebelum Test**
```bash
# Via ADB
adb shell pm clear com.hatchmate.app

# Via HP: Settings > Storage > Cache
```

### **2. Monitor Database di Runtime**
```bash
# Connect ke SQLite database emulator
adb shell
cd /data/data/com.hatchmate.app/databases/
sqlite3 hatchmate-db

# Query data
sqlite> SELECT * FROM master_harga;
sqlite> SELECT * FROM gudang_stok;
```

### **3. Capture Screenshots**
```bash
# Via ADB
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png ~/

# Via HP: Power + Volume Down (3 detik)
```

### **4. Record Video**
```bash
# Via ADB
adb shell screenrecord /sdcard/video.mp4
# Press Ctrl+C to stop
adb pull /sdcard/video.mp4 ~/
```

### **5. Share APK (untuk teman test)**
```bash
# Build APK
./gradlew assembleDebug

# Find APK
app/build/outputs/apk/debug/app-debug.apk

# Share file ini (bisa via email, drive, dll)
# Teman bisa install dengan:
#   - Copy ke HP
#   - Tap file
#   - Install
```

---

## 🚀 Next After Testing Success

1. **Read Full Documentation**
   - ARCHITECTURE.md (understand code)
   - SAMPLE_USAGE.md (more workflows)
   - ROADMAP.md (future features)

2. **Customize untuk Bisnis Anda**
   - Change harga defaults
   - Adjust data validation
   - Add custom business logic

3. **Prepare untuk Production**
   - Build Release APK dengan signature
   - Deploy ke Play Store
   - Setup backend (v1.2+)

---

## 💬 Support

**Stuck?**
- Check [Issues](https://github.com/dhannilow-rgb/HatchMate-Android/issues)
- Read SETUP.md troubleshooting section
- Check [CONTRIBUTING.md](CONTRIBUTING.md) for more help

---

**Happy Testing!** 🎉

**Sekarang Anda bisa mulai explore HatchMate di HP Anda sendiri!**
