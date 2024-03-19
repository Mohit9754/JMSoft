package com.jmsoft.main.adapter

import android.annotation.SuppressLint
import android.app.Dialog
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
import com.jmsoft.basic.UtilityTools.Constants.Companion.active
import com.jmsoft.basic.UtilityTools.Constants.Companion.inActive
import com.jmsoft.databinding.ItemDeviceListBinding
import com.jmsoft.main.model.DeviceModel

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
        private lateinit var data: DeviceModel
        //postion of DeviceModel
        private var position: Int = 0

        fun bind(data: DeviceModel, position: Int) {
            this.data = data
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
            binding.ivDeviceIcon.setImageDrawable(data.deviceIcon)
        }

        //Setting the Device Name
        private fun setDeviceName() {
            binding.tvDeviceName.text = data.deviceName
        }

        //Setting the Device MAC Address
        private fun setDeviceMacAddress() {
            binding.tvMacAddress.text  = data.idNumber
        }

        //Setting the Device Status and Change there Staus Color Accourding to it.
        private fun setDeviceStatus() {

            if (data.status == active) {

                binding.mcvIndicator.setCardBackgroundColor(context.getColor(R.color.green))
                binding.tvStatus.text = data.status
                binding.tvStatus.setTextColor(context.getColor(R.color.green))
            } else if (data.status == inActive) {

                binding.mcvIndicator.setCardBackgroundColor(context.getColor(R.color.dark_red))
                binding.tvStatus.text = data.status
                binding.tvStatus.setTextColor(context.getColor(R.color.dark_red))
            }
        }

        //Handles All the Clicks
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {

            // Deleting the Device
            if (v == binding.ivDelete) {

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