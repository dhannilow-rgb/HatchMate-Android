# HatchMate - API & Integration Guide

## Memahami Closed-Loop System

HatchMate mengimplementasikan **Closed-Loop Architecture** di mana setiap data point saling terhubung:

```
┌──────────────────────────────────────────────────────────┐
│ Aliran Dana (Master Harga)                               │
│ SSOT untuk semua harga transaksi                         │
└────────────┬────────────────────────────┬────────────────┘
             │                            │
      ┌──────▼──────┐             ┌──────▼──────────┐
      │ Analisa AI  │             │ Penjualan       │
      │ Hasil Tetas │◄────────────│ Retail/Grosir   │
      └──────┬──────┘             │ Auto-log Stok   │
             │                    └─────────────────┘
      ┌──────▼──────────────┐
      │ Gudang Stok         │
      │ Real-time Updates   │
      └──────┬──────────────┘
             │
      ┌──────▼──────────────┐
      │ Log Kematian Unggas │
      │ Auto-deduct Efisiensi
      └─────────────────────┘
```

### Konsep: SSOT (Single Source of Truth)
Semua data di HatchMate berpusat pada **Master Harga**:
- Ketika harga updated, semua transaksi & perhitungan HPP otomatis konsisten
- Tidak ada redundancy atau data conflict

## API Reference

### Database Layer (DAO)

#### 1. Master Harga Operations

```kotlin
// Insert/Update Master Harga
suspend fun insertMasterHarga(harga: MasterHarga)

// Ambil harga item spesifik
suspend fun getHargaByItem(itemName: String): MasterHarga?

// Ambil semua master harga (reactive)
fun getAllMasterHarga(): Flow<List<MasterHarga>>
```

**Contoh Usage:**
```kotlin
val dao = database.hatchMateDao()

// Update harga DOC
dao.insertMasterHarga(MasterHarga("DOC", 3500.0, 4200.0))

// Ambil harga di ViewModel
val masterList = dao.getAllMasterHarga().collectAsState(initial = emptyList())
```

#### 2. Batch Inkubator Operations

```kotlin
suspend fun insertBatchInkubator(batch: BatchInkubator)
fun getAllBatchInkubator(): Flow<List<BatchInkubator>>
fun getActiveInkubatorBatches(): Flow<List<BatchInkubator>>
suspend fun deactivateBatch(id: Int)
```

**Contoh:**
```kotlin
val batch = BatchInkubator(
    id = 0,  // auto-generate
    tanggalMasuk = "2026-05-31",
    mulaiPutar = "2026-06-01",
    teropong = "2026-06-10",
    stopPutar = "2026-06-20",
    tanggalMenetas = "2026-06-21",
    jenisUnggas = "Ayam",
    suhu = 37.8,
    kelembapan = 60.0,
    perlakuanTambahan = "Spray harian",
    isActive = true
)

dao.insertBatchInkubator(batch)
```

#### 3. Gudang Stok Operations

```kotlin
// Tambah stok
suspend fun tambahStokGudang(itemName: String, tambah: Int)

// Kurangi stok
suspend fun kurangiStokGudang(itemName: String, kurang: Int)

// Lihat semua stok (reactive)
fun getAllGudangStok(): Flow<List<GudangStok>>
```

**Contoh:**
```kotlin
// Stok DOC bertambah 50 dari hasil tetas
dao.tambahStokGudang("DOC", 50)

// Stok kurangi karena penjualan
dao.kurangiStokGudang("DOC", 10)
```

#### 4. Transaksi Keuangan Operations

```kotlin
suspend fun insertTransaksiKeuangan(transaksi: TransaksiKeuangan)
fun getAllTransaksi(): Flow<List<TransaksiKeuangan>>
```

**Contoh:**
```kotlin
val transaksi = TransaksiKeuangan(
    id = 0,  // auto-generate
    tipe = "MASUK",  // atau "KELUAR"
    itemName = "DOC",
    jumlah = 50,
    totalHarga = 200000.0,
    tanggal = "2026-05-31"
)

dao.insertTransaksiKeuangan(transaksi)
```

#### 5. Analisa AI Tetas Operations

```kotlin
suspend fun insertAnalisaAI(analisa: AnalisaAITetas)
fun getAllAnalisaAI(): Flow<List<AnalisaAITetas>>
```

### ViewModel Layer (Business Logic)

#### HatchMateViewModel Public Methods

```kotlin
class HatchMateViewModel(private val dao: HatchMateDao) : ViewModel()
```

##### 1. Kalkulasi HPP Real Cost

```kotlin
fun hitungHPPRealCost(
    wattMesin: Double,
    jamInkubasi: Double,
    tarifPLN: Double = 1300.0,
    biayaPenyusutan: Double,
    jenisUnggas: String,
    biayaPakanSeminggu: Double,
    persentaseKeuntungan: Double
): Flow<Pair<Double, Double>>
```

**Contoh:**
```kotlin
viewModel.hitungHPPRealCost(
    wattMesin = 300.0,
    jamInkubasi = 504.0,  // 21 hari × 24 jam
    tarifPLN = 1300.0,    // per kWh
    biayaPenyusutan = 50000.0,
    jenisUnggas = "Ayam",
    biayaPakanSeminggu = 10000.0,
    persentaseKeuntungan = 30.0
).collectLatest { (hpp, hargaJual) ->
    println("HPP: Rp $hpp")
    println("Harga Jual Rekomendasi: Rp $hargaJual")
}
// Output: HPP: Rp 1500, Harga Jual: Rp 1950
```

##### 2. Input Analisa AI Tetas (Closed-Loop Trigger #1)

```kotlin
fun inputAnalisaAITetas(analisa: AnalisaAITetas)
```

**Apa yang terjadi:**
- ✅ Simpan hasil tetas ke `analisa_ai_tetas`
- ✅ **OTOMATIS** tambah stok gudang "Telur Infertil"
- ✅ Atomik: kedua operasi succeed atau keduanya fail

**Contoh:**
```kotlin
val analisa = AnalisaAITetas(
    batchId = 1,
    tglMasuk = "2026-05-31",
    menetas = "2026-06-21",
    jumlahTotal = 1000,
    jumlahInfertil = 50,
    jumlahMenetas = 900,
    gagalMenetas = 50,
    efisiensiPembesaran = 100.0
)

viewModel.inputAnalisaAITetas(analisa)
// Gudang "Telur Infertil" otomatis +50
```

##### 3. Proses Penjualan Digital (Closed-Loop Trigger #2)

```kotlin
fun prosesPenjualanDigital(itemName: String, kuantitas: Int, tanggal: String)
```

**Apa yang terjadi:**
- ✅ Ambil harga dari Master Harga
- ✅ Potong stok gudang sesuai kuantitas
- ✅ Catat transaksi masuk otomatis
- ✅ Atomik: ketiga operasi terjamin konsisten

**Contoh:**
```kotlin
viewModel.prosesPenjualanDigital(
    itemName = "DOC",
    kuantitas = 100,
    tanggal = "2026-05-31"
)
// Stok DOC -100, Transaksi masuk Rp 400.000 tercatat otomatis
```

##### 4. Input Kematian Unggas (Closed-Loop Trigger #3)

```kotlin
fun inputKematianUnggas(
    batchUnggasId: Int,
    jumlahMati: Int,
    jenisUnggas: String,
    batchTetasId: Int
)
```

**Apa yang terjadi:**
- ✅ Kurangi jumlah di batch_unggas
- ✅ Kurangi stok gudang DOC/DOD
- ✅ Penurunan efisiensi AI (-jumlahMati × 2%)
- ✅ Atomik: semua terjadi bersama atau tidak sama sekali

**Contoh:**
```kotlin
viewModel.inputKematianUnggas(
    batchUnggasId = 1,
    jumlahMati = 5,
    jenisUnggas = "Ayam",
    batchTetasId = 1
)
// Batch unggas #1: 900 → 895
// Stok DOC: 100 → 95
// Efisiensi batch tetas #1: 100% → 90%
```

##### 5. Update Master Harga

```kotlin
fun updateMasterHarga(itemName: String, hargaBeli: Double, hargaJual: Double)
```

**Contoh:**
```kotlin
viewModel.updateMasterHarga(
    itemName = "DOC",
    hargaBeli = 3500.0,
    hargaJual = 4200.0
)
// Efek: Semua transaksi penjualan DOC mulai sekarang akan pakai harga baru
```

##### 6. Input Data Master

```kotlin
fun inputBatchInkubator(batch: BatchInkubator)
fun inputBatchUnggas(batch: BatchUnggas)
fun inputGudangStok(stok: GudangStok)
```

### StateFlow Observers

ViewModel expose reactive data streams:

```kotlin
val masterHargaList: StateFlow<List<MasterHarga>>
val inkubatorList: StateFlow<List<BatchInkubator>>
val unggasList: StateFlow<List<BatchUnggas>>
val gudangStokList: StateFlow<List<GudangStok>>
val riwayatTransaksi: StateFlow<List<TransaksiKeuangan>>
val analisaAIList: StateFlow<List<AnalisaAITetas>>
```

**Contoh di Compose:**
```kotlin
@Composable
fun MyScreen(viewModel: HatchMateViewModel) {
    val stokList by viewModel.gudangStokList.collectAsState()
    
    LazyColumn {
        items(stokList) { item ->
            Text("${item.itemName}: ${item.kuantitas} unit")
        }
    }
}
```

## Integration Examples

### Contoh 1: Dashboard Real-time Stok

```kotlin
@Composable
fun StokDashboard(viewModel: HatchMateViewModel) {
    val stok by viewModel.gudangStokList.collectAsState()
    val transaksi by viewModel.riwayatTransaksi.collectAsState()
    
    Column {
        Text("INVENTORI REAL-TIME")
        stok.forEach { item ->
            Row {
                Text(item.itemName)
                Text(item.kuantitas.toString())
            }
        }
        
        Divider()
        
        Text("TRANSAKSI HARI INI")
        transaksi
            .filter { it.tanggal == LocalDate.now().toString() }
            .forEach { t ->
                Text("${t.tipe}: ${t.itemName} x${t.jumlah} = Rp${t.totalHarga}")
            }
    }
}
```

### Contoh 2: Proses Penjualan Lengkap

```kotlin
@Composable
fun KasirScreen(viewModel: HatchMateViewModel) {
    var selectedItem by remember { mutableStateOf<GudangStok?>(null) }
    var kuantitas by remember { mutableStateOf("1") }
    
    val stok by viewModel.gudangStokList.collectAsState()
    val harga by viewModel.masterHargaList.collectAsState()
    
    Column {
        // 1. Pilih item dari gudang
        LazyColumn {
            items(stok) { item ->
                Button(onClick = { selectedItem = item }) {
                    Text("${item.itemName} (${item.kuantitas}) - Rp ${harga.find { it.itemName == item.itemName }?.hargaJual}")
                }
            }
        }
        
        // 2. Input kuantitas
        TextField(
            value = kuantitas,
            onValueChange = { kuantitas = it },
            label = { Text("Jumlah") }
        )
        
        // 3. Tombol Bayar
        Button(onClick = {
            selectedItem?.let { item ->
                viewModel.prosesPenjualanDigital(
                    itemName = item.itemName,
                    kuantitas = kuantitas.toIntOrNull() ?: 1,
                    tanggal = LocalDate.now().toString()
                )
                // Reset form
                selectedItem = null
                kuantitas = "1"
            }
        }) {
            Text("PROSES PENJUALAN")
        }
    }
}
```

### Contoh 3: Monitoring Efisiensi Tetas

```kotlin
@Composable
fun EfisiensiChart(viewModel: HatchMateViewModel) {
    val aiData by viewModel.analisaAIList.collectAsState()
    
    Column {
        Text("EFISIENSI PEMBESARAN BATCH")
        aiData.sortedByDescending { it.batchId }.forEach { batch ->
            Row {
                Text("Batch ${batch.batchId}")
                val efisiensiColor = when {
                    batch.efisiensiPembesaran >= 95 -> Color.Green
                    batch.efisiensiPembesaran >= 80 -> Color.Yellow
                    else -> Color.Red
                }
                Text(
                    "${batch.efisiensiPembesaran}%",
                    color = efisiensiColor
                )
                
                // Tampilkan rekomendasi jika ada masalah
                if (batch.gagalMenetas > batch.jumlahTotal * 0.15) {
                    Text(
                        "⚠ Perhatian: Tingkat kegagalan tinggi",
                        color = Color.Red
                    )
                }
            }
        }
    }
}
```

## Data Validation Rules

### Master Harga
- ✅ `hargaBeli` harus ≤ `hargaJual`
- ✅ `itemName` unik (primary key)
- ⚠️ Harga tidak boleh negatif

### Batch Inkubator
- ✅ `tanggalMasuk` < `tanggalMenetas`
- ✅ `mulaiPutar` ≤ `teropong` ≤ `stopPutar`
- ⚠️ Suhu optimal: 37.5-38.5°C
- ⚠️ Kelembapan optimal: 55-75%

### Gudang Stok
- ✅ `kuantitas` tidak boleh negatif (enforcement: jangan biarkan -stock)
- ✅ `itemName` unik
- ⚠️ Kategori: "LOGISTIK" atau "PERALATAN"

## Error Handling

Semua operasi di ViewModel aman untuk exception handling:

```kotlin
viewModel.inputAnalisaAITetas(analisa)
// Jika gagal, tidak ada perubahan data (transaction rollback)
```

## Performance Considerations

1. **Batch Operations**: Untuk multiple inserts, gunakan loop dalam transaction
2. **Flow Collection**: `collectAsState()` di Compose otomatis handle lifecycle
3. **Database Queries**: Semua indexed untuk fast lookup

---

**More examples?** Check [Sample Usage](./SAMPLE_USAGE.md)
