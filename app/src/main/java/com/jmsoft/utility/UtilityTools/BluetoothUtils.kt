package com.jmsoft.utility.UtilityTools

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.ACTION_FOUND
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.View
import androidx.core.content.IntentCompat
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.main.`interface`.BluetoothOffCallback
import com.jmsoft.main.`interface`.ConnectedDeviceCallback
import com.jmsoft.main.`interface`.DeviceConnectedCallback
import com.jmsoft.main.`interface`.DeviceFoundCallback
import com.jmsoft.main.`interface`.PairStatusCallback
import java.io.IOException
import java.util.UUID


object BluetoothUtils {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothStateReceiver: BroadcastReceiver
    private lateinit var receiver: BroadcastReceiver



    //Checks if device is paired or not
    @SuppressLint("MissingPermission")
    fun isDevicePaired(device: BluetoothDevice): Boolean {
        return device.bondState == BluetoothDevice.BOND_BONDED
    }

    // Method to initiate pairing with a Bluetooth device
    @SuppressLint("MissingPermission")
    fun pairDevice(
        context: Context,
        device: BluetoothDevice,
        pairStatusCallback: PairStatusCallback
    ) {

        // BroadcastReceiver to handle bond state changes
        val bondStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {

                    val device = IntentCompat.getParcelableExtra(intent, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)

                    val bondState =
                        intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                    if (device != null && bondState == BluetoothDevice.BOND_BONDED) {
                        // Device successfully paired
                        Utils.E("Device paired successfully:  ${device.name}")
                        // Pairing successful, now connect
                        pairStatusCallback.pairSuccess()

                        context.unregisterReceiver(this)
                    } else if (bondState == BluetoothDevice.BOND_NONE) {
                        // Pairing failed or was cancelled
                        Utils.E("Pairing failed or was cancelled. ")
                        pairStatusCallback.pairFail()

                        context.unregisterReceiver(this)
                    }
                }
            }
        }
        // Register for bond state changes
        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        context.registerReceiver(bondStateReceiver, filter)

        // Start pairing process
        val pairingStarted = device.createBond()
        if (!pairingStarted) {
            Utils.E("Failed to initiate pairing process.")
        }
    }

    // Establish a connection with a Bluetooth device
    class ConnectToDeviceThread(val device:BluetoothDevice,val root:View,val deviceConnectedCallback: DeviceConnectedCallback) : Thread() {

        private val SERIAL_UUID = UUID.fromString(Constants.bluetoothUuid)

        @SuppressLint("MissingPermission")
        private val mmSocket: BluetoothSocket? =
            device.createRfcommSocketToServiceRecord(SERIAL_UUID)

        @SuppressLint("MissingPermission")
        override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
//                bluetoothAdapter?.cancelDiscovery()

            mmSocket?.let { socket ->
                try {

                    // Connect to the remote device through the socket. This call blocks
                    // until it succeeds or throws an exception.
                    socket.connect()

                    root.post {
                        // Update the UI here
                        // For example, show a dialog
                        deviceConnectedCallback.onDeviceConnected(device)

                    }
                } catch (e: IOException) {
                    // Connection attempt failed
                    // You can handle the error here
                    e.printStackTrace()
                    root.post {
                        // Update the UI here
                        // For example, show a dialog
                        deviceConnectedCallback.onDeviceConnected(device)

                    }
                }
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Utils.E("Could not close the client socket")
            }
        }
    }

    //Checks if Bluetooth is on or off
    fun isEnableBluetooth(
        context: Context
    ): Boolean {

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter ?: // Device doesn't support Bluetooth
        return  false

        return bluetoothAdapter.isEnabled
    }

    //Unpair the device
    @SuppressLint("MissingPermission")
    fun unpairDevice(device: BluetoothDevice) {
        try {
            val method = device.javaClass.getMethod("removeBond")
            method.invoke(device)
            Utils.E("Device unpaired successfully: ${device.name}")
        } catch (e: Exception) {
            Utils.E("Failed to unpair device: ${e.message}")
        }
    }

    // Register BroadcastReceiver for Bluetooth device discovery
    @SuppressLint("MissingPermission")
    fun registerBroadCastReceiver(context: Context, deviceFoundCallback: DeviceFoundCallback) {

        // Create a Broadcast Receiver for ACTION_FOUND.
        receiver = object : BroadcastReceiver() {
            @SuppressLint("NotifyDataSetChanged", "MissingPermission", "SuspiciousIndentation")
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action

                if (ACTION_FOUND == action) {

                    val device = IntentCompat.getParcelableExtra(intent, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)

                    if (device != null && device.name != null) {
                    deviceFoundCallback.onDeviceFound(device)

                  }
                }
            }
        }

        val filter = IntentFilter(ACTION_FOUND)

        //Register Receiver
        context.registerReceiver(receiver, filter)
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter.startDiscovery()
    }

    //Unregister Broadcast Receiver
    fun unRegisterBroadCastReceiver(context: Context) {
        context.unregisterReceiver(receiver)
    }

    //Register Bluetooth State Receiver
    fun registerBluetoothStateReceiver(
        context: Context, bluetoothOffCallback: BluetoothOffCallback
    ) {

        bluetoothStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action
                if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    val state =
                        intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    when (state) {
                        BluetoothAdapter.STATE_OFF -> {
                            // Bluetooth turned off
                            bluetoothOffCallback.onBluetoothOff()
                        }

                        BluetoothAdapter.STATE_ON -> {
                            // Bluetooth turned on
                            println("Bluetooth turned on")
                        }
                    }
                }
            }
        }
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(bluetoothStateReceiver, filter)
    }

    //Unregister Bluetooth State Receiver
    fun unRegisterBluetoothStateReceiver(context: Context) {
        context.unregisterReceiver(bluetoothStateReceiver)
    }

    //get all connected Device
    @SuppressLint("MissingPermission")
    fun getConnectedDevice(context: Context, callback: ConnectedDeviceCallback) {

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            // callback.onError("Bluetooth is either not supported or disabled")
            return
        }

        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices

        if (pairedDevices.isNotEmpty()) {
            // Here you get all paired devices, you may need to filter based on the device type or name
            val connectedDevices = ArrayList<BluetoothDevice>()

            for (device in pairedDevices) {
                // Assuming the RFID device has a specific name or type, filter accordingly
                // e.g., if (device.name.contains("RFID")) or other criteria
                connectedDevices.add(device)
            }

            if (connectedDevices.isNotEmpty()) {
                callback.onDeviceFound(connectedDevices)
            } else {
                callback.onDeviceNotFound()
            }
        } else {
            callback.onDeviceNotFound()
        }
    }

    //get all connected Device
//    fun getConnectedDevice(context: Context, callback: ConnectedDeviceCallback) {
//
//        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
//        val bluetoothAdapter = bluetoothManager.adapter
//
//        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
////            callback.onError("Bluetooth is either not supported or disabled")
//            return
//        }
//
//        val serviceListener: BluetoothProfile.ServiceListener =
//            object : BluetoothProfile.ServiceListener {
//
//                @SuppressLint("MissingPermission")
//                override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
//                    if (profile == BluetoothProfile.A2DP) {
//                        val a2dp = proxy as BluetoothA2dp
//                        val connectedDevices = a2dp.connectedDevices
//                        if (connectedDevices.isNotEmpty()) {
//                            // Get the first connected device (you may handle multiple devices if needed)
//                            val connectedDevice =  ArrayList(connectedDevices)
//                            callback.onDeviceFound(connectedDevice)
//                        } else {
//                            callback.onDeviceNotFound()
//                        }
//                    }
//                }
//
//                override fun onServiceDisconnected(profile: Int) {
//                    // Handle disconnection if needed
//                }
//            }
//        bluetoothAdapter.getProfileProxy(context, serviceListener, BluetoothProfile.A2DP)
//    }

}