package com.jmsoft.main.`interface`

import android.bluetooth.BluetoothDevice

interface DeviceConnectedCallback {
    fun onDeviceConnected(device:BluetoothDevice)

    fun onDeviceNotConnected()

}