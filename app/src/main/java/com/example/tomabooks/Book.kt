package com.example.tomabooks

import android.net.Uri

data class Book(
    val name: String,
    val uri: Uri,
    val title: String? = null,
    val author: String? = null,
    val parentUri: Uri? = null,
    val relativePath: List<String> = emptyList()
)
