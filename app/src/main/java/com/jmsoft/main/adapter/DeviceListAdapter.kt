package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.Utility.UtilityTools.BluetoothUtils
import com.jmsoft.basic.UtilityTools.Utils

import com.jmsoft.databinding.ItemDeviceListBinding
import com.jmsoft.main.`interface`.ConnectedDeviceCallback
import com.jmsoft.main.model.BluetoothScanModel
import com.jmsoft.main.model.DeviceModel
import okio.Utf8

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


    inner class MyViewHolder(private val binding: ItemDeviceListBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        // Store DeviceModel data
        private lateinit var deviceModel: DeviceModel
        //postion of DeviceModel
        private var position: Int = 0

        fun bind(data: DeviceModel, position: Int) {
            this.deviceModel = data
            this.position = position

            setDeviceIcon()
            setDeviceName()
            setDeviceMacAddress()
            setDeviceStatus()

            //set click on delete icon
            binding.ivDelete.setOnClickListener(this)
        }

        //Setting Device Icon
        private fun setDeviceIcon() {

            if (deviceModel.deviceName == context.getString(R.string.rfid_scanner)){

                binding.ivDeviceIcon.setImageResource(R.drawable.icon_scanner)
            }
            else if(deviceModel.deviceName == context.getString(R.string.rfid_tag_printer)){

                binding.ivDeviceIcon.setImageResource(R.drawable.icon_tag_printer)
            }
            else if(deviceModel.deviceName == context.getString(R.string.ticket_printer)){

                binding.ivDeviceIcon.setImageResource(R.drawable.icon_ticket_printer)
            }

//            binding.ivDeviceIcon.setImageDrawable(data.deviceIcon)
        }

        //Setting the Device Name
        private fun setDeviceName() {
            binding.tvDeviceName.text = deviceModel.deviceName
        }

        //Setting the Device MAC Address
        private fun setDeviceMacAddress() {
            binding.tvMacAddress.text  = deviceModel.deviceAddress
        }

        //Setting the Device Status and Change there Status Color according to it.
        private fun setDeviceStatus() {

            BluetoothUtils.getConnectedDevice(context, object : ConnectedDeviceCallback {

                override fun onDeviceFound(device: BluetoothDevice) {

                    binding.mcvIndicator.setCardBackgroundColor(context.getColor(R.color.green))
                    binding.tvStatus.text = context.getString(R.string.active)
                    binding.tvStatus.setTextColor(context.getColor(R.color.green))

                }

                override fun onDeviceNotFound() {

                  binding.mcvIndicator.setCardBackgroundColor(context.getColor(R.color.dark_red))
                  binding.tvStatus.text = context.getString(R.string.inactive)
                  binding.tvStatus.setTextColor(context.getColor(R.color.dark_red))

                }
            })
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
            }

        }

    }
}