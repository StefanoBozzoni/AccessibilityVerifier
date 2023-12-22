package com.example.accessibilityverifier.models

data class Results(
    val analyzedUrl: String,
    val htmlDocumentHTML: String,
    val violations: List<String>
)