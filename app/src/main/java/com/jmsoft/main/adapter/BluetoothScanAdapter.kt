package com.jmsoft.main.adapter

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.Utility.Database.DeviceDataModel
import com.jmsoft.Utility.UtilityTools.BluetoothUtils
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Constants.Companion.connected
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.ItemDeviceListBinding
import com.jmsoft.main.`interface`.DeviceConnectedCallback
import com.jmsoft.main.`interface`.PairStatusCallback
import com.jmsoft.main.model.BluetoothScanModel

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
    private val deviceType: String?,
    private val bottomSheetBluetoothScan:BottomSheetDialog
) : RecyclerView.Adapter<BluetoothScanAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = ItemDeviceListBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = bluetoothScanList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(bluetoothScanList[position], position)
    }

    //Connecting Dialog
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
        private val rotateAnimator = ObjectAnimator.ofFloat(binding.ivReconnect,
            Constants.rotation, 360f, 0f)

        fun bind(deviceModel: BluetoothScanModel, position: Int) {
            this.deviceModel = deviceModel
            this.position = position

            //setting the Name of the Scanned Device
            setName()

            //setting the Mac Address of the Scanned Device
            setMacAddress()

            //Setting Device Icon
            setDeviceIcon()

            //Hide the Bin
            hideBin()

            //Check if Device is already Connected or Not
            checkTheDeviceConnection()

            //Setting Click on Device
            binding.mcvDevice.setOnClickListener(this)

        }

        //Setting Device Icon
        private fun setDeviceIcon() {

            when (deviceType) {

                context.getString(R.string.rfid_scanner) -> {

                    binding.ivDeviceIcon.setImageResource(R.drawable.icon_scanner)
                }
                context.getString(R.string.rfid_tag_printer) -> {

                    binding.ivDeviceIcon.setImageResource(R.drawable.icon_tag_printer)
                }
                context.getString(R.string.ticket_printer) -> {

                    binding.ivDeviceIcon.setImageResource(R.drawable.icon_ticket_printer)
                }
            }
        }

        // Method to move the Connected to the first position
        private fun moveToFirstPosition() {

            if (position != 0) {

                bluetoothScanList.remove(bluetoothScanList[position])
                bluetoothScanList.add(
                    0,
                    bluetoothScanList[position]
                ) // Add item to the beginning of the list
                notifyItemMoved(position, 0) // Notify adapter about the move

            }
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
            binding.tvDeviceName.visibility = View.GONE
            binding.mcvReconnect.visibility = View.GONE
            binding.tvDeviceType.text = deviceModel.device.name.toString()
        }

        //setting the Mac Address of the Scanned Device
        private fun setMacAddress() {
            binding.tvMacAddress.text = deviceModel.device.address.toString()
        }

        //This method is called when the device is connected
        private fun onConnectSuccess() {

            //showing connected dialog
            connectingDialog(deviceModel.device.name)

            //setting the connected status
            setConnectedStatus()

            // Move the Connected Device to First Position
            //  moveToFirstPosition()

            Utils.E("Connected to device: ${deviceModel.device.name}")

            //get userUUID
            val userUUID = Utils.GetSession().userUUID

            val deviceDataMode = DeviceDataModel()

            deviceDataMode.deviceUUID = Utils.generateUUId() //Generating the UUID for device
            deviceDataMode.deviceType = deviceType
            deviceDataMode.deviceName = deviceModel.device.name
            deviceDataMode.deviceAddress = deviceModel.device.address
            deviceDataMode.userUUID = userUUID

            val addedDeviceList = Utils.getDevicesThroughUserUUID(Utils.GetSession().userUUID!!)
            if (addedDeviceList.firstOrNull { it.deviceAddress == deviceModel.device.address } != null) {

            }
            else {
                //Insert Data in the device table
                Utils.insertNewDeviceData(deviceDataMode)
            }

            rotateAnimator.cancel()
            binding.mcvReconnect.visibility = View.GONE

            //Dismiss Bluetooth Scan Bottom Sheet
            bottomSheetBluetoothScan.dismiss()
        }

        //Checks Device is paired or not and connect it
        @SuppressLint("MissingPermission")
        private fun checkPairAndConnectDevice() {

            // Check if the device is paired
            if (BluetoothUtils.isDevicePaired(deviceModel.device)) {

                //Unpair the device
                BluetoothUtils.unpairDevice(deviceModel.device)

                val countDownTimer = object : CountDownTimer(2000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        // This method will be called every 1 second (1000 milliseconds) until the countdown is finished

                    }

                    override fun onFinish() {
                        // This method will be called when the countdown is finished

                        // Execute your Bluetooth pairing code here
                        BluetoothUtils.pairDevice(context, deviceModel.device, object : PairStatusCallback {

                            override fun pairSuccess() {

                                // Device is paired, you can establish a connection here
                                val connectToDeviceThread = BluetoothUtils.ConnectToDeviceThread(deviceModel.device,binding.root,object:DeviceConnectedCallback{

                                    override fun onDeviceConnected(device: BluetoothDevice) {
                                        onConnectSuccess()
                                    }

                                    override fun onDeviceNotConnected() {

                                    }

                                })
                                connectToDeviceThread.start()
                            }

                            override fun pairFail() {

                                rotateAnimator.cancel()
                                binding.mcvReconnect.visibility = View.GONE

                                Utils.T(context, "Paired Failed")
                            }
                        })
                    }
                }
                countDownTimer.start()

                Utils.E("Device is already paired")

            } else {

                //Make Pair Device
                BluetoothUtils.pairDevice(context, deviceModel.device, object : PairStatusCallback {

                    override fun pairSuccess() {

                        // Device is paired, you can establish a connection here
                        val connectToDeviceThread = BluetoothUtils.ConnectToDeviceThread(deviceModel.device,binding.root,object:DeviceConnectedCallback{

                            override fun onDeviceConnected(device: BluetoothDevice) {
                                onConnectSuccess()
                            }

                            override fun onDeviceNotConnected() {

                            }
                        })
                        connectToDeviceThread.start()
                    }
                    override fun pairFail() {
                        Utils.T(context, "Paired Failed")

                        rotateAnimator.cancel()
                        binding.mcvReconnect.visibility = View.GONE

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

                    binding.mcvReconnect.visibility = View.VISIBLE
                    rotateAnimator.duration = 1000 // Duration in milliseconds
                    rotateAnimator.repeatCount = ObjectAnimator.INFINITE // Repeat indefinitely
                    rotateAnimator.interpolator = LinearInterpolator() // Linear interpolation
                    rotateAnimator.start()

                    //Checks Device is paired or not and connect it
                    checkPairAndConnectDevice()

                } else {

                    Utils.T(context, context.getString(R.string.already_connected))
                    //showing connected dialog
                    connectingDialog(deviceModel.device.name)
                    //Dismiss the Bottom Sheet
                    bottomSheetBluetoothScan.dismiss()

                }

            }
        }
    }
}
