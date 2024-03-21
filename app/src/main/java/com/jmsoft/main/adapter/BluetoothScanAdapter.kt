package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import com.jmsoft.basic.UtilityTools.Constants.Companion.connected
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.ItemDeviceListBinding
import com.jmsoft.main.model.BluetoothScanModel
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
    private val bluetoothScanList: ArrayList<BluetoothScanModel>
) :
    RecyclerView.Adapter<BluetoothScanAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemDeviceListBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = bluetoothScanList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(bluetoothScanList[position])
    }

    fun connectingDialog(deviceName:String) {

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

    inner class MyViewHolder(private val binding: ItemDeviceListBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private lateinit var deviceModel: BluetoothScanModel

        fun bind(deviceModel: BluetoothScanModel) {
            this.deviceModel = deviceModel

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

        private fun setConnectedStatus(){
            binding.llStatus.visibility = View.VISIBLE
            binding.mcvIndicator.setCardBackgroundColor(context.getColor(R.color.green))
            binding.tvStatus.text = connected
            binding.tvStatus.setTextColor(context.getColor(R.color.green))
        }

        //Check if Device is already Connected or Not
        private fun checkTheDeviceConnection(){

            if (deviceModel.isConnected){
                setConnectedStatus()

            } else {
                binding.llStatus.visibility = View.GONE
            }

        }

        //Hide the Bin
        private fun hideBin(){
            binding.ivDelete.visibility = View.GONE
        }

        //setting the Name of the Scanned Device
        @SuppressLint("MissingPermission")
        private fun setName() {
            Utils.E(deviceModel.device.name.toString())
            binding.tvDeviceName.text = deviceModel.device.name.toString()
        }

        //setting the Mac Address of the Scanned Device
        private fun setMacAddress(){
            binding.tvMacAddress.text = deviceModel.device.address.toString()
        }

        // Method to initiate pairing with a Bluetooth device
        @SuppressLint("MissingPermission")
        fun pairDevice() {
            // Register for bond state changes
            val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            context.registerReceiver(bondStateReceiver, filter)

            // Start pairing process
            val pairingStarted = deviceModel.device.createBond()
            if (!pairingStarted) {
                Utils.E("Failed to initiate pairing process.")
            }
        }

        // BroadcastReceiver to handle bond state changes
        private val bondStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
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
                        Utils.E("Device paired successfully:  ${device.getName()}")
                        // Pairing successful, now connect

                        val connectToDeviceThread = ConnectToDeviceThread()
                        connectToDeviceThread.start()
                        context.unregisterReceiver(this)
                    } else if (bondState == BluetoothDevice.BOND_NONE) {
                        // Pairing failed or was cancelled
                        Utils.E("Pairing failed or was cancelled. ")

                        context.unregisterReceiver(this)
                    }
                }
            }
        }

        // Establish a connection with a Bluetooth device
        @SuppressLint("MissingPermission")
        private inner class ConnectToDeviceThread() : Thread() {

            val SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

            private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
                deviceModel.device.createRfcommSocketToServiceRecord(SERIAL_UUID)
            }

            override fun run() {
                // Cancel discovery because it otherwise slows down the connection.
//                bluetoothAdapter?.cancelDiscovery()

                mmSocket?.let { socket ->
                    // Connect to the remote device through the socket. This call blocks
                    // until it succeeds or throws an exception.
                    socket.connect()

                    binding.root.post {
                        // Update the UI here
                        // For example, show a dialog
                        connectingDialog(deviceModel.device.name)
                        setConnectedStatus()

                    }

                    Utils.E("Connected to device: ${deviceModel.device.getName()}")

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

                    Utils.E("Could not close the client socket\"")

                }
            }
        }

        //Handles All the Clicks
        @SuppressLint("MissingPermission")
        override fun onClick(v: View?) {

            if (v == binding.mcvDevice) {

                // Check if the device is paired

                if (deviceModel.device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    // Device is paired, you can establish a connection here
                    val connectToDeviceThread = ConnectToDeviceThread()
                    connectToDeviceThread.start()

                } else {
                    pairDevice()
                }

            }
        }

    }
}