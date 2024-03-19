package com.jmsoft.main.model

import android.bluetooth.BluetoothDevice

data class BluetoothScanModel(val device: BluetoothDevice, val isConnected: Boolean)