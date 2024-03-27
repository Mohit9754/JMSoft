package com.jmsoft.main.`interface`

import android.bluetooth.BluetoothDevice

interface DeviceFoundCallback {

    fun onDeviceFound(device: BluetoothDevice)

}