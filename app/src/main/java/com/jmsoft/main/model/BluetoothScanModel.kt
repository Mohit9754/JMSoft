package com.jmsoft.main.model

import android.bluetooth.BluetoothDevice

data class BluetoothScanModel(val device: BluetoothDevice, var isConnected: Boolean)