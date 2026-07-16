package com.skygrime.googledriveanalyser.presentation.utils

import java.text.DecimalFormat

object Formatter {
    fun formatSize(sizeInBytes: Long): String {
        if (sizeInBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(sizeInBytes.toDouble()) / Math.log10(1024.0)).toInt()
        val index = digitGroups.coerceIn(0, units.lastIndex)
        return DecimalFormat("#,##0.#").format(sizeInBytes / Math.pow(1024.0, index.toDouble())) + " " + units[index]
    }
}
