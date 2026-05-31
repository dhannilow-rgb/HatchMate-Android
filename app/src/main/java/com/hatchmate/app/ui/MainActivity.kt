package com.hatchmate.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.hatchmate.app.core.database.AppDatabase
import com.hatchmate.app.features.HatchMateViewModel
import com.hatchmate.app.ui.theme.HatchMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.hatchMateDao()
        val viewModel = HatchMateViewModel(dao)
        
        setContent {
            HatchMateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainDashboardScreen(viewModel)
                }
            }
        }
    }
}
