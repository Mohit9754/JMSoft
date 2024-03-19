package com.jmsoft.main.model

import android.graphics.drawable.Drawable

data class DeviceModel(
    val deviceIcon: Drawable,
    val deviceName: String,
    val idNumber: String,
    val status: String,
)