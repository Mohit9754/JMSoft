package com.jmsoft.main.`interface`

import android.bluetooth.BluetoothDevice

interface ConnectedDeviceCallback {
    fun onDeviceFound(device: BluetoothDevice)
}