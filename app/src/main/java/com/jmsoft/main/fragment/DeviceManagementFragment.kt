package com.jmsoft.main.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout.LayoutParams
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jmsoft.R
import com.jmsoft.Utility.UtilityTools.BluetoothUtils
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.BottomSheetAddDeviceBinding
import com.jmsoft.databinding.BottomSheetBluetoothScanListBinding
import com.jmsoft.databinding.DialogOpenSettingBinding
import com.jmsoft.databinding.FragmentDeviceManagementBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.BluetoothScanAdapter
import com.jmsoft.main.adapter.DeviceListAdapter
import com.jmsoft.main.`interface`.BluetoothOffCallback
import com.jmsoft.main.`interface`.ConnectedDeviceCallback
import com.jmsoft.main.`interface`.DeviceFoundCallback
import com.jmsoft.main.model.BluetoothScanModel
import com.jmsoft.main.model.DeviceModel

/**
 * DeviceManagement Fragment
 *
 * Showing the Connected Device list
 * Ask for bluetooth permission if require
 * Turn on bluetooth
 *
 */

class DeviceManagementFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentDeviceManagementBinding
    private var isReceiverRegistered = false
    private lateinit var bottomSheetBluetoothScan: BottomSheetDialog
    private var deviceType: String? = null
    private var isAddDeviceClicked = false
    private var addedDeviceList: ArrayList<DeviceModel> = ArrayList()

    @RequiresApi(Build.VERSION_CODES.S)
    val permissionsForVersionAbove11 = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private val permissionsForVersionBelow12 = arrayOf(

        Manifest.permission.BLUETOOTH,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH_ADMIN
    )

    // Bluetooth Intent for turn on the bluetooth
    private var bluetoothIntent: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == AppCompatActivity.RESULT_OK) {

                //if Add device clicked
                if (isAddDeviceClicked){
                    // Show Add device bottom sheet
                    addDeviceBottomSheet()
                    isAddDeviceClicked = false
                }
                else {

                    //set Up the Device list
                    setRecyclerOfDeviceList()
                }
            } else {
                Utils.T(requireActivity(), "Turn on Bluetooth for Scanning")
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment

        binding = FragmentDeviceManagementBinding.inflate(layoutInflater)

        //set the Clicks And initialization
        init()
        return binding.root
    }

    // Setting the Device list through Recycler View
    private fun setRecyclerOfDeviceList() {

        //get User UserId
        val userId = Utils.GetSession().userId

        //Checks if no devices data for this userId
        if (Utils.isNoDeviceForUser(userId!!)) {

            binding.rlNoDevice!!.visibility = View.VISIBLE
            binding.llDevicePresent!!.visibility = View.GONE

        } else {

            binding.rlNoDevice!!.visibility = View.GONE
            binding.llDevicePresent!!.visibility = View.VISIBLE

            //Devices list for particular user
            addedDeviceList = Utils.getDevicesThroughUserId(Utils.GetSession().userId!!)

            val deviceListAdapter = DeviceListAdapter(
                requireActivity(), addedDeviceList, binding.rlNoDevice!!, binding.llDevicePresent!!
            )
            binding.rvDeviceList?.layoutManager =
                LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            binding.rvDeviceList?.adapter = deviceListAdapter
            setDeviceStatus()

        }
    }
    private fun  setDeviceStatus() {

        BluetoothUtils.getConnectedDevice(context, object : ConnectedDeviceCallback {

            @SuppressLint("NotifyDataSetChanged")
            override fun onDeviceFound(device: ArrayList<BluetoothDevice>) {
                addedDeviceList.forEach { data ->
                    if (device.firstOrNull { it.address == data.deviceAddress } != null) {
                        data.isConnected = true

                    }

                }
                binding.rvDeviceList?.adapter?.notifyDataSetChanged()
            }

            override fun onDeviceNotFound() {
            }
        })
    }

    //Checks All the necessary permission related to bluetooth
    private var customPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var allPermissionsGranted = true // Flag to track permission status
        permissions.entries.forEach { entry ->
            val permission = entry.key
            val isGranted = entry.value
            if (!isGranted) {
                // If any permission is not granted, set the flag to false
                allPermissionsGranted = false
                // Permission is not granted
                // Handle the denied permission accordingly
                if (!shouldShowRequestPermissionRationale(permission)) {
                    // Permission denied ,Show Open Setting Dialog
                    showOpenSettingDialog()

                }
            } else {
                Utils.E(permission)
            }
        }

        // Check if all permissions are granted or not
        if (allPermissionsGranted) {

            //Checks Continuously Bluetooth is on or off
            BluetoothUtils.registerBluetoothStateReceiver(requireActivity(),
                object : BluetoothOffCallback {

                    override fun onBluetoothOff() {
                        bluetoothIntent.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    }
                })

            if (BluetoothUtils.isEnableBluetooth()) {

                //if Add device clicked
                if (isAddDeviceClicked){
                    // Show Add device bottom sheet
                    addDeviceBottomSheet()
                    isAddDeviceClicked = false
                }
                else {
                    //set Up the Device list
                    setRecyclerOfDeviceList()
                }

            } else {
                bluetoothIntent.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }
        }
    }

    private fun showOpenSettingDialog() {

        val dialog = Dialog(requireActivity())
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogOpenSettingBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.tvTitle.text = getString(R.string.permission_request)
        dialogBinding.tvMessage.text =
            getString(R.string.we_need_your_permission_to_access_bluetooth_and_location_services_in_order_to_provide_the_full_functionality_of_our_app)

        dialogBinding.mcvCancel.setOnClickListener {

            dialog.dismiss()
        }
        dialogBinding.mcvOpenSetting.setOnClickListener {

            dialog.dismiss()
            Utils.openAppSettings(requireActivity())
        }

        dialog.setCancelable(true)
        dialog.show()
    }

    private fun init() {

        //Checks the Android Version And  Launch Custom Permission ,according to Version
        checkAndroidVersionAndLaunchPermission()

        //set click on Add device button
        binding.mcvAddDevice?.setOnClickListener(this)
        //set click on add device button when no device is there
        binding.mcvAddFirstDevice?.setOnClickListener(this)
        binding.mcvNoDeviceBackBtn?.setOnClickListener(this)
        binding.mcvBackBtn?.setOnClickListener(this)
    }

    //Checks the Android Version And  Launch Custom Permission ,according to Version
    private fun checkAndroidVersionAndLaunchPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            customPermissionLauncher.launch(permissionsForVersionAbove11)
        } else {
            customPermissionLauncher.launch(permissionsForVersionBelow12)

        }
    }

    //Creating Add device Bottom Sheet Dialog
    private fun addDeviceBottomSheet() {

        val bottomSheetAddDevice = BottomSheetDialog(requireContext())
        val addDeviceBottomSheetBinding = BottomSheetAddDeviceBinding.inflate(layoutInflater)
        bottomSheetAddDevice.setCancelable(true)
        bottomSheetAddDevice.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetAddDevice.behavior.maxWidth = LayoutParams.MATCH_PARENT

        addDeviceBottomSheetBinding.mcvRfidScanner.setOnClickListener {
            deviceType = getString(R.string.rfid_scanner)
            bottomSheetAddDevice.dismiss()
            bluetoothScanBottomSheet()
        }

        addDeviceBottomSheetBinding.mcvRfidTagPrinter.setOnClickListener {

            deviceType = getString(R.string.rfid_tag_printer)
            bottomSheetAddDevice.dismiss()
            bluetoothScanBottomSheet()
        }

        addDeviceBottomSheetBinding.mcvTicketPrinter.setOnClickListener {

            deviceType = getString(R.string.ticket_printer)
            bottomSheetAddDevice.dismiss()
            bluetoothScanBottomSheet()
        }

        bottomSheetAddDevice.setContentView(addDeviceBottomSheetBinding.root)
        bottomSheetAddDevice.show()
    }

    //Creating Scan devices Bottom Sheet Dialog
    @SuppressLint("MissingPermission")
    private fun bluetoothScanBottomSheet() {

        bottomSheetBluetoothScan = BottomSheetDialog(requireContext())
        val bottomSheetBinding = BottomSheetBluetoothScanListBinding.inflate(layoutInflater)
        bottomSheetBluetoothScan.setCancelable(true)
        bottomSheetBluetoothScan.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBluetoothScan.behavior.maxWidth = LayoutParams.MATCH_PARENT
        bottomSheetBluetoothScan.setContentView(bottomSheetBinding.root)

        bottomSheetBluetoothScan.setOnDismissListener {
            //set up Recycler View
            setRecyclerOfDeviceList()
        }


        val bluetoothScanList = ArrayList<BluetoothScanModel>()
        val adapter = BluetoothScanAdapter(requireActivity(), bluetoothScanList, deviceType)
        bottomSheetBinding.rvBtScanList.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        bottomSheetBinding.rvBtScanList.adapter = adapter
        BluetoothUtils.getConnectedDevice(requireActivity(), object : ConnectedDeviceCallback {


            @SuppressLint("NotifyDataSetChanged")
            override fun onDeviceFound(device: ArrayList<BluetoothDevice>) {
                device.forEach {

                    bluetoothScanList.add(BluetoothScanModel(it, true))

                }
                adapter.notifyDataSetChanged()

            }

            override fun onDeviceNotFound() {

            }
        })

        //Set Up BroadCast Receiver For Bluetooth Scan

        BluetoothUtils.registerBroadCastReceiver(requireActivity(), object : DeviceFoundCallback {

            @SuppressLint("NotifyDataSetChanged")
            override fun onDeviceFound(device: BluetoothDevice) {

                if (!bluetoothScanList.contains(BluetoothScanModel(device, false))) {
                    bluetoothScanList.add(BluetoothScanModel(device, false))
//                    adapter.notifyItemRangeInserted(0,bluetoothScanList.size)
                    adapter.notifyDataSetChanged()
                }
            }
        })
        // flag for unregister the Receiver
        isReceiverRegistered = true

        //show bottom sheet
        bottomSheetBluetoothScan.show()
    }

    //Handles All the Clicks

    override fun onClick(v: View?) {

        // Handles Click on Add Device button
        if (v == binding.mcvAddDevice || v == binding.mcvAddFirstDevice) {

            isAddDeviceClicked = true
            //Checks the Android Version And  Launch Custom Permission ,according to Version
            checkAndroidVersionAndLaunchPermission()
        }else if (v == binding.mcvBackBtn || v == binding.mcvNoDeviceBackBtn) {

            (requireActivity() as DashboardActivity).navController?.popBackStack()
        }
    }

    override fun onDestroy() {

        super.onDestroy()

        if (isReceiverRegistered) {

            // Unregister the BroadcastReceiver when the activity is destroyed
            BluetoothUtils.unRegisterBroadCastReceiver(requireActivity())

            //Unregister Bluetooth State Receiver
            BluetoothUtils.unRegisterBluetoothStateReceiver(requireActivity())
        }
    }

}