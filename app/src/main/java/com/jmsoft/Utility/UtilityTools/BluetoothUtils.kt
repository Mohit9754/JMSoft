package com.jmsoft.Utility.UtilityTools

import android.annotation.SuppressLint
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.ACTION_FOUND
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.main.`interface`.BluetoothOffCallback
import com.jmsoft.main.`interface`.ConnectedDeviceCallback
import com.jmsoft.main.`interface`.DeviceFoundCallback
import com.jmsoft.main.`interface`.PairStatusCallback


object BluetoothUtils {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothStateReceiver: BroadcastReceiver
    private lateinit var receiver: BroadcastReceiver

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
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
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

    //Checks if Bluetooth is on or off
    fun isEnableBluetooth(
    ): Boolean {

        val bluetoothAdapter =
            BluetoothAdapter.getDefaultAdapter() ?: // Device doesn't support Bluetooth
            // Handle this case accordingly
            return false

        return bluetoothAdapter.isEnabled
    }

    // Register BroadcastReceiver for Bluetooth device discovery
    @SuppressLint("MissingPermission")
    fun registerBroadCastReceiver(contexts: Context, deviceFoundCallback: DeviceFoundCallback) {

        // Create a BroadcastReceiver for ACTION_FOUND.
        receiver = object : BroadcastReceiver() {
            @SuppressLint("NotifyDataSetChanged", "MissingPermission", "SuspiciousIndentation")
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action

                if (ACTION_FOUND == action) {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                    if (device != null && device.name != null) {

                        deviceFoundCallback.onDeviceFound(device)

                    }
                }
            }
        }

        val filter = IntentFilter(ACTION_FOUND)

        //Register Receiver
        contexts.registerReceiver(receiver, filter)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothAdapter.startDiscovery()
    }

    fun unRegisterBroadCastReceiver(context: Context) {
        context.unregisterReceiver(receiver)
    }

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

    fun unRegisterBluetoothStateReceiver(context: Context) {
        context.unregisterReceiver(bluetoothStateReceiver)
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
                            val connectedDevice =  ArrayList(connectedDevices)
                            callback.onDeviceFound(connectedDevice)
                        } else {
                            callback.onDeviceNotFound()
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