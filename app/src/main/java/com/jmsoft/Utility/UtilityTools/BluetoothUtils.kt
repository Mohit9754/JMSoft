package com.jmsoft.Utility.UtilityTools

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.main.`interface`.ConnectedDeviceCallback


object BluetoothUtils {

    @RequiresApi(Build.VERSION_CODES.S)
    private val permissionsForVersionAbove11 = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADMIN
//        Manifest.permission.ACCESS_FINE_LOCATION,
//        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val permissionsForVersionBelow12 = arrayOf(

        Manifest.permission.BLUETOOTH,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH_ADMIN
    )

    // Requesting Run time permission for bluetooth
    fun requestPermissions(
        context: Context,
        permissionsRequestCode: Int
    ) {

        // For versions above Android 11
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                context as Activity,
                permissionsForVersionAbove11,
                permissionsRequestCode
            )
        } else {
            // For versions below Android 12, you should request location permissions normally
            ActivityCompat.requestPermissions(
                context as Activity,
                permissionsForVersionBelow12,
                permissionsRequestCode
            )
        }

    }

    // Bluetooth Intent for turn on the bluetooth And Show Bottom Sheet
    fun checksForAccessCoarseLocationPermission(context: Context,permissionsRequestCode: Int):Boolean {

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(context,permissionsRequestCode)
            return false
        }
        return true
    }

    //Checks if Bluetooth is on or off
    fun isEnableBluetooth(
    ): Boolean {

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            ?: // Device doesn't support Bluetooth
            // Handle this case accordingly
            return false

        return bluetoothAdapter.isEnabled
    }

    // Checks if bluetooth permission has or not
    fun hasBluetoothPermissions(context: Context): Boolean {

        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            permissionsForVersionAbove11
        else
            permissionsForVersionBelow12

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_DENIED
            ) {
                return false
            }
        }
        return true
    }

    fun getConnectedDevice(context: Context?, callback: ConnectedDeviceCallback) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
//            callback.onError("Bluetooth is either not supported or disabled")
            return
        }

        val serviceListener: BluetoothProfile.ServiceListener =
            object : BluetoothProfile.ServiceListener {
                @SuppressLint("MissingPermission")
                override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                    if (profile == BluetoothProfile.A2DP) {
                        val a2dp = proxy as BluetoothA2dp
                        val connectedDevices = a2dp.connectedDevices
                        if (connectedDevices.isNotEmpty()) {
                            // Get the first connected device (you may handle multiple devices if needed)
                            val connectedDevice = connectedDevices[0]
                            callback.onDeviceFound(connectedDevice)
                        } else {
//                            callback.onError("No connected device found")
                        }
                    }
                }

                override fun onServiceDisconnected(profile: Int) {
                    // Handle disconnection if needed
                }
            }
        bluetoothAdapter.getProfileProxy(context, serviceListener, BluetoothProfile.A2DP)
    }



}