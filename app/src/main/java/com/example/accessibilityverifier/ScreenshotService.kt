package com.example.accessibilityverifier

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.accessibilityverifier.axemodels.MediaProjectionHolder
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream


class ScreenshotService : Service() {

    lateinit var mediaProjectionManager: MediaProjectionManager
    var mediaProjection: MediaProjection? = null

    override fun onCreate() {
        super.onCreate()
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun setForegroundService() {
        //set the notification
        val notification = NotificationCompat.Builder(this, "channel_service")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("My Service")
            .setContentText("This service is running in the foreground")
            .build()

        //set the channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("channel_service", "channel service name", NotificationManager.IMPORTANCE_LOW)
            channel.description = "This channel is used for my service notifications"
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        //set the service as foreground
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        //mediaProjection?.unregisterCallback(projectorCallback)
        mediaProjection?.stop()
        mediaProjection = null
        Log.d("XDEBUG", "foreground service destroyed")
        //stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    companion object {
        const val ACTION_SCREENSHOT = "action_take_screenshot"
        const val ACTION_STOP = "action_stop"
        const val NOTIFICATION_ID = 111
        var imageProcessed = false
    }

}