package com.example.accessibilityverifier

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import androidx.core.content.ContextCompat.getSystemService

object MediaProjectionHolder {
    var resultCode: Int = 0
    var resultData: Intent? = null


    private var sharedMediaProjection: MediaProjection? = null

    fun cleanUp() {
        if (sharedMediaProjection != null) {
            sharedMediaProjection!!.stop()
            sharedMediaProjection = null
        }
    }

    fun get(): MediaProjection? {
        return sharedMediaProjection
    }

    fun set(projection: MediaProjection?) {
        sharedMediaProjection = projection
    }

    fun instantiateProjection(context: Context) {
        val mediaProjectionManager = getSystemService(context, MediaProjectionManager::class.java)
        resultData?.let {
            sharedMediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, it)
        }
    }

    fun setParametersForInstantiation(code: Int, intentData: Intent?) {
        resultCode = code
        resultData = intentData
    }

}