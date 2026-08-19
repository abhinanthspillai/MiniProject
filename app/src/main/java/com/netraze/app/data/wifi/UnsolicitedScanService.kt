package com.netraze.app.data.wifi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class UnsolicitedScanService : Service() {

    @Inject
    lateinit var wifiScanCoordinator: WifiScanCoordinator

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var scanJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val surveyIdStr = intent?.getStringExtra(EXTRA_SURVEY_ID)

        if (action == ACTION_STOP_SCAN) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (!surveyIdStr.isNull_or_empty_or_blank()) {
            val surveyId = UUID.fromString(surveyIdStr)
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Netraze Unsolicited Auto-Scan")
                .setContentText("Continuous Wi-Fi observations active...")
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build()

            startForeground(NOTIFICATION_ID, notification)

            scanJob?.cancel()
            scanJob = serviceScope.launch {
                while (isActive) {
                    // D076 Frozen Contract: Unsolicited scan cycles MUST have spatialPositionId = null
                    wifiScanCoordinator.performScanCycle(surveyId = surveyId, spatialPositionId = null)
                    delay(10_000L) // 10s auto-scan interval
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        scanJob?.cancel()
        serviceJob.cancel()
        super.onDestroy()
    }

    private fun String?.isNull_or_empty_or_blank(): Boolean {
        return this == null || this.trim().isEmpty()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Netraze Unsolicited Auto-Scan",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground channel for Netraze background Wi-Fi scanning"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "netraze_unsolicited_scan"
        const val NOTIFICATION_ID = 4001
        const val EXTRA_SURVEY_ID = "extra_survey_id"
        const val ACTION_STOP_SCAN = "action_stop_scan"

        fun startService(context: Context, surveyId: UUID) {
            val intent = Intent(context, UnsolicitedScanService::class.java).apply {
                putExtra(EXTRA_SURVEY_ID, surveyId.toString())
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, UnsolicitedScanService::class.java).apply {
                action = ACTION_STOP_SCAN
            }
            context.startService(intent)
        }
    }
}
