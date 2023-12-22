package com.example.accessibilityverifier

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log

// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
class ScreenshotActivity : Activity() {
    private var mediaManager: MediaProjectionManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("XDEBUG", "avvio activity screenshot")
        mediaManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mediaManager?.createScreenCaptureIntent(), SCREENSHOT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SCREENSHOT) {
            if (resultCode== RESULT_OK) {
                MediaProjectionHolder.setParametersForInstantiation(resultCode, data)
                val eventIntent = Intent(MyAccessibilityService.drawUiEvent)
                eventIntent.putExtra("exit", false)
                finish()
                this.sendBroadcast(eventIntent)
                finishAffinity()
            } else {
                val eventIntent = Intent(MyAccessibilityService.drawUiEvent)
                eventIntent.putExtra("exit", true)
                finish()
                this.sendBroadcast(eventIntent)
                finishAffinity()
            }
        }
    }

    companion object {
        private const val SCREENSHOT = 99999
    }
}