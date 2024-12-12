package com.jmsoft.main.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout.LayoutParams
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.godex.Godex
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jmsoft.R
import com.jmsoft.Utility.UtilityTools.BluetoothUtils
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Constants.Companion.rfid_Scanner
import com.jmsoft.basic.UtilityTools.Constants.Companion.rfid_tag_Printer
import com.jmsoft.basic.UtilityTools.Constants.Companion.ticket_Printer
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.basic.validation.ResultReturn
import com.jmsoft.basic.validation.Validation
import com.jmsoft.basic.validation.ValidationModel
import com.jmsoft.databinding.BottomSheetAddDeviceBinding
import com.jmsoft.databinding.BottomSheetBluetoothScanListBinding
import com.jmsoft.databinding.DialogOpenSettingBinding
import com.jmsoft.databinding.FragmentDeviceManagementBinding
import com.jmsoft.databinding.ItemPrinterConnectionBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.BluetoothScanAdapter
import com.jmsoft.main.adapter.DeviceListAdapter
import com.jmsoft.main.adapter.WIFIScanAdapter
import com.jmsoft.main.`interface`.BluetoothOffCallback
import com.jmsoft.main.`interface`.ConnectedDeviceCallback
import com.jmsoft.main.`interface`.DeviceFoundCallback
import com.jmsoft.main.model.BluetoothScanModel
import com.jmsoft.main.model.DeviceModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class DeviceManagementFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentDeviceManagementBinding

    // flag for unregister the Receiver
    private var isReceiverRegistered = false
    private lateinit var bottomSheetBluetoothScan: BottomSheetDialog
    private var deviceType: String? = null
    private var isAddDeviceClicked = false
    private var addedDeviceList: ArrayList<DeviceModel> = ArrayList()

    private var wifiScanList = ArrayList<ScanResult>()

    private lateinit var wifiManager: WifiManager
    private lateinit var connectivityManager: ConnectivityManager

    private val scanIntervalMillis = 30000L // 30 seconds

    private var wifiScanAdapter: WIFIScanAdapter? = null

    private var bottomSheetWIFIScan: BottomSheetDialog? = null
    private var wifiScanReceiver: BroadcastReceiver? = null

    private var isBluetoothReceiverRegistered = false

    // Permission for above 11 version
    @RequiresApi(Build.VERSION_CODES.S)
    val permissionsForVersionAbove11 = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE
    )

    // Permission for below 12 version
    private val permissionsForVersionBelow12 = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE
    )

    // Bluetooth Intent for turn on the bluetooth
    private var bluetoothIntent: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == AppCompatActivity.RESULT_OK) {

                //if Add device clicked
                if (isAddDeviceClicked) {
                    // Show Add device bottom sheet
                    addDeviceBottomSheet()
                    isAddDeviceClicked = false
                } else {
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

        lifecycleScope.launch(Dispatchers.Main) {
            init()
        }

        return binding.root
    }

    // Set the Device list through Recycler View
    private fun setRecyclerOfDeviceList() {

        //get User UserId
        val userId = Utils.GetSession().userUUID

        //Checks if no devices data for this userUUID
        if (Utils.isNoDeviceForUser(userId!!)) {

            binding.rlNoDevice!!.visibility = View.VISIBLE
            binding.llDevicePresent!!.visibility = View.GONE

        } else {

            binding.rlNoDevice!!.visibility = View.GONE
            binding.llDevicePresent!!.visibility = View.VISIBLE

            //Devices list for particular user
            addedDeviceList = Utils.getDevicesThroughUserUUID(Utils.GetSession().userUUID!!)

            val deviceListAdapter = DeviceListAdapter(
                requireActivity(), addedDeviceList, binding.rlNoDevice!!, binding.llDevicePresent!!
            )
            binding.rvDeviceList?.layoutManager =
                LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            binding.rvDeviceList?.adapter = deviceListAdapter
            setDeviceStatus()

        }
    }

    // Set the Status of the connected device
    private fun setDeviceStatus() {

        BluetoothUtils.getConnectedDevice(requireActivity(), object : ConnectedDeviceCallback {

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

    // Checks All the necessary permission related to bluetooth
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

            // Checks Continuously Bluetooth is on or off
            BluetoothUtils.registerBluetoothStateReceiver(requireActivity(),
                object : BluetoothOffCallback {

                    override fun onBluetoothOff() {
                        bluetoothIntent.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    }
                })

            if (BluetoothUtils.isEnableBluetooth(requireActivity())) {

                //if Add device clicked
                if (isAddDeviceClicked) {
                    // Show Add device bottom sheet
                    addDeviceBottomSheet()
                    isAddDeviceClicked = false
                } else {
                    //set Up the Device list
                    setRecyclerOfDeviceList()
                }

            } else {
                bluetoothIntent.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }
        }
    }

    // Open Setting Dialog
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

    // set the Clicks And initialization
    private suspend fun init() {

        // Initialize the WifiManager
        wifiManager =
            requireActivity().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        //Checks the Android Version And  Launch Custom Permission ,according to Version
        val job = lifecycleScope.launch(Dispatchers.Main) {
            checkAndroidVersionAndLaunchPermission()
        }

        // Set click on Add device button
        binding.mcvAddDevice?.setOnClickListener(this)

        // Set click on add device button when no device is there
        binding.mcvAddFirstDevice?.setOnClickListener(this)

        binding.mcvNoDeviceBackBtn?.setOnClickListener(this)

        binding.mcvBackBtn?.setOnClickListener(this)

        job.join()

        GetProgressBar.getInstance(requireActivity())?.dismiss()

    }

    // Function to start continuous Wi-Fi scanning using Coroutines
    @SuppressLint("MissingPermission")
    private fun startContinuousWifiScan() {
        wifiScanReceiver = object : BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            override fun onReceive(context: Context?, intent: Intent?) {
                val wifiScanResults: List<ScanResult> = wifiManager.scanResults
                handleScanResults(wifiScanResults)

                // Unregister after receiving scan results to avoid multiple registrations
                context?.unregisterReceiver(this)
                isBluetoothReceiverRegistered = false // Update the registration status
            }
        }

        // Use lifecycleScope to launch the coroutine
        viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                // Check if Fragment is added before proceeding
                if (isAdded) {
                    if (wifiManager.isWifiEnabled) {
                        // Register the receiver just before starting the scan
                        if (!isBluetoothReceiverRegistered) {
                            context?.registerReceiver(
                                wifiScanReceiver,
                                IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
                            )
                            isBluetoothReceiverRegistered = true // Update the registration status
                        }

                        val success = wifiManager.startScan()
                        if (!success) {
                            if (!Utils.isLocationEnabled(requireActivity())) {
                                Utils.openLocationSettings(requireActivity())
                            }
                        }
                    } else {
                        Utils.openWifiSettings(requireActivity())
                    }
                } else {
                    // If the Fragment is not added, exit the loop
                    break
                }
                delay(scanIntervalMillis) // Wait before the next scan
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()

        // Unregister the receiver only if it is registered
        if (isBluetoothReceiverRegistered) {
            context?.unregisterReceiver(wifiScanReceiver)
            isReceiverRegistered = false // Reset the registration status
        }

    }

    // Function to handle Wi-Fi scan results
    @SuppressLint("NotifyDataSetChanged")
    private fun handleScanResults(scanResults: List<ScanResult>) {

        for (result in scanResults) {

            val ssid = result.SSID
            val bssid = result.BSSID
            val signalLevel = result.level

            // Check if the BSSID is already in the list before adding
            if (wifiScanList.none { it.SSID == ssid }) {
                wifiScanList.add(result)
            }

            // Process or display the network details as needed
            Utils.E("scan data is  SSID: $ssid, BSSID: $bssid, Signal: $signalLevel")
        }

        CoroutineScope(Dispatchers.Main).launch {
            wifiScanAdapter?.notifyDataSetChanged()

            Utils.E("scan data ${wifiScanList.size} ${wifiScanAdapter == null}")

        }
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
            deviceType = rfid_Scanner
            bottomSheetAddDevice.dismiss()
            bluetoothScanBottomSheet()
        }

        addDeviceBottomSheetBinding.mcvRfidTagPrinter.setOnClickListener {

            deviceType = rfid_tag_Printer
            bottomSheetAddDevice.dismiss()
//            bluetoothScanBottomSheet()

            wifiScanBottomSheet()
        }

        addDeviceBottomSheetBinding.mcvTicketPrinter.setOnClickListener {

            deviceType = ticket_Printer
            bottomSheetAddDevice.dismiss()
            bluetoothScanBottomSheet()

//            showPrinterConnectionDialog()
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
        val adapter = BluetoothScanAdapter(
            requireActivity(),
            bluetoothScanList,
            deviceType,
            bottomSheetBluetoothScan
        )
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

    @SuppressLint("MissingPermission")
    private fun checkConnectedStatus(callBack: (WifiInfo?) -> Unit) {

        val cm = context?.getSystemService(ConnectivityManager::class.java)

        val activeNetwork = cm?.activeNetwork

        val netCaps = cm?.getNetworkCapabilities(activeNetwork)

        if (netCaps != null && netCaps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {

            // Get the transportInfo, which should be of type WifiInfo
            val wifiInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                netCaps.transportInfo as? WifiInfo
            } else {
                @Suppress("DEPRECATION") // Suppress deprecation warning for older versions
                wifiManager.connectionInfo
            }

            // If wifiInfo is not null, retrieve the SSID
            if (wifiInfo != null) {

                callBack(wifiInfo)


            } else {
                // Handle the case when WifiInfo is null

                callBack(null)

            }
        } else {
            // Handle the case when there is no active network or it's not Wi-Fi
            callBack(null)

        }
    }

    // Wifi Scan BottomSheet
    @SuppressLint("MissingPermission")
    private fun wifiScanBottomSheet() {

        bottomSheetWIFIScan = BottomSheetDialog(requireContext())
        val bottomSheetWIFIBinding = BottomSheetBluetoothScanListBinding.inflate(layoutInflater)
        bottomSheetWIFIScan?.setCancelable(true)
        bottomSheetWIFIScan?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetWIFIScan?.behavior?.maxWidth = LayoutParams.MATCH_PARENT
        bottomSheetWIFIScan?.setContentView(bottomSheetWIFIBinding.root)

        // val wifiInfo = wifiManager.connectionInfo

        checkConnectedStatus {

            Handler(Looper.getMainLooper()).post {

                wifiScanAdapter =
                    WIFIScanAdapter(requireActivity(), wifiScanList, it?.ssid, it?.bssid)
                bottomSheetWIFIBinding.rvBtScanList.layoutManager =
                    LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
                bottomSheetWIFIBinding.rvBtScanList.adapter = wifiScanAdapter

            }
        }

        //show bottom sheet
        // Check if the fragment is added and visible
        if (isAdded && isVisible) {
            // If the bottom sheet is already showing, dismiss it
            if (bottomSheetWIFIScan?.isShowing == true) {
                bottomSheetWIFIScan?.dismiss()
            }
            // Show the bottom sheet dialog
            bottomSheetWIFIScan?.show()
        }

        //Scanning start
        startContinuousWifiScan()

    }

    // Handles All the Clicks
    override fun onClick(v: View?) {

        // Handles Click on Add Device button
        if (v == binding.mcvAddDevice || v == binding.mcvAddFirstDevice) {

            isAddDeviceClicked = true
            //Checks the Android Version And  Launch Custom Permission ,according to Version
            checkAndroidVersionAndLaunchPermission()

        } else if (v == binding.mcvBackBtn || v == binding.mcvNoDeviceBackBtn) {

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

        // Dismiss the bottom sheet if it's showing
        bottomSheetWIFIScan?.dismiss()
        bottomSheetWIFIScan = null // Optional: Clear reference to prevent memory leaks

    }

}


