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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return START_NOT_STICKY
        }

        if (intent.action == ACTION_SCREENSHOT) {
            setForegroundService()
            //val resultCode = intent.getIntExtra("code", 0)
            //val resultData: Intent? = intent.getParcelableExtra("data")
            if (MediaProjectionHolder.get() == null) {
                MediaProjectionHolder.instantiateProjection(this)
            }
            mediaProjection = MediaProjectionHolder.get()
            //mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData!!)
            startScreenShotCapture(mediaProjection)
        }

        if (intent.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE) //remove the notificaiton and stop the service
            stopSelf()  //will not be restarted automatically by the system
        }

        return START_NOT_STICKY
    }

    fun getProjection(): MediaProjection? {
        return mediaProjection
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

    private fun startScreenShotCapture(projection: MediaProjection?) {

        val window = getSystemService(WINDOW_SERVICE) as WindowManager
        val display = window.getDefaultDisplay()  //this.display
        val displayMetrics = Resources.getSystem().displayMetrics // Default values
        display.getRealMetrics(displayMetrics)
        val mImageReader = ImageReader.newInstance(displayMetrics.widthPixels, displayMetrics.heightPixels, PixelFormat.RGBA_8888, 2)

        val handlerThread = HandlerThread("MyHandlerThread")
        handlerThread.start()

        projection?.registerCallback(
            object : MediaProjection.Callback() {
                override fun onStop() {
                    super.onStop()
                }
                override fun onCapturedContentResize(width: Int, height: Int) {
                    super.onCapturedContentResize(width, height)
                }
                override fun onCapturedContentVisibilityChanged(isVisible: Boolean) {
                    super.onCapturedContentVisibilityChanged(isVisible)
                }
            }
            , Handler(handlerThread.looper))

        projection?.createVirtualDisplay(
            "ScreenCapture",
            displayMetrics.widthPixels,
            displayMetrics.heightPixels,
            displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mImageReader.getSurface(),
            null,
            null
        )

        val lock = Any()
        mImageReader.setOnImageAvailableListener(
            { reader ->
                    synchronized(lock) {
                        val image = reader.acquireLatestImage()
                        if (image != null && !imageProcessed) {
                            Log.d("XDEBUG", "Inside image reader writing function")
                            val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
                            bitmap.copyPixelsFromBuffer(image.getPlanes()[0].getBuffer())
                            image.close()
                            val fileOutputStream = BufferedOutputStream(FileOutputStream(File(filesDir, "screenshot.png")))
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                            fileOutputStream.flush()
                            fileOutputStream.close()
                            Log.d("XDEBUG", "Scrittura bitmap")
                            imageProcessed = true
                            this.sendBroadcast(Intent(MyAccessibilityService.screenshotDone))
                        } else {
                            if (image != null) {
                                image.close()
                            }
                        }
                    }
            },
            Handler(handlerThread.looper)
        )
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