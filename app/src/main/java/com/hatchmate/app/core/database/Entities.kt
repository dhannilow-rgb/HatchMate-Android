package com.hatchmate.app.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// 1. MODUL MASTER: Aliran Dana / Master Price Controller
@Entity(tableName = "master_harga")
data class MasterHarga(
    @PrimaryKey val itemName: String,
    val hargaBeli: Double,
    val hargaJual: Double
)

// 2. TAB INKUBATOR: Manajemen Siklus Inkubasi
@Entity(tableName = "batch_inkubator")
data class BatchInkubator(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tanggalMasuk: String,
    val mulaiPutar: String,
    val teropong: String,
    val stopPutar: String,
    val tanggalMenetas: String,
    val jenisUnggas: String,
    val suhu: Double,
    val kelembapan: Double,
    val perlakuanTambahan: String,
    val isActive: Boolean = true
)

// 3. TAB UNGGAS: Manajemen Pembesaran & Monitoring
@Entity(tableName = "batch_unggas")
data class BatchUnggas(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val jenisUnggas: String,
    val usiaHari: Int,
    val jumlah: Int,
    val pemberianPakan: String,
    val jadwalVaksin: String,
    val statusKesehatan: String,
    val keterangan: String
)

// 4. MODUL GUDANG: Inventaris Stok Real-time
@Entity(tableName = "gudang_stok")
data class GudangStok(
    @PrimaryKey val itemName: String,
    val kuantitas: Int,
    val kategori: String
)

// 5. MODUL TRANSAKSI: Catatan Keuangan Masuk/Keluar
@Entity(tableName = "transaksi_keuangan")
data class TransaksiKeuangan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tipe: String,
    val itemName: String,
    val jumlah: Int,
    val totalHarga: Double,
    val tanggal: String
)

// 6. MODUL PEMICU: Analisa AI Hasil Penetasan
@Entity(tableName = "analisa_ai_tetas")
data class AnalisaAITetas(
    @PrimaryKey val batchId: Int,
    val tglMasuk: String,
    val menetas: String,
    val jumlahTotal: Int,
    val jumlahInfertil: Int,
    val jumlahMenetas: Int,
    val gagalMenetas: Int,
    val efisiensiPembesaran: Double = 100.0
)
