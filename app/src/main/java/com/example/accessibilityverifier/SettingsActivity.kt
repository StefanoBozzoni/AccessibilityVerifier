package com.example.accessibilityverifier

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.example.accessibilityverifier.axemodels.MediaProjectionHolder

// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
class SettingsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
    }

}