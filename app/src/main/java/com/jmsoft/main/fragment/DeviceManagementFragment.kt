package com.jmsoft.main.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.jmsoft.databinding.FragmentDeviceManagementBinding
import com.jmsoft.databinding.ItemAddDeviceBinding
import com.jmsoft.databinding.ItemBluetoothScanListBinding
import com.jmsoft.main.adapter.BluetoothScanAdapter
import com.jmsoft.main.adapter.DeviceListAdapter
import com.jmsoft.main.`interface`.ConnectedDeviceCallback
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
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var receiver: BroadcastReceiver
    private var isReceiverRegistered = false



    // Bluetooth Intent for turn on the bluetooth
    private var bluetoothIntent: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == AppCompatActivity.RESULT_OK) {

                bluetoothScanBottomSheet()
            }
        }

    private val permissionsRequestCodeForBluetooth = 100 // You can use any value for the request code

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment

        binding = FragmentDeviceManagementBinding.inflate(layoutInflater)

        //set the Clicks And initalize
        init()
        return binding.root
    }

    // Setting the Device list through Recycler View
    private fun setRecyclerOfDeviceList() {

        val list = ArrayList<DeviceModel>()

        list.add(
            DeviceModel(
                requireActivity().getDrawable(R.drawable.icon_scanner)!!,
                "RFID scanner",
                "123456",
                "Active"
            )
        )
        list.add(
            DeviceModel(
                requireActivity().getDrawable(R.drawable.icon_ticket_printer)!!,
                "Ticket printer",
                "123456",
                "Inactive"
            )
        )
        list.add(
            DeviceModel(
                requireActivity().getDrawable(R.drawable.icon_tag_printer)!!,
                "RFID tag printer",
                "123456",
                "Active"
            )
        )

        if (list.isEmpty()) {

            binding.rlNoDevice!!.visibility = View.VISIBLE
            binding.llDevicePresent!!.visibility = View.GONE

        } else {

            binding.rlNoDevice!!.visibility = View.GONE
            binding.llDevicePresent!!.visibility = View.VISIBLE

            val deviceListAdapter = DeviceListAdapter(
                requireActivity(), list,
                binding.rlNoDevice!!, binding.llDevicePresent!!
            )
            binding.rvDeviceList?.layoutManager =
                LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            binding.rvDeviceList?.adapter = deviceListAdapter
        }
    }

    private fun init() {

        //set Up the Device list
        setRecyclerOfDeviceList()
        //set click on Add device button
        binding.mcvAddDevice?.setOnClickListener(this)
        //set click on add device button when no device is there
        binding.mcvAddFirstDevice?.setOnClickListener(this)
    }

    //Creating Add device Bottom Sheet Dialog
    private fun addDeviceBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetBinding = ItemAddDeviceBinding.inflate(layoutInflater)
        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.behavior.maxWidth = LayoutParams.MATCH_PARENT
        bottomSheetDialog.setContentView(bottomSheetBinding.root)
        bottomSheetDialog.show()
    }

    //Creating Scan devices Bottom Sheet Dialog
    private fun bluetoothScanBottomSheet() {

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetBinding = ItemBluetoothScanListBinding.inflate(layoutInflater)
        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.behavior.maxWidth = LayoutParams.MATCH_PARENT
        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        val bluetoothScanList = ArrayList<BluetoothScanModel>()
        BluetoothUtils.getConnectedDevice(requireActivity(), object :ConnectedDeviceCallback{
            override fun onDeviceFound(device: BluetoothDevice) {
                bluetoothScanList.add(BluetoothScanModel(device,true))
            }
        })

        val adapter = BluetoothScanAdapter(requireActivity(), bluetoothScanList)
        bottomSheetBinding.rvBtScanList.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        bottomSheetBinding.rvBtScanList.adapter = adapter

        // Register BroadcastReceiver for Bluetooth device discovery

        // Create a BroadcastReceiver for ACTION_FOUND.
        receiver = object : BroadcastReceiver() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (BluetoothDevice.ACTION_FOUND == action) {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                    if (BluetoothUtils.checksForAccessCoarseLocationPermission(requireActivity(),permissionsRequestCodeForBluetooth)){

                        if (device != null && device.name != null) {
                            bluetoothScanList.add(BluetoothScanModel(device,false))
                            adapter.notifyDataSetChanged()

                        }
                    }
                }
            }
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)

        //Register Receiver
        requireActivity().registerReceiver(receiver, filter)
        isReceiverRegistered = true
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothAdapter.startDiscovery()

        //show bottom sheet
        bottomSheetDialog.show()
    }

    //Handles All the Clicks
    override fun onClick(v: View?) {

        // Handles Click on Add Device button
        if (v == binding.mcvAddDevice || v == binding.mcvAddFirstDevice) {

            // Check if Bluetooth permission has or not

            if (BluetoothUtils.hasBluetoothPermissions(requireActivity())) {

                //Check if Bluetooth is enable or not
                if (BluetoothUtils.isEnableBluetooth()) {

                    bluetoothScanBottomSheet()

                } else {
                    //Enable the Bluetooth
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    bluetoothIntent.launch(enableBtIntent)
                }
            }

            else {

                //Request for permission
                BluetoothUtils.requestPermissions(
                    requireActivity(),
                    permissionsRequestCodeForBluetooth
                )
            }
        }
    }

    // Runtime Bluetooth permission result
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray,
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if (requestCode == permissionsRequestCode) {
//            // Check if all permissions are granted
//            var allPermissionsGranted = true
//            for (result in grantResults) {
//                if (result != PackageManager.PERMISSION_GRANTED) {
//                    allPermissionsGranted = false
//                    break
//                }
//            }
//            if (allPermissionsGranted) {
//                // All permissions are granted, proceed with your logic
//                // For example, start Bluetooth functionality
//
//                if (BluetoothUtils.isEnableBluetooth()) {
//
//                    addDeviceBottomSheet()
//
//
//                } else {
//                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//                    bluetoothIntent.launch(enableBtIntent)
//                }
//            } else {
//
//                Utils.T(activity, "Please allow the Permission To connect with your Device")
//                // Permissions are not granted, handle the scenario
//                // For example, show a message to the user or disable Bluetooth functionality
//            }
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the BroadcastReceiver when the activity is destroyed
        if (isReceiverRegistered) {
            requireActivity().unregisterReceiver(receiver)
        }
    }

}