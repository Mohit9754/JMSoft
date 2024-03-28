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
import com.jmsoft.basic.UtilityTools.Constants.Companion.rotation
import com.jmsoft.basic.UtilityTools.Utils

import com.jmsoft.databinding.ItemDeviceListBinding
import com.jmsoft.main.`interface`.DeviceConnectedCallback
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
        private val rotateAnimator = ObjectAnimator.ofFloat(binding.ivReconnect, rotation, 360f, 0f)

        //position of DeviceModel
        private var position: Int = 0

        //Device for reconnecting
        lateinit var device: BluetoothDevice

        fun bind(data: DeviceModel, position: Int) {
            this.deviceModel = data
            this.position = position

            //Getting device through MAC Address so that we can reconnect it.
            device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceModel.deviceAddress)

            //Setting Device Icon And Device Type
            setDeviceIcon()

            //Setting the Device Name
            setDeviceName()

            //Setting the Device MAC Address
            setDeviceMacAddress()

            //Setting the Device Status and Change there Status Color according to it.
            setDeviceStatus()

            //set click on delete icon
            binding.ivDelete.setOnClickListener(this)

            //set click on reconnect button
            binding.mcvReconnect.setOnClickListener(this)
        }

        //Setting Device Icon And Device Type
        private fun setDeviceIcon() {

            if (deviceModel.deviceType == context.getString(R.string.rfid_scanner)) {

                binding.ivDeviceIcon.setImageResource(R.drawable.icon_scanner)
                binding.tvDeviceType.text = context.getString(R.string.rfid_scanner)

            } else if (deviceModel.deviceType == context.getString(R.string.rfid_tag_printer)) {

                binding.ivDeviceIcon.setImageResource(R.drawable.icon_tag_printer)
                binding.tvDeviceType.text = context.getString(R.string.rfid_tag_printer)


            } else if (deviceModel.deviceType == context.getString(R.string.ticket_printer)) {

                binding.ivDeviceIcon.setImageResource(R.drawable.icon_ticket_printer)
                binding.tvDeviceType.text = context.getString(R.string.ticket_printer)

            }

        }

        //Setting the Device Name
        private fun setDeviceName() {

            binding.tvDeviceName.text = buildString {
                append(" (")
                append(deviceModel.deviceName)
                append(")")
            }
        }

        //Setting the Device MAC Address
        private fun setDeviceMacAddress() {
            binding.tvMacAddress.text = deviceModel.deviceAddress
        }

        //Setting the Device Status and Change there Status Color according to it.
        private fun setDeviceStatus() {

            //Check if device is connected or not
            if (deviceModel.isConnected) {
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
                Utils.deleteDeviceThoughDeviceUUID(deviceModel.deviceUUID!!)

                //Remove the device from the device list
                deviceList.removeAt(position)

                notifyDataSetChanged()

                // if the device list is empty then Show No device Status
                if (deviceList.isEmpty()) {

                    rlNoDevice.visibility = View.VISIBLE
                    llDevicePresent.visibility = View.GONE
                }

            // When reconnect button Clicks
            } else if (v == binding.mcvReconnect) {

                rotateAnimator.duration = 1000 // Duration in milliseconds
                rotateAnimator.repeatCount = ObjectAnimator.INFINITE // Repeat indefinitely
                rotateAnimator.interpolator = LinearInterpolator() // Linear interpolation
                rotateAnimator.start()

                //First UnPair the device then connect
                unPairDeviceAndConnect()
            }

        }

        //First UnPair the device then connect
        @SuppressLint("MissingPermission")
        private fun unPairDeviceAndConnect() {

            //UnPair the device
            BluetoothUtils.unpairDevice(device)
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

                            // Device is paired, you can establish a connection here
                            val connectToDeviceThread = BluetoothUtils.ConnectToDeviceThread(device,binding.root, object:

                                DeviceConnectedCallback {

                                override fun onDeviceConnected(device: BluetoothDevice) {
                                    onConnectSuccess()
                                    rotateAnimator.cancel()

                                }

                                override fun onDeviceNotConnected() {

                                }
                            })
                            connectToDeviceThread.start()
                        }

                        override fun pairFail() {

                            rotateAnimator.cancel()

                            Utils.T(context, context.getString(R.string.paired_failed))
                        }
                    })
                }
            }
            countDownTimer.start()
            
        }

        //This method is called when the device is reconnected
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

}