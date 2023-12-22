package com.example.accessibilityverifier

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
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
import android.widget.Button
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.example.accessibilityverifier.ScreenshotService.Companion.ACTION_SCREENSHOT
import com.example.accessibilityverifier.ScreenshotService.Companion.ACTION_STOP
import com.example.accessibilityverifier.axemodels.AxeScanner
import com.example.accessibilityverifier.axemodels.AxeScannerFactory
import com.example.accessibilityverifier.axemodels.DeviceConfigFactory
import com.example.accessibilityverifier.axemodels.DisplayMetricsHelper
import com.example.accessibilityverifier.axemodels.EventBroadCastReceiver
import com.example.accessibilityverifier.axemodels.MediaProjectionHolder
import com.example.accessibilityverifier.axemodels.ResultsV2ContainerSerializer
import com.example.accessibilityverifier.axemodels.ScanException
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileOutputStream


class MyAccessibilityService : AccessibilityService() {
    var mLayout: FrameLayout? = null

    private var axeScanner: AxeScanner? = null
    //private var atfaScanner: ATFAScanner? = null
    private var deviceConfigFactory: DeviceConfigFactory? = null

    private val eventReceiver = object : EventBroadCastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == drawUiEvent) {
                if (intent.getBooleanExtra("exit", true) == false) {
                    drawUI()
                } else {
                    stopSelf()
                }
            }
            if (intent?.action == screenshotDone) {
                mLayout?.visibility = View.VISIBLE
            }

        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerEventReceiver() {
        val eventFilter = IntentFilter()
        eventFilter.addAction(drawUiEvent)
        eventFilter.addAction(screenshotDone)
        if (SDK_INT > VERSION_CODES.TIRAMISU) {
            registerReceiver(eventReceiver, eventFilter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(eventReceiver, eventFilter, RECEIVER_EXPORTED)
        }
    }

    init {
        deviceConfigFactory = DeviceConfigFactory()
        axeScanner  = AxeScannerFactory.createAxeScanner(deviceConfigFactory) { this.getRealDisplayMetrics() }
        //atfaScanner = ATFAScannerFactory.createATFAScanner(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onServiceConnected() {
        /*
        registerEventReceiver()
        val intent = Intent(getApplicationContext(), ScreenshotActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        (getApplicationContext()).startActivity(intent)
        */

        drawUI()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun drawUI() {
        //if (MediaProjectionHolder.resultCode!=0) {
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
            val btnScan = mLayout?.findViewById<Button>(R.id.btnScan)

            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

            btnScan?.setOnTouchListener(object : View.OnTouchListener {
                private var initialX: Int = 0
                private var initialY: Int = 0
                private var moved: Boolean = false
                private var initialTouchX: Float = 0.toFloat()
                private var initialTouchY: Float = 0.toFloat()

                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialX = params.x
                            initialY = params.y
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            moved = false
                            return true
                        }

                        MotionEvent.ACTION_MOVE -> {
                            params.x = initialX + (event.rawX - initialTouchX).toInt()
                            params.y = initialY + (event.rawY - initialTouchY).toInt()
                            windowManager.updateViewLayout(mLayout, params)
                            moved = true
                            return true
                        }

                        MotionEvent.ACTION_UP -> {
                            // Perform a click action when the touch is released
                            if (!moved) {
                                v.post {
                                    v.performClick()
                                }
                                return false
                            } else
                                return true
                        }

                        else -> return false
                    }
                }
            })
            configureBtn()
        //}
    }

    private fun configureBtn() {
        val btnScan = mLayout?.findViewById<Button>(R.id.btnScan)
        val btnScreenShot = mLayout?.findViewById<Button>(R.id.btnScreenshot)

        btnScan?.setOnClickListener {
            animateButtonBackground(btnScan)
            val risultato = doFullScan(getRootInActiveWindow())
            Repository.sendAxeResultTest(risultato)
        }

        btnScreenShot?.setOnClickListener {
            mLayout?.visibility = View.GONE
            if (MediaProjectionHolder.get() == null) {
                val intent = Intent(this, ScreenshotService::class.java)
                intent.action = ACTION_SCREENSHOT
                startService(intent)
            } else {
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                    ScreenshotService.imageProcessed = false
                    }, 40)
            }
        }

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
        unregisterReceiver(eventReceiver)
        val intent = Intent(this,ScreenshotService::class.java).apply { action = ACTION_STOP }
        startService(intent)
        super.onDestroy()
    }

    private fun getRealDisplayMetrics(): DisplayMetrics? {
        // Correct screen metrics are only accessible within the context of the running
        // service. They're not available when the service initializes, hence the callback
        return DisplayMetricsHelper.getRealDisplayMetrics(this)
    }

    private fun doFullScan( rootNode: AccessibilityNodeInfo): String {
        val screenShot: Bitmap? = null
        val axeResult = axeScanner!!.scanWithAxe(rootNode, screenShot) ?: throw ScanException("Scanner returned no data")
        /* Scansione con Android Testing Framework
        val atfaResults: List<AccessibilityHierarchyCheckResult> = atfaScanner!!.scanWithATFA(rootNode,
            BitmapImage(screenshot)
        )
       */
        val gson = Gson()
        val json = gson.toJson(axeResult)

        val regex = "\"\"".toRegex()
        val newString = json.replace(regex, "'")

        Log.d("XDEBUG", newString)

        //val resultsV2ContainerSerializer = ResultsV2ContainerSerializer(GsonBuilder())
        //return resultsV2ContainerSerializer.createResultsJson(axeResult)
        return newString

    }

    fun animateButtonBackground(context: View) {
        val button: Button = context.findViewById(R.id.btnScan)

        val originalBackgroundColor = button.backgroundTintList?.defaultColor ?: Color.TRANSPARENT

        val darkGreyColor = Color.parseColor("#DDDDDD") // Dark grey color

        val toDarkGreyAnimator = ValueAnimator.ofObject(ArgbEvaluator(), originalBackgroundColor, darkGreyColor)
        toDarkGreyAnimator.duration = 100

        val toOriginalColorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), darkGreyColor, originalBackgroundColor)
        toOriginalColorAnimator.duration = 100

        toDarkGreyAnimator.addUpdateListener { valueAnimator ->
            val color = valueAnimator.animatedValue as Int
            val colorStateList = ColorStateList.valueOf(color)
            button.backgroundTintList = colorStateList
        }

        toOriginalColorAnimator.addUpdateListener { valueAnimator ->
            val color = valueAnimator.animatedValue as Int
            val colorStateList = ColorStateList.valueOf(color)
            button.backgroundTintList = colorStateList
        }

        toDarkGreyAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
            }
            override fun onAnimationEnd(animation: Animator) {
                toOriginalColorAnimator.start()
            }
            override fun onAnimationCancel(animation: Animator) {
            }
            override fun onAnimationRepeat(animation: Animator) {
            }
        })
        toDarkGreyAnimator.start()
    }

    @RequiresApi(VERSION_CODES.R)
    fun takeScreenshot() {
        takeScreenshot(
            Display.DEFAULT_DISPLAY,
            applicationContext.mainExecutor,
            object : TakeScreenshotCallback {
                override fun onSuccess(screenshotResult: ScreenshotResult) {
                    //TODO("Not yet implemented")
                    val bitmap = Bitmap.wrapHardwareBuffer(screenshotResult.hardwareBuffer, screenshotResult.colorSpace)
                    //AccessibilityUtils.saveImage(bitmap, applicationContext, "WhatsappIntegration")
                    // Save the bitmap to a file
                    val filename = "provafile.png" //"${System.currentTimeMillis()}.png"
                    val file = File(filesDir, filename)
                    try {
                        val out = FileOutputStream(file)
                        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, out)
                        out.flush()
                        out.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(errorCode: Int) {
                    TODO("Not yet implemented")
                }
            }
        )
    }

    companion object {
        const val drawUiEvent    = "com.example.accessibilityverifier.displayUi"
        const val screenshotDone = "com.example.accessibilityverifier.screenShotDone"
    }

}
