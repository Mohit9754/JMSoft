package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.Utility.UtilityTools.BluetoothUtils
import com.jmsoft.basic.UtilityTools.Constants.Companion.bluetoothUuid
import com.jmsoft.basic.UtilityTools.Constants.Companion.connected
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.ItemDeviceListBinding
import com.jmsoft.main.`interface`.PairStatusCallback
import com.jmsoft.main.model.BluetoothScanModel
import com.jmsoft.main.model.DeviceModel
import java.io.IOException
import java.util.UUID

/**
 * Bluetooth Scan Adapter
 *
 * Show the list of Scan Bluetooth devices
 * Make pair
 * Make Connection
 */

class BluetoothScanAdapter(
    private val context: Context,
    private val bluetoothScanList: ArrayList<BluetoothScanModel>,
    private val deviceType: String?
) :
    RecyclerView.Adapter<BluetoothScanAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemDeviceListBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = bluetoothScanList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(bluetoothScanList[position], position)
    }

    fun connectingDialog(deviceName: String) {

        val dialog = Dialog(context)
        dialog.setCanceledOnTouchOutside(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_connecting)
        dialog.findViewById<TextView>(R.id.tvDeviceName).text = deviceName
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.findViewById<MaterialCardView>(R.id.mcvOK).setOnClickListener {
            dialog.dismiss()
        }
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()
    }

    @SuppressLint("MissingPermission")
    inner class MyViewHolder(private val binding: ItemDeviceListBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var deviceModel: BluetoothScanModel
        private var position: Int = -1
        private var connectedDevice: BluetoothDevice? = null

        fun bind(deviceModel: BluetoothScanModel, position: Int) {
            this.deviceModel = deviceModel
            this.position = position

            //setting the Name of the Scanned Device
            setName()

            //setting the Mac Address of the Scanned Device
            setMacAddress()

            //Hide the Bin
            hideBin()

            //Check if Device is already Connected or Not
            checkTheDeviceConnection()

            //Setting Click on Device
            binding.mcvDevice.setOnClickListener(this)
        }

        // Method to move the Connected to the first position
        private fun moveToFirstPosition() {

//            if (position != 0) {
//
//                bluetoothScanList.remove(bluetoothScanList[position])
//                bluetoothScanList.add(
//                    0,
//                    bluetoothScanList[position]
//                ) // Add item to the beginning of the list
//                notifyItemMoved(position, 0) // Notify adapter about the move
//
//            }
        }

        //Setting the Status of the Connected Device
        private fun setConnectedStatus() {
            deviceModel.isConnected = true
            binding.llStatus.visibility = View.VISIBLE
            binding.mcvIndicator.setCardBackgroundColor(context.getColor(R.color.green))
            binding.tvStatus.text = connected
            binding.tvStatus.setTextColor(context.getColor(R.color.green))


        }

        //Check if Device is already Connected or Not
        private fun checkTheDeviceConnection() {

            if (deviceModel.isConnected) {
                setConnectedStatus()

            } else {
                binding.llStatus.visibility = View.GONE
            }
        }

        //Hide the Bin
        private fun hideBin() {
            binding.ivDelete.visibility = View.GONE
        }

        //setting the Name of the Scanned Device
        @SuppressLint("MissingPermission")
        private fun setName() {
            Utils.E(deviceModel.device.name.toString())
            binding.tvDeviceName.text = deviceModel.device.name.toString()
        }

        //setting the Mac Address of the Scanned Device
        private fun setMacAddress() {
            binding.tvMacAddress.text = deviceModel.device.address.toString()
        }

        private fun onConnectSuccess() {

            connectingDialog(deviceModel.device.name)
            setConnectedStatus()
            connectedDevice = deviceModel.device
            // Move the Connected Device to First Position
            moveToFirstPosition()

            Utils.E("Connected to device: ${deviceModel.device.getName()}")

            val userId = Utils.GetSession().userId

            val deviceMode = DeviceModel()

            deviceMode.deviceName = deviceType
            deviceMode.deviceAddress = deviceModel.device.address
            deviceMode.userId = userId

            //Insert Data in the device table
            Utils.insertNewDeviceData(deviceMode)

        }

        private fun onConnectFailed() {

            Utils.T(context, "Failed to Connect")
        }

        // Establish a connection with a Bluetooth device

        private inner class ConnectToDeviceThread : Thread() {

            private val SERIAL_UUID = UUID.fromString(bluetoothUuid)

            private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
                deviceModel.device.createRfcommSocketToServiceRecord(SERIAL_UUID)
            }

            override fun run() {
                // Cancel discovery because it otherwise slows down the connection.
//                bluetoothAdapter?.cancelDiscovery()

                mmSocket?.let { socket ->

                    try {

                        // Connect to the remote device through the socket. This call blocks
                        // until it succeeds or throws an exception.
                        socket.connect()

                        binding.root.post {
                            // Update the UI here
                            // For example, show a dialog
                            onConnectSuccess()


                        }
                    } catch (e: IOException) {
                        // Connection attempt failed
                        // You can handle the error here
                        e.printStackTrace()
                        onConnectFailed()
                    }

                    // The connection attempt succeeded. Perform work associated with
                    // the connection in a separate thread.
//                    manageMyConnectedSocket(socket)
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

        //Checks Device is paired or not and connect it
        @SuppressLint("MissingPermission")
        private fun checkPairAndConnectDevice() {

            // Check if the device is paired
            if (BluetoothUtils.isDevicePaired(deviceModel.device)) {
                // Device is paired, you can establish a connection here

                val connectToDeviceThread = ConnectToDeviceThread()
                connectToDeviceThread.start()

                Utils.E("Device is already paired")

            } else {

                //Make Pair Device
                BluetoothUtils.pairDevice(context, deviceModel.device, object : PairStatusCallback {

                    override fun pairSuccess() {

                        val connectToDeviceThread = ConnectToDeviceThread()
                        connectToDeviceThread.start()
                    }

                    override fun pairFail() {
                        Utils.T(context, "Paired Failed")
                    }

                })

            }
        }

        //Handles All the Clicks
        @SuppressLint("MissingPermission")
        override fun onClick(v: View?) {

            if (v == binding.mcvDevice) {

                //Checks if device is already Connected or not
                if (!deviceModel.isConnected) {

                    if (connectedDevice == null) {

                        checkPairAndConnectDevice()

                    } else {

//                        var connectToDeviceThread = ConnectToDeviceThread()
////                        connectToDeviceThread.start()
//                        connectToDeviceThread.disconnectDevice()
//                        connectedDevice = null
                        Utils.T(context, "fount $connectedDevice")

                    }
                } else {

                    Utils.T(context, context.getString(R.string.already_connected))
                }

            }
        }
    }
}
