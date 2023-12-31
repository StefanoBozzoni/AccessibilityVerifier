package com.example.accessibilityverifier

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import com.example.accessibilityverifier.axemodels.MediaProjectionHolder

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

    companion object {
        private const val SCREENSHOT = 99999
    }
}