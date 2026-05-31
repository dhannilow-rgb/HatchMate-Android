package com.hatchmate.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hatchmate.app.features.HatchMateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(viewModel: HatchMateViewModel) {
    var currentTab by remember { mutableStateOf("INKUBATOR") }
    
    Scaffold(
        topBar = { 
            TopAppBar(title = { Text("HatchMate Pro ERP", fontSize = 20.sp) }) 
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == "INKUBATOR",
                    onClick = { currentTab = "INKUBATOR" },
                    icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                    label = { Text("Inkubator") }
                )
                NavigationBarItem(
                    selected = currentTab == "UNGGAS",
                    onClick = { currentTab = "UNGGAS" },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Unggas") }
                )
                NavigationBarItem(
                    selected = currentTab == "ANALISA_AI",
                    onClick = { currentTab = "ANALISA_AI" },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                    label = { Text("AI Analisa") }
                )
                NavigationBarItem(
                    selected = currentTab == "PENJUALAN",
                    onClick = { currentTab = "PENJUALAN" },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                    label = { Text("Penjualan") }
                )
            }
        },
        floatingActionButton = {
            if (currentTab == "INKUBATOR") {
                FloatingActionButton(onClick = { /* Trigger Dialog Input Batch Telur */ }) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Batch Telur")
                }
            } else if (currentTab == "UNGGAS") {
                FloatingActionButton(onClick = { /* Trigger Dialog Input Unggas Baru */ }) {
                    Icon(Icons.Default.AccountBox, contentDescription = "Tambah Anak Ayam")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentTab) {
                "INKUBATOR" -> InkubatorTabContent(viewModel)
                "UNGGAS" -> UnggasTabContent(viewModel)
                "ANALISA_AI" -> AnalisaAITabContent(viewModel)
                "PENJUALAN" -> PenjualanTabContent(viewModel)
            }
        }
    }
}

@Composable
fun InkubatorTabContent(viewModel: HatchMateViewModel) {
    val inkubatorList by viewModel.inkubatorList.collectAsState()
    LazyColumn(Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        items(inkubatorList) { batch ->
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Jenis Unggas: ${batch.jenisUnggas}", style = MaterialTheme.typography.titleMedium)
                    Text("Masuk: ${batch.tanggalMasuk} | Menetas: ${batch.tanggalMenetas}")
                    Text("Suhu: ${batch.suhu}°C | Kelembapan: ${batch.kelembapan}%")
                    Text("Perlakuan: ${batch.perlakuanTambahan}", color = Color.Blue)
                }
            }
        }
    }
}

@Composable
fun UnggasTabContent(viewModel: HatchMateViewModel) {
    val unggasList by viewModel.unggasList.collectAsState()
    LazyColumn(Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        items(unggasList) { unggas ->
            val statusColor = when(unggas.statusKesehatan.lowercase()) {
                "sehat" -> Color.Green
                "lesu" -> Color.Yellow
                else -> Color.Red
            }
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier
                            .size(12.dp)
                            .background(statusColor))
                        Spacer(Modifier.width(8.dp))
                        Text("Batch Unggas #${unggas.id} - ${unggas.jenisUnggas}")
                    }
                    Text("Usia: ${unggas.usiaHari} Hari | Jumlah Stok Hidup: ${unggas.jumlah} Ekor")
                    Text("Pakan harian: ${unggas.pemberianPakan}")
                    
                    Button(
                        onClick = { viewModel.inputKematianUnggas(unggas.id, 1, unggas.jenisUnggas, 1) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Catat Kematian (-1 Ekor)", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AnalisaAITabContent(viewModel: HatchMateViewModel) {
    val aiData by viewModel.analisaAIList.collectAsState()
    LazyColumn(Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        items(aiData) { data ->
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Batch ID: ${data.batchId}", style = MaterialTheme.typography.titleLarge)
                    Text("Total Telur: ${data.jumlahTotal} -> Menetas: ${data.jumlahMenetas}")
                    Text("Gagal Tetas: ${data.gagalMenetas} | Infertil: ${data.jumlahInfertil}")
                    Text("Efisiensi Pembesaran AI: ${String.format("%.1f", data.efisiensiPembesaran)}%", color = Color.Magenta)
                    
                    Divider(Modifier.padding(vertical = 8.dp))
                    Text("Rekomendasi AI:", style = MaterialTheme.typography.bodyMedium)
                    if (data.gagalMenetas > (data.jumlahTotal * 0.2)) {
                        Text("⚠ Periksa kualitas telur dan suhu transportasi", color = Color.Red)
                        Text("⚠ Naikkan kelembapan 70-75% pada 3 hari terakhir", color = Color.Red)
                        Text("✓ Lakukan fumigasi inkubator setelah batch ini", color = Color.DarkGray)
                    } else {
                        Text("✔ Kondisi Batch Aman - Pertahankan sanitasi", color = Color.Green)
                    }
                }
            }
        }
    }
}

@Composable
fun PenjualanTabContent(viewModel: HatchMateViewModel) {
    val stokGudang by viewModel.gudangStokList.collectAsState()
    Column(Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Mesin Kasir Digital", style = MaterialTheme.typography.titleLarge)
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        LazyColumn(Modifier.weight(1f)) {
            items(stokGudang) { item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(item.itemName, style = MaterialTheme.typography.bodyLarge)
                        Text("Stok: ${item.kuantitas} unit")
                    }
                    Button(onClick = { viewModel.prosesPenjualanDigital(item.itemName, 1, "2026-05-31") }) {
                        Text("Jual")
                    }
                }
            }
        }
    }
}
