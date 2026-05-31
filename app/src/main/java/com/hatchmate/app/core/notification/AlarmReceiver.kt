package com.hatchmate.app.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.hatchmate.app.core.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val database = AppDatabase.getDatabase(context)
        val dao = database.hatchMateDao()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val activeBatches = dao.getActiveInkubatorBatches().first()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayDate = Date()

                var alarmShouldRing = false

                for (batch in activeBatches) {
                    try {
                        val dateMulai = dateFormat.parse(batch.mulaiPutar) ?: continue
                        val dateStop = dateFormat.parse(batch.stopPutar) ?: continue

                        if (todayDate.after(dateMulai) && todayDate.before(dateStop)) {
                            alarmShouldRing = true
                            break
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                if (alarmShouldRing) {
                    NotificationHelper.showTurnEggsNotification(context)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
