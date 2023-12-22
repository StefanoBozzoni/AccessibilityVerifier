package com.example.accessibilityverifier.models

data class SaveTestRequest(
    val results: Results,
    val url: String,
    val mobile: Boolean
)