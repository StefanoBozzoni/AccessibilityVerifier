package com.example.accessibilityverifier

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.accessibilityverifier.axemodels.AxeScanner
import com.example.accessibilityverifier.axemodels.AxeScannerFactory
import com.example.accessibilityverifier.axemodels.DeviceConfigFactory
import com.example.accessibilityverifier.axemodels.DisplayMetricsHelper
import com.example.accessibilityverifier.axemodels.EventBroadCastReceiver
import com.example.accessibilityverifier.axemodels.ResultsV2ContainerSerializer
import com.example.accessibilityverifier.axemodels.ScanException
import com.example.accessibilityverifier.data.Repository
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executor
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MyAccessibilityService : AccessibilityService() {
    var mLayout: FrameLayout? = null
    var mWaitingPageLayout: FrameLayout? = null

    private var axeScanner: AxeScanner? = null
    private var deviceConfigFactory: DeviceConfigFactory? = null
    private lateinit var btnScan: ImageView
    private var movingBar: Boolean = false
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        deviceConfigFactory = DeviceConfigFactory()
        axeScanner  = AxeScannerFactory.createAxeScanner(deviceConfigFactory) { this.getRealDisplayMetrics() }
        //atfaScanner = ATFAScannerFactory.createATFAScanner(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onServiceConnected() {
        drawUI()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun drawUI() {
            val info = AccessibilityServiceInfo()
            info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            info.feedbackType = AccessibilityEvent.TYPES_ALL_MASK
            info.notificationTimeout = 0
            info.flags = (AccessibilityServiceInfo.DEFAULT or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS)
            serviceInfo = info

            val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            mLayout = FrameLayout(this)
            val lp = WindowManager.LayoutParams()
            lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            lp.format = PixelFormat.TRANSLUCENT
            lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            lp.gravity = Gravity.CENTER
            val inflater = LayoutInflater.from(this)
            inflater.inflate(R.layout.action_bar, mLayout)
            wm.addView(mLayout, lp)

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.CENTER
            btnScan = mLayout!!.findViewById(R.id.btnScan)

            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

            btnScan.setOnTouchListener(object : View.OnTouchListener {
                private var initialX: Int = 0
                private var initialY: Int = 0
                private var initialTouchX: Float = 0.toFloat()
                private var initialTouchY: Float = 0.toFloat()

                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialX = params.x
                            initialY = params.y
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            movingBar = false
                            return true
                        }

                        MotionEvent.ACTION_MOVE -> {
                            movingBar = true
                            btnScan.setImageResource(R.drawable.accessibility_icon3);
                            params.x = initialX + (event.rawX - initialTouchX).toInt()
                            params.y = initialY + (event.rawY - initialTouchY).toInt()
                            windowManager.updateViewLayout(mLayout, params)
                            return true
                        }

                        MotionEvent.ACTION_UP -> {
                            // Perform a click action when the touch is released

                            if (!movingBar) {
                                Toast.makeText(btnScan.context, "Invio dati in corso...", Toast.LENGTH_SHORT).show()
                                btnScan.setImageResource(R.drawable.hourglass)
                                serviceScope.launch {
                                    val scrShotBitmap = if (SDK_INT>30) {
                                        withContext(Dispatchers.Main) {
                                            mLayout?.visibility = View.INVISIBLE
                                        }
                                        prepareScreenshotBitmap()
                                    } else null

                                    withContext(Dispatchers.Main) {
                                        mLayout?.visibility = View.VISIBLE
                                    }

                                    val risultato = doFullScan(getRootInActiveWindow(), scrShotBitmap)

                                    Repository.sendAxeResultAccessibilityCheck(risultato)
                                    withContext(Dispatchers.Main) {
                                        btnScan.setImageResource(R.drawable.accessibility_icon3)
                                    }
                                }

                                return false
                            } else {
                                btnScan.setImageResource(R.drawable.accessibility_icon3)
                                return true
                            }

                        }

                        else -> return false
                    }
                }
            })
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            val eventoDescr = AccessibilityEvent.eventTypeToString(it.eventType)
        }
    }

    override fun onInterrupt() {
        //Not yet implemented
    }

    override fun onDestroy() {
        Log.d("XDEBUG","destroy accessibility service")
        super.onDestroy()
    }

    private fun getRealDisplayMetrics(): DisplayMetrics? {
        // Correct screen metrics are only accessible within the context of the running
        // service. They're not available when the service initializes, hence the callback
        return DisplayMetricsHelper.getRealDisplayMetrics(this)
    }

    private fun doFullScan( rootNode: AccessibilityNodeInfo, screenShot: Bitmap? = null): String {
        val axeResult = axeScanner!!.scanWithAxe(rootNode, screenShot) ?: throw ScanException("Scanner returned no data")
        val resultsV2ContainerSerializer = ResultsV2ContainerSerializer(GsonBuilder())
        val newString = resultsV2ContainerSerializer.createResultsJson(axeResult)
        return newString
    }

    @RequiresApi(VERSION_CODES.R)
    suspend fun prepareScreenshotBitmap(): Bitmap? {
        val dispatcher: CoroutineDispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher
        val executor = dispatcher.asExecutor()
        val result = suspendCoroutine{ continuation ->
            takeMyScreenshot(
                 executor,
                 onSuccess = { bitmap -> continuation.resumeWith(Result.success(bitmap))},
                 onError = {exception -> continuation.resumeWithException(exception)}
            )
        }
        return result
    }

    @RequiresApi(VERSION_CODES.R)
    fun takeMyScreenshot(executor: Executor, onSuccess: (bitmap: Bitmap?) -> Unit, onError: (e:Exception) -> Unit) {
        takeScreenshot(
            Display.DEFAULT_DISPLAY,
            executor,
            object : TakeScreenshotCallback {
                override fun onSuccess(screenshotResult: ScreenshotResult) {
                    val bitmap = Bitmap.wrapHardwareBuffer(screenshotResult.hardwareBuffer, screenshotResult.colorSpace)
                    onSuccess.invoke(bitmap)
                    /*
                    // Save the bitmap to a file
                    val filename = "provafile.png" //"${System.currentTimeMillis()}.png"
                    val file = File(filesDir, filename)
                    try {
                        val out = FileOutputStream(file)
                        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, out)
                        out.flush()
                        out.close()
                        Log.d("XDEBUG", "bitmap saved")
                        onSuccess.invoke(bitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onError.invoke(e)
                    }
                     */
                }

                override fun onFailure(errorCode: Int) {
                    Log.d("XDEBUG", "failed on takeScreenshot, errorcode: $errorCode")
                    onError.invoke(Exception(errorCode.toString()))
                }
            }
        )
    }

}
