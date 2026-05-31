# Sample Usage & Common Workflows

## Workflow 1: Setup Awal Aplikasi (Hari Pertama)

### Langkah 1: Input Master Harga
```kotlin
// MainActivity atau dalam di setup screen
private suspend fun setupMasterData() {
    val dao = database.hatchMateDao()
    
    val prices = listOf(
        MasterHarga("Telur Segar Ayam", 1500.0, 2000.0),
        MasterHarga("Telur Segar Bebek", 2000.0, 2500.0),
        MasterHarga("DOC", 3000.0, 4000.0),
        MasterHarga("DOD", 4000.0, 5500.0),
        MasterHarga("Pakan 20kg", 80000.0, 95000.0),
        MasterHarga("Telur Infertil", 0.0, 500.0)
    )
    
    prices.forEach { dao.insertMasterHarga(it) }
}
```

### Langkah 2: Inisialisasi Gudang Stok
```kotlin
private suspend fun setupGudang() {
    val dao = database.hatchMateDao()
    
    val stokItems = listOf(
        GudangStok("DOC", 0, "LOGISTIK"),
        GudangStok("DOD", 0, "LOGISTIK"),
        GudangStok("Telur Infertil", 0, "LOGISTIK"),
        GudangStok("Pakan 20kg", 10, "PERALATAN")
    )
    
    stokItems.forEach { dao.insertGudangStok(it) }
}
```

---

## Workflow 2: Siklus Lengkap Penetasan Telur

### Hari 1: Input Batch Telur
```kotlin
// Di ViewModel
fun createBatchInkubator() {
    val batch = BatchInkubator(
        id = 0,  // auto-generate jadi 1
        tanggalMasuk = "2026-05-31",
        mulaiPutar = "2026-06-01",
        teropong = "2026-06-10",
        stopPutar = "2026-06-20",
        tanggalMenetas = "2026-06-21",
        jenisUnggas = "Ayam",
        suhu = 37.8,
        kelembapan = 60.0,
        perlakuanTambahan = "Spray 2x sehari dari hari 10",
        isActive = true
    )
    
    inputBatchInkubator(batch)
}

// Di ViewModel
fun inputBatchInkubator(batch: BatchInkubator) {
    viewModelScope.launch {
        dao.insertBatchInkubator(batch)
    }
}
```

### Hari 10: Candling (Teropong)
```kotlin
// Peternak cek perkembangan telur di tab Inkubator
// UI hanya menampilkan info, tidak ada aksi database khusus
Text("Teropong dilakukan pada: ${batch.teropong}")
```

### Hari 21: Hasil Penetasan & Closed-Loop Trigger #1
```kotlin
// Peternak membuka Tab "AI Analisa" dan input hasil
fun inputHasilTetas() {
    val analisa = AnalisaAITetas(
        batchId = 1,
        tglMasuk = "2026-05-31",
        menetas = "2026-06-21",
        jumlahTotal = 1000,
        jumlahInfertil = 50,    // Telur tidak subur
        jumlahMenetas = 900,    // Sukses tetas
        gagalMenetas = 50,      // Busuk/mati dalam telur
        efisiensiPembesaran = 100.0
    )
    
    viewModel.inputAnalisaAITetas(analisa)
    // ✅ OTOMATIS:
    // - Simpan analisa
    // - Gudang "Telur Infertil" +50
    // - Transaksi atomik (semua sukses atau semua gagal)
}
```

**Database State:**
```sql
-- analisa_ai_tetas
INSERT INTO analisa_ai_tetas 
VALUES (1, '2026-05-31', '2026-06-21', 1000, 50, 900, 50, 100.0);

-- gudang_stok (OTOMATIS)
UPDATE gudang_stok 
SET kuantitas = 50 
WHERE itemName = 'Telur Infertil';
```

---

## Workflow 3: Pembesaran DOC (Anak Ayam)

### Input Batch Unggas (dari hasil tetas)
```kotlin
fun createBatchUnggas() {
    val batch = BatchUnggas(
        id = 0,  // auto-generate jadi 1
        jenisUnggas = "Ayam",
        usiaHari = 0,
        jumlah = 900,  // dari jumlahMenetas
        pemberianPakan = "Pakan pabrik full ad libitum",
        jadwalVaksin = "ND hari 4, AI hari 7",
        statusKesehatan = "Sehat",
        keterangan = "Dari batch tetas #1"
    )
    
    viewModel.inputBatchUnggas(batch)
}
```

### Input Gudang: DOC Terisi dari Hasil Tetas
```kotlin
// CATATAN: Ini bukan automated dari app, tapi peternak manual input
// atau bisa automated di backend jika ada integration
val docStok = GudangStok(
    itemName = "DOC",
    kuantitas = 900,  // Jumlah yang menetas
    kategori = "LOGISTIK"
)

viewModel.inputGudangStok(docStok)
```

### Hari 10: Pencatatan Kematian (Closed-Loop Trigger #3)
```kotlin
// Di tab Unggas, peternak lihat batch dan ada tombol "Catat Kematian"
fun logMortalityEvent() {
    viewModel.inputKematianUnggas(
        batchUnggasId = 1,     // Batch unggas #1
        jumlahMati = 5,        // 5 ekor mati
        jenisUnggas = "Ayam",  // Untuk determine DOC vs DOD
        batchTetasId = 1       // Batch tetas yang menghasilkan unggas ini
    )
    // ✅ OTOMATIS:
    // 1. batch_unggas #1: 900 → 895
    // 2. gudang_stok "DOC": 900 → 895
    // 3. analisa_ai_tetas #1: efisiensi 100% → 90% (5 x 2%)
}
```

**Database State After:**
```sql
-- batch_unggas
UPDATE batch_unggas 
SET jumlah = 895 
WHERE id = 1;

-- gudang_stok
UPDATE gudang_stok 
SET kuantitas = 895 
WHERE itemName = 'DOC';

-- analisa_ai_tetas
UPDATE analisa_ai_tetas 
SET efisiensiPembesaran = 90.0 
WHERE batchId = 1;
```

---

## Workflow 4: Penjualan Retail (Closed-Loop Trigger #2)

### Skenario: Peternak menjual 100 DOC ke pembeli
```kotlin
fun processSale() {
    // Tanggal hari ini
    val today = LocalDate.now().toString()  // "2026-05-31"
    
    viewModel.prosesPenjualanDigital(
        itemName = "DOC",
        kuantitas = 100,
        tanggal = today
    )
    // ✅ OTOMATIS:
    // 1. Ambil harga dari master_harga: Rp 4.000/ekor
    // 2. Potong stok: DOC 895 → 795
    // 3. Catat transaksi masuk: Rp 400.000
}
```

**Database State After:**
```sql
-- gudang_stok
UPDATE gudang_stok 
SET kuantitas = 795 
WHERE itemName = 'DOC';

-- transaksi_keuangan (OTOMATIS)
INSERT INTO transaksi_keuangan 
VALUES (
    1,                   -- id auto-generate
    'MASUK',            -- tipe
    'DOC',              -- itemName
    100,                -- jumlah
    400000.0,           -- totalHarga (100 x 4000)
    '2026-05-31'        -- tanggal
);
```

### Flow Kasir di UI
```kotlin
@Composable
fun CheckoutScreen(viewModel: HatchMateViewModel) {
    val stok by viewModel.gudangStokList.collectAsState()
    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    
    Column {
        // 1. Tampilkan items dari gudang
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(stok) { item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.itemName)
                    Text("Stok: ${item.kuantitas}")
                    
                    // Tombol +1 ke keranjang
                    Button(onClick = {
                        cartItems = cartItems + CartItem(item.itemName, 1)
                    }) {
                        Text("+")
                    }
                }
            }
        }
        
        Divider()
        
        // 2. Tampilkan keranjang
        cartItems.forEach { item ->
            Text("${item.name} x${item.qty}")
        }
        
        // 3. Tombol checkout
        Button(
            onClick = {
                cartItems.forEach { item ->
                    viewModel.prosesPenjualanDigital(
                        itemName = item.name,
                        kuantitas = item.qty,
                        tanggal = LocalDate.now().toString()
                    )
                }
                cartItems = emptyList()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("CHECKOUT")
        }
    }
}

data class CartItem(val name: String, val qty: Int)
```

---

## Workflow 5: Laporan Penjualan Harian

### Tampilkan Transaksi Hari Ini
```kotlin
@Composable
fun DailySalesReport(viewModel: HatchMateViewModel) {
    val allTransaksi by viewModel.riwayatTransaksi.collectAsState()
    val today = LocalDate.now().toString()
    
    // Filter hanya transaksi hari ini yang tipe MASUK
    val todaysSales = allTransaksi.filter { 
        it.tanggal == today && it.tipe == "MASUK" 
    }
    
    val totalPendapatan = todaysSales.sumOf { it.totalHarga }
    
    Column {
        Text("LAPORAN PENJUALAN - $today", style = MaterialTheme.typography.titleLarge)
        Divider()
        
        todaysSales.forEach { transaksi ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(transaksi.itemName)
                Text("${transaksi.jumlah}x")
                Text("Rp ${transaksi.totalHarga}")
            }
        }
        
        Divider()
        Text(
            "TOTAL: Rp $totalPendapatan",
            style = MaterialTheme.typography.titleMedium
        )
    }
}
```

---

## Workflow 6: Update Harga Master (Dynamic Pricing)

### Skenario: Harga DOC naik karena supply terbatas
```kotlin
fun updatePricing() {
    viewModel.updateMasterHarga(
        itemName = "DOC",
        hargaBeli = 3500.0,   // Naik dari 3000
        hargaJual = 4500.0    // Naik dari 4000
    )
    // ✅ EFEK: Semua penjualan DOC setelah ini pakai harga baru
}
```

**Impact:**
- Transaksi penjualan lama tetap pakai harga lama (history)
- Penjualan baru otomatis pakai harga baru
- HPP calculator otomatis adjust

---

## Workflow 7: Kalkulasi HPP untuk Batch Baru

### Contoh: HPP untuk batch telur ayam 21 hari
```kotlin
fun calculateNewBatchHPP() {
    viewModel.hitungHPPRealCost(
        wattMesin = 300.0,           // 300 watt inkubator
        jamInkubasi = 504.0,         // 21 hari x 24 jam
        tarifPLN = 1300.0,           // Rp per kWh
        biayaPenyusutan = 50000.0,   // Depresiasi mesin per batch
        jenisUnggas = "Ayam",
        biayaPakanSeminggu = 10000.0,// Pakan indukan 1 minggu
        persentaseKeuntungan = 35.0  // Target margin 35%
    ).collectLatest { (hpp, hargaJual) ->
        println("Biaya Pokok HPP: Rp $hpp")
        println("Rekomendasi Harga Jual: Rp $hargaJual")
        // Output: HPP: Rp 1600, Harga Jual: Rp 2160
    }
}
```

**Breakdown HPP:**
```
Telur Segar Ayam (dari master)    : Rp 1.500
Biaya Listrik (300W x 504h)       : Rp 196
Biaya Penyusutan Mesin            : Rp 50
Biaya Pakan Indukan               : Rp 10
─────────────────────────────────────────
TOTAL HPP                         : Rp 1.756

Keuntungan 35%                    : Rp 614
─────────────────────────────────────────
Harga Jual (dibulatkan ke atas)   : Rp 2.171
```

---

## Workflow 8: Query Data Advanced

### Lihat Efisiensi Semua Batch
```kotlin
@Composable
fun EfficiencyOverview(viewModel: HatchMateViewModel) {
    val aiData by viewModel.analisaAIList.collectAsState()
    
    val avgEfficiency = if (aiData.isNotEmpty()) {
        aiData.map { it.efisiensiPembesaran }.average()
    } else 0.0
    
    Column {
        Text("Rata-rata Efisiensi: ${String.format("%.1f", avgEfficiency)}%")
        
        // Batch dengan efisiensi paling rendah
        val worstBatch = aiData.minByOrNull { it.efisiensiPembesaran }
        worstBatch?.let {
            Card(colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))) {
                Text("⚠ Batch #${it.batchId}: ${it.efisiensiPembesaran}% (Perlu Analisa)")
            }
        }
    }
}
```

---

## Error Handling Examples

### Jangan Double-Click Checkout
```kotlin
var isProcessing by remember { mutableStateOf(false) }

Button(
    onClick = {
        if (!isProcessing) {
            isProcessing = true
            viewModel.prosesPenjualanDigital(itemName, qty, date)
            // Setelah operasi database selesai
            isProcessing = false
        }
    },
    enabled = !isProcessing
) {
    Text(if (isProcessing) "Processing..." else "Checkout")
}
```

### Handle Stok Tidak Cukup
```kotlin
fun prosesPenjualanWithValidation(itemName: String, qty: Int) {
    val currentStok = gudangStokList.value.find { it.itemName == itemName }?.kuantitas ?: 0
    
    if (qty > currentStok) {
        showErrorDialog("Stok ${itemName} hanya ${currentStok} unit!")
    } else {
        viewModel.prosesPenjualanDigital(itemName, qty, today)
    }
}
```

---

## Tips & Best Practices

1. **Always use Transactions untuk multi-step ops** ✅ Closed-loop logic
2. **Validate input sebelum save** ✅ Cegah data invalid
3. **Use StateFlow untuk reactivity** ✅ Automatic UI updates
4. **Batch database operations** ✅ Lebih efisien
5. **Log transaksi penting** ✅ Audit trail

---

**Need more examples?** Check the source code di `/app/src/main/java`
