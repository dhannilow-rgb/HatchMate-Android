package com.hatchmate.app.core.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HatchMateDao {

    // ========== Master Harga ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMasterHarga(harga: MasterHarga)

    @Query("SELECT * FROM master_harga WHERE itemName = :itemName")
    suspend fun getHargaByItem(itemName: String): MasterHarga?

    @Query("SELECT * FROM master_harga")
    fun getAllMasterHarga(): Flow<List<MasterHarga>>

    // ========== Batch Inkubator ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatchInkubator(batch: BatchInkubator)

    @Query("SELECT * FROM batch_inkubator")
    fun getAllBatchInkubator(): Flow<List<BatchInkubator>>

    @Query("SELECT * FROM batch_inkubator WHERE isActive = 1")
    fun getActiveInkubatorBatches(): Flow<List<BatchInkubator>>

    @Query("UPDATE batch_inkubator SET isActive = 0 WHERE id = :id")
    suspend fun deactivateBatch(id: Int)

    // ========== Batch Unggas ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatchUnggas(batch: BatchUnggas)

    @Query("SELECT * FROM batch_unggas")
    fun getAllBatchUnggas(): Flow<List<BatchUnggas>>

    // ========== Gudang Stok ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGudangStok(stok: GudangStok)

    @Query("SELECT * FROM gudang_stok")
    fun getAllGudangStok(): Flow<List<GudangStok>>

    @Query("UPDATE gudang_stok SET kuantitas = kuantitas + :tambah WHERE itemName = :itemName")
    suspend fun tambahStokGudang(itemName: String, tambah: Int)

    @Query("UPDATE gudang_stok SET kuantitas = kuantitas - :kurang WHERE itemName = :itemName")
    suspend fun kurangiStokGudang(itemName: String, kurang: Int)

    // ========== Transaksi Keuangan ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaksiKeuangan(transaksi: TransaksiKeuangan)

    @Query("SELECT * FROM transaksi_keuangan")
    fun getAllTransaksi(): Flow<List<TransaksiKeuangan>>

    // ========== Analisa AI Tetas ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalisaAI(analisa: AnalisaAITetas)

    @Query("SELECT * FROM analisa_ai_tetas")
    fun getAllAnalisaAI(): Flow<List<AnalisaAITetas>>

    // ========== CLOSED-LOOP TRANSACTION LOGIC ==========

    @Transaction
    suspend fun simpanHasilAnalisaAITetas(analisa: AnalisaAITetas) {
        insertAnalisaAI(analisa)
        tambahStokGudang("Telur Infertil", analisa.jumlahInfertil)
    }

    @Transaction
    suspend fun eksekusiTransaksiPenjualan(itemName: String, jumlahJual: Int, totalHarga: Double, tanggal: String) {
        kurangiStokGudang(itemName, jumlahJual)
        insertTransaksiKeuangan(
            TransaksiKeuangan(tipe = "MASUK", itemName = itemName, jumlah = jumlahJual, totalHarga = totalHarga, tanggal = tanggal)
        )
    }

    @Transaction
    suspend fun catatKematianUnggas(batchUnggasId: Int, jumlahMati: Int, itemNameGudang: String, associatedBatchIdTetas: Int) {
        kurangiJumlahUnggasDirect(batchUnggasId, jumlahMati)
        kurangiStokGudang(itemNameGudang, jumlahMati)
        updatePenurunanEfisiensiAI(associatedBatchIdTetas, jumlahMati * 2.0)
    }

    @Query("UPDATE batch_unggas SET jumlah = jumlah - :jumlahMati WHERE id = :id")
    suspend fun kurangiJumlahUnggasDirect(id: Int, jumlahMati: Int)

    @Query("UPDATE analisa_ai_tetas SET efisiensiPembesaran = efisiensiPembesaran - :penurunan WHERE batchId = :batchId")
    suspend fun updatePenurunanEfisiensiAI(batchId: Int, penurunan: Double)
}
