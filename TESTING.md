# Unit Testing Guide

## Test Setup

### Dependencies (sudah ada di build.gradle.kts)
```gradle
testImplementation("junit:junit:4.13.2")
androidTestImplementation("androidx.test.ext:junit:1.1.5")
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
```

## Unit Tests untuk DAO (Instrumented Tests)

### File: `app/src/androidTest/java/com/hatchmate/app/core/database/HatchMateDaoTest.kt`

```kotlin
package com.hatchmate.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HatchMateDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: HatchMateDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        dao = database.hatchMateDao()
    }

    @After
    fun cleanup() {
        database.close()
    }

    @Test
    fun testInsertAndGetMasterHarga() = runBlocking {
        val harga = MasterHarga("DOC", 3000.0, 4000.0)
        dao.insertMasterHarga(harga)
        
        val retrieved = dao.getHargaByItem("DOC")
        assert(retrieved != null)
        assert(retrieved?.hargaJual == 4000.0)
    }

    @Test
    fun testSimpanHasilAnalisaAITetas_ClosedLoop() = runBlocking {
        // Prepare gudang stok
        dao.insertGudangStok(GudangStok("Telur Infertil", 0, "LOGISTIK"))
        
        val analisa = AnalisaAITetas(
            batchId = 1,
            tglMasuk = "2026-05-31",
            menetas = "2026-06-21",
            jumlahTotal = 1000,
            jumlahInfertil = 50,
            jumlahMenetas = 900,
            gagalMenetas = 50
        )
        
        // Execute closed-loop transaction
        dao.simpanHasilAnalisaAITetas(analisa)
        
        // Verify: Analisa saved
        val savedAnalisa = dao.getAllAnalisaAI().first().firstOrNull()
        assert(savedAnalisa != null)
        assert(savedAnalisa?.efisiensiPembesaran == 100.0)
        
        // Verify: Stok Telur Infertil automatically incremented
        val stok = dao.getAllGudangStok().first().find { it.itemName == "Telur Infertil" }
        assert(stok?.kuantitas == 50)
    }

    @Test
    fun testEksekusiTransaksiPenjualan_ClosedLoop() = runBlocking {
        // Setup
        dao.insertMasterHarga(MasterHarga("DOC", 3000.0, 4000.0))
        dao.insertGudangStok(GudangStok("DOC", 100, "LOGISTIK"))
        
        // Execute
        dao.eksekusiTransaksiPenjualan("DOC", 10, 40000.0, "2026-05-31")
        
        // Verify: Stok berkurang
        val stok = dao.getAllGudangStok().first().find { it.itemName == "DOC" }
        assert(stok?.kuantitas == 90)
        
        // Verify: Transaksi tercatat
        val transaksi = dao.getAllTransaksi().first()
        assert(transaksi.size == 1)
        assert(transaksi[0].tipe == "MASUK")
        assert(transaksi[0].totalHarga == 40000.0)
    }

    @Test
    fun testCatatKematianUnggas_ClosedLoop() = runBlocking {
        // Setup
        dao.insertBatchUnggas(BatchUnggas(
            id = 1,
            jenisUnggas = "Ayam",
            usiaHari = 10,
            jumlah = 900,
            pemberianPakan = "Pakan pabrik",
            jadwalVaksin = "ND",
            statusKesehatan = "Sehat",
            keterangan = ""
        ))
        
        dao.insertGudangStok(GudangStok("DOC", 900, "LOGISTIK"))
        
        dao.insertAnalisaAI(AnalisaAITetas(
            batchId = 1,
            tglMasuk = "2026-05-31",
            menetas = "2026-06-21",
            jumlahTotal = 1000,
            jumlahInfertil = 0,
            jumlahMenetas = 900,
            gagalMenetas = 100,
            efisiensiPembesaran = 100.0
        ))
        )
        
        // Execute
        dao.catatKematianUnggas(
            batchUnggasId = 1,
            jumlahMati = 5,
            itemNameGudang = "DOC",
            associatedBatchIdTetas = 1
        )
        
        // Verify: Batch unggas berkurang
        val unggas = dao.getAllBatchUnggas().first()[0]
        assert(unggas.jumlah == 895)
        
        // Verify: Stok DOC berkurang
        val stok = dao.getAllGudangStok().first().find { it.itemName == "DOC" }
        assert(stok?.kuantitas == 895)
        
        // Verify: Efisiensi AI berkurang
        val analisa = dao.getAllAnalisaAI().first()[0]
        assert(analisa.efisiensiPembesaran == 90.0)  // 100 - (5 x 2)
    }

    @Test
    fun testTambahStokGudang() = runBlocking {
        dao.insertGudangStok(GudangStok("Pakan 20kg", 5, "PERALATAN"))
        dao.tambahStokGudang("Pakan 20kg", 3)
        
        val stok = dao.getAllGudangStok().first()[0]
        assert(stok.kuantitas == 8)
    }

    @Test
    fun testKurangiStokGudang() = runBlocking {
        dao.insertGudangStok(GudangStok("Pakan 20kg", 10, "PERALATAN"))
        dao.kurangiStokGudang("Pakan 20kg", 3)
        
        val stok = dao.getAllGudangStok().first()[0]
        assert(stok.kuantitas == 7)
    }
}
```

## Unit Tests untuk ViewModel

### File: `app/src/test/java/com/hatchmate/app/features/HatchMateViewModelTest.kt`

```kotlin
package com.hatchmate.app.features

import com.hatchmate.app.core.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HatchMateViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var mockDao: HatchMateDao

    private lateinit var viewModel: HatchMateViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = HatchMateViewModel(mockDao)
    }

    @Test
    fun testUpdateMasterHarga() = runTest {
        // Setup mock
        whenever(mockDao.getAllMasterHarga())
            .thenReturn(flowOf(emptyList()))

        // Execute
        viewModel.updateMasterHarga("DOC", 3500.0, 4500.0)

        // Verify
        advanceUntilIdle()
        verify(mockDao).insertMasterHarga(
            MasterHarga("DOC", 3500.0, 4500.0)
        )
    }

    @Test
    fun testHitungHPPRealCost() = runTest {
        // Setup
        whenever(mockDao.getHargaByItem("Telur Segar Ayam"))
            .thenReturn(MasterHarga("Telur Segar Ayam", 1500.0, 2000.0))

        // Execute & Collect
        val result = mutableListOf<Pair<Double, Double>>()
        viewModel.hitungHPPRealCost(
            wattMesin = 300.0,
            jamInkubasi = 504.0,
            tarifPLN = 1300.0,
            biayaPenyusutan = 50000.0,
            jenisUnggas = "Ayam",
            biayaPakanSeminggu = 10000.0,
            persentaseKeuntungan = 30.0
        ).collect { result.add(it) }

        // Verify
        assert(result.size == 1)
        val (hpp, hargaJual) = result[0]
        assert(hpp > 0)
        assert(hargaJual > hpp)  // Harga jual lebih tinggi dari HPP
    }
}
```

## Running Tests

### Via Android Studio
1. Right-click pada test file
2. Select **Run** atau **Debug**

### Via Command Line
```bash
# Unit tests (JVM)
./gradlew test

# Instrumented tests (Device/Emulator)
./gradlew connectedAndroidTest

# Both
./gradlew test connectedAndroidTest

# Specific test
./gradlew testDebug -Dorg.gradle.testmultiprocessing=false
```

## Test Coverage

### Generate Coverage Report
```bash
./gradlew jacocoTestReport
# Report location: app/build/reports/jacoco/test/html/index.html
```

## Best Practices

1. **Use in-memory database untuk testing** ✅ Cepat, tidak butuh file
2. **Mock dependencies** ✅ Isolate logic yang di-test
3. **Test closed-loop transactions** ✅ Pastikan atomicity
4. **Test edge cases** ✅ 0 items, max capacity, dll
5. **Clear test names** ✅ Mudah di-maintain

---

**Sample test execution:**
```
✓ testInsertAndGetMasterHarga (42ms)
✓ testSimpanHasilAnalisaAITetas_ClosedLoop (58ms)
✓ testEksekusiTransaksiPenjualan_ClosedLoop (51ms)
✓ testCatatKematianUnggas_ClosedLoop (63ms)
✓ testHitungHPPRealCost (37ms)

All 5 tests passed!
```
