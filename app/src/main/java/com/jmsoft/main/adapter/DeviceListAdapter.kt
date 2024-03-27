package com.jmsoft.main.adapter

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.Utility.UtilityTools.BluetoothUtils
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils

import com.jmsoft.databinding.ItemDeviceListBinding
import com.jmsoft.main.`interface`.PairStatusCallback
import com.jmsoft.main.model.DeviceModel
import java.io.IOException
import java.util.UUID


/**
 * Device List Adapter
 *
 * Showing the list of Devices
 * Removing the device
 *
 */
class DeviceListAdapter(
    private val context: Context,
    private val deviceList: ArrayList<DeviceModel>,
    private val rlNoDevice: RelativeLayout,
    private val llDevicePresent: LinearLayout,

    ) : RecyclerView.Adapter<DeviceListAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemDeviceListBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = deviceList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.bind(deviceList[position], position)
    }


    inner class MyViewHolder(private var binding: ItemDeviceListBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        // Store DeviceModel data
        private lateinit var deviceModel: DeviceModel
        val rotateAnimator = ObjectAnimator.ofFloat(binding.ivReconnect, "rotation", 360f, 0f)


        //postion of DeviceModel
        private var position: Int = 0
        lateinit var device: BluetoothDevice

        fun bind(data: DeviceModel, position: Int) {
            this.deviceModel = data
            this.position = position
            device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceModel.deviceAddress)

            setDeviceIcon()
            setDeviceName()
            setDeviceMacAddress()
            setDeviceStatus(data)

            //set click on delete icon
            binding.ivDelete.setOnClickListener(this)
            binding.mcvReconnect.setOnClickListener(this)
        }

        //Setting Device Icon
        private fun setDeviceIcon() {

            if (deviceModel.deviceType == context.getString(R.string.rfid_scanner)) {

                binding.ivDeviceIcon.setImageResource(R.drawable.icon_scanner)
            } else if (deviceModel.deviceType == context.getString(R.string.rfid_tag_printer)) {

                binding.ivDeviceIcon.setImageResource(R.drawable.icon_tag_printer)
            } else if (deviceModel.deviceType == context.getString(R.string.ticket_printer)) {

                binding.ivDeviceIcon.setImageResource(R.drawable.icon_ticket_printer)
            }

//            binding.ivDeviceIcon.setImageDrawable(data.deviceIcon)
        }

        //Setting the Device Name
        private fun setDeviceName() {
            binding.tvDeviceType.text = deviceModel.deviceType
            binding.tvDeviceName.text = buildString {
                append(" (")
                append(deviceModel.deviceName)
                append(")")
            }


//            binding.tvDeviceName.text = "${deviceModel.deviceType} (${deviceModel.deviceName})"
        }

        //Setting the Device MAC Address
        private fun setDeviceMacAddress() {
            binding.tvMacAddress.text = deviceModel.deviceAddress
        }

        //Setting the Device Status and Change there Staus Color Accourding to it.
        private fun setDeviceStatus(data: DeviceModel) {

            if (data.isConnected) {
                binding.mcvIndicator.setCardBackgroundColor(context.getColor(R.color.green))
                binding.tvStatus.text = context.getString(R.string.active)
                binding.tvStatus.setTextColor(context.getColor(R.color.green))
                binding.mcvReconnect.visibility = View.GONE
            } else {

                binding.mcvIndicator.setCardBackgroundColor(context.getColor(R.color.dark_red))
                binding.tvStatus.text = context.getString(R.string.inactive)
                binding.tvStatus.setTextColor(context.getColor(R.color.dark_red))
                binding.mcvReconnect.visibility = View.VISIBLE

            }
        }

        //Handles All the Clicks
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            // Deleting the Device
            if (v == binding.ivDelete) {

                //Deleting the device from the  device table
                Utils.deleteDeviceThoughDeviceId(deviceModel.deviceId!!)
                deviceList.removeAt(position)
                notifyDataSetChanged()

                if (deviceList.isEmpty()) {

                    rlNoDevice.visibility = View.VISIBLE
                    llDevicePresent.visibility = View.GONE
                }
            } else if (v == binding.mcvReconnect) {
                rotateAnimator.duration = 1000 // Duration in milliseconds
                rotateAnimator.repeatCount = ObjectAnimator.INFINITE // Repeat indefinitely
                rotateAnimator.interpolator = LinearInterpolator() // Linear interpolation
                rotateAnimator.start()
                checkPairAndConnectDevice()
            }

        }

        @SuppressLint("MissingPermission")
        private fun checkPairAndConnectDevice() {
            // Device is paired, you can establish a connection here

            unpairDevice(device)
//
            val countDownTimer = object : CountDownTimer(2000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    // This method will be called every 1 second (1000 milliseconds) until the countdown is finished

                }

                override fun onFinish() {
                    // This method will be called when the countdown is finished

                    // Execute your Bluetooth pairing code here
                    BluetoothUtils.pairDevice(context, device, object : PairStatusCallback {
                        override fun pairSuccess() {
                            val connectToDeviceThread = ConnectToDeviceThread()
                            connectToDeviceThread.start()
                        }

                        override fun pairFail() {
                            rotateAnimator.cancel()

                            Utils.T(context, "Paired Failed")
                        }
                    })
                }
            }

            countDownTimer.start()


            Utils.E("Device is already paired")

        }

        @SuppressLint("MissingPermission")

        private inner class ConnectToDeviceThread : Thread() {

            private val SERIAL_UUID = UUID.fromString(Constants.bluetoothUuid)

            private val mmSocket: BluetoothSocket? =
                device.createRfcommSocketToServiceRecord(SERIAL_UUID)


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
                        binding.root.post {
                            rotateAnimator.cancel()
                            // Update the UI here
                            // For example, show a dialog
                        }
                    }
                    // The connection attempt succeeded. Perform work associated with
                    // the connection in a separate thread.
//                    manageMyConnectedSocket(socket)
                }
            }

            // Closes the client socket and causes the thread to finish.
            fun cancel() {
                rotateAnimator.cancel()
                try {
                    mmSocket?.close()
                } catch (e: IOException) {
                    Utils.E("Could not close the client socket")
                }
            }
        }

        @SuppressLint("MissingPermission")
        private fun onConnectSuccess() {
            Utils.E("Connected to Device ::::: ${device.name}")

            rotateAnimator.cancel()
            binding.mcvIndicator.setCardBackgroundColor(context.getColor(R.color.green))
            binding.tvStatus.text = context.getString(R.string.active)
            binding.tvStatus.setTextColor(context.getColor(R.color.green))
            binding.mcvReconnect.visibility = View.GONE
        }
    }


    @SuppressLint("MissingPermission")
    private fun unpairDevice(device: BluetoothDevice) {
        try {
            val method = device.javaClass.getMethod("removeBond")
            method.invoke(device)
            Utils.E("Device unpaired successfully: ${device.name}")
        } catch (e: Exception) {
            Utils.E("Failed to unpair device: ${e.message}")
        }
    }
}