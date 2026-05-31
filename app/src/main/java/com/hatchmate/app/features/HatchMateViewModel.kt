package com.hatchmate.app.features

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hatchmate.app.core.database.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.ceil

class HatchMateViewModel(private val dao: HatchMateDao) : ViewModel() {

    val masterHargaList = dao.getAllMasterHarga().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val inkubatorList = dao.getAllBatchInkubator().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val unggasList = dao.getAllBatchUnggas().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val gudangStokList = dao.getAllGudangStok().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val riwayatTransaksi = dao.getAllTransaksi().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val analisaAIList = dao.getAllAnalisaAI().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun hitungHPPRealCost(
        wattMesin: Double,
        jamInkubasi: Double,
        tarifPLN: Double = 1300.0,
        biayaPenyusutan: Double,
        jenisUnggas: String,
        biayaPakanSeminggu: Double,
        persentaseKeuntungan: Double
    ): Flow<Pair<Double, Double>> = flow {
        val hargaTelurMaster = dao.getHargaByItem("Telur Segar $jenisUnggas")?.hargaBeli ?: 0.0
        val biayaListrik = (wattMesin / 1000.0) * jamInkubasi * tarifPLN
        val hppModalBase = hargaTelurMaster + biayaListrik + biayaPenyusutan + biayaPakanSeminggu
        val hargaJualRekomendasi = ceil(hppModalBase + (hppModalBase * (persentaseKeuntungan / 100.0)))
        
        emit(Pair(hppModalBase, hargaJualRekomendasi))
    }

    fun inputAnalisaAITetas(analisa: AnalisaAITetas) {
        viewModelScope.launch {
            dao.simpanHasilAnalisaAITetas(analisa)
        }
    }

    fun prosesPenjualanDigital(itemName: String, kuantitas: Int, tanggal: String) {
        viewModelScope.launch {
            val hargaMaster = dao.getHargaByItem(itemName)?.hargaJual ?: 0.0
            val totalHarga = hargaMaster * kuantitas
            dao.eksekusiTransaksiPenjualan(itemName, kuantitas, totalHarga, tanggal)
        }
    }

    fun inputKematianUnggas(batchUnggasId: Int, jumlahMati: Int, jenisUnggas: String, batchTetasId: Int) {
        viewModelScope.launch {
            val itemNameGudang = if (jenisUnggas.contains("Bebek", true)) "DOD" else "DOC"
            dao.catatKematianUnggas(batchUnggasId, jumlahMati, itemNameGudang, batchTetasId)
        }
    }
    
    fun updateMasterHarga(itemName: String, hargaBeli: Double, hargaJual: Double) {
        viewModelScope.launch {
            dao.insertMasterHarga(MasterHarga(itemName, hargaBeli, hargaJual))
        }
    }

    fun inputBatchInkubator(batch: BatchInkubator) {
        viewModelScope.launch {
            dao.insertBatchInkubator(batch)
        }
    }

    fun inputBatchUnggas(batch: BatchUnggas) {
        viewModelScope.launch {
            dao.insertBatchUnggas(batch)
        }
    }

    fun inputGudangStok(stok: GudangStok) {
        viewModelScope.launch {
            dao.insertGudangStok(stok)
        }
    }
}
