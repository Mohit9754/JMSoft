package com.jmsoft.main.fragment

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.LinearInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jmsoft.R
import com.jmsoft.Utility.Database.CategoryDataModel
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.Utility.Database.StockLocationDataModel
import com.jmsoft.Utility.UtilityTools.BluetoothUtils
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.Utility.UtilityTools.RFIDSetUp
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Constants.Companion.frequencyData
import com.jmsoft.basic.UtilityTools.Constants.Companion.frequencyIndex
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.DialogOpenSettingBinding
import com.jmsoft.databinding.FragmentAuditBinding
import com.jmsoft.main.adapter.ExpectedAdapter
import com.jmsoft.main.adapter.ScannedAdapter
import com.jmsoft.main.adapter.UnknownAdapter
import com.jmsoft.main.`interface`.ConnectedDeviceCallback
import com.jmsoft.main.`interface`.PairStatusCallback
import com.rscja.deviceapi.entity.UHFTAGInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuditFragment : Fragment(), View.OnClickListener, RFIDSetUp.RFIDCallback {

    // Permission for above 11 version
    @RequiresApi(Build.VERSION_CODES.S)
    val permissionsForVersionAbove11 = arrayOf(

        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Permission for below 12 version
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

                checkConnectedDevice()

            } else {
                Utils.T(requireActivity(), getString(R.string.please_turn_on_bluetooth))
            }
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

            if (BluetoothUtils.isEnableBluetooth(requireActivity())) {

                checkConnectedDevice()


            } else {
                bluetoothIntent.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }
        }
    }

    private val binding by lazy { FragmentAuditBinding.inflate(layoutInflater) }

    private var rFIDSetUp: RFIDSetUp? = null

//    private var isRFIDScanning = false

    private var scannedProductList = ArrayList<ProductDataModel>()

    private var unKnownList = ArrayList<String>()

    private var adapterScanned: ScannedAdapter? = null

    private var adapterUnKnown: UnknownAdapter? = null

    private var adapterExpected: ExpectedAdapter? = null

    private var selectedStockLocationIndex = 0

    private var expectedProductList = ArrayList<ProductDataModel>()

    private var stockLocationDataList = ArrayList<StockLocationDataModel>()

    private val fValues = intArrayOf(
        0x01,
        0x02,
        0x04,
        0x08,
        0x16,
        0x32,
        0x33,
        0x34,
        0x35,
        0x36,
        0x37,
        0x80,
        0x3B
    )

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        lifecycleScope.launch(Dispatchers.Main) {
            init()
        }

        return binding.root

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

    private suspend fun setExpectedRecyclerView(stockLocationUUID: String) {

        val result = lifecycleScope.async(Dispatchers.IO) {
            return@async Utils.getAllProductsThatHasRFID(stockLocationUUID)
        }

        expectedProductList = result.await()

        if (expectedProductList.isEmpty())
            GetProgressBar.getInstance(requireActivity())?.dismiss()

        adapterExpected = ExpectedAdapter(requireContext(), expectedProductList)

        binding.rvExpected?.also {
            it.layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
            it.adapter = adapterExpected
        }

        binding.tvTotal?.text = expectedProductList.size.toString()

    }

    private fun setScannedRecyclerView() {

        adapterScanned = ScannedAdapter(requireContext(), scannedProductList)

        binding.rvScanned?.also {
            it.layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
            it.adapter = adapterScanned
        }

    }

    private fun setUnKnownRecyclerView() {

        adapterUnKnown = UnknownAdapter(requireActivity(), unKnownList)

        binding.rvUnknown?.also {
            it.layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
            it.adapter = adapterUnKnown
        }
    }

    override fun onPause() {
        super.onPause()

        rFIDSetUp?.onPause(object : PairStatusCallback {

            override fun pairSuccess() {

                lifecycleScope.launch {

                    changeToPlay()
                }

            }

            override fun pairFail() {
            }

        })
//        binding.ivScan?.setImageResource(R.drawable.icon_play)
    }

    override fun onDestroy() {
        super.onDestroy()
        rFIDSetUp?.onPause(object : PairStatusCallback {
            override fun pairSuccess() {

            }

            override fun pairFail() {
            }

        })
        binding.ivScan?.setImageResource(R.drawable.icon_play)

    }

    private suspend fun setStockLocationSpinner() {

        val result = lifecycleScope.async(Dispatchers.IO) {
            return@async Utils.getAllStockLocation()
        }

        stockLocationDataList = result.await()

        val listSpinner = mutableListOf<String?>()
        listSpinner.add(Constants.All)
        stockLocationDataList.map { it.stockLocationName }.let { listSpinner.addAll(it) }

        withContext(Dispatchers.Main) {

            val spinnerAdapter = ArrayAdapter(requireActivity(), R.layout.item_spinner, listSpinner)
            spinnerAdapter.setDropDownViewResource(R.layout.item_custom_spinner_list)
            binding.spinner?.adapter = spinnerAdapter

            binding.spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    selectedStockLocationIndex = position

                    lifecycleScope.launch(Dispatchers.Main) {
                        refresh()
                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Handle case when nothing is selected (optional)
                }
            }
        }
    }




    private suspend fun init() {

        rFIDSetUp = RFIDSetUp(requireActivity(), this)

        setStockLocationSpinner()

        val scannedJob = lifecycleScope.launch(Dispatchers.Main) {
            setScannedRecyclerView()
        }

        val unKnownJob = lifecycleScope.launch(Dispatchers.Main) {
            setUnKnownRecyclerView()
        }

        scannedJob.join()
        unKnownJob.join()

        GetProgressBar.getInstance(requireActivity())?.dismiss()

        binding.tvMissing?.text = expectedProductList.size.toString()
        binding.tvTotal?.text = expectedProductList.size.toString()

        binding.mcvScan?.setOnClickListener(this)

        binding.mcvRefresh?.setOnClickListener(this)

    }

    //Checks the Android Version And  Launch Custom Permission ,according to Version
    private fun checkAndroidVersionAndLaunchPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            customPermissionLauncher.launch(permissionsForVersionAbove11)
        } else {
            customPermissionLauncher.launch(permissionsForVersionBelow12)

        }
    }

    suspend fun changeToPause() {
        withContext(Dispatchers.Main) {
            GetProgressBar.getInstance(requireContext())?.dismiss()

            binding.ivScan?.setImageResource(R.drawable.icon_pause)
        }
    }

    suspend fun changeToPlay() {
        withContext(Dispatchers.Main) {
            GetProgressBar.getInstance(requireContext())?.dismiss()

            binding.ivScan?.setImageResource(R.drawable.icon_play)
        }
    }

    private fun checkConnectedDevice() {

        BluetoothUtils.getConnectedDevice(requireActivity(), object : ConnectedDeviceCallback {

            @SuppressLint("MissingPermission")
            override fun onDeviceFound(device: ArrayList<BluetoothDevice>) {


                Utils.E("Status is ${rFIDSetUp?.getScanningStatus()}")

                if (rFIDSetUp?.getScanningStatus() == true) {

                    Utils.E("Rfid device is not scanning")

                    GetProgressBar.getInstance(requireContext())?.show()

                    rFIDSetUp?.onPause(object : PairStatusCallback {

                        override fun pairSuccess() {

                            Utils.E("Status is success ${rFIDSetUp?.getScanningStatus()}")

                            lifecycleScope.launch {
                                changeToPlay()
                            }
                        }

                        override fun pairFail() {

                            Utils.E("Status is fail ${rFIDSetUp?.getScanningStatus()}")

                        }

                    })

                    if (rFIDSetUp?.getScanningStatus() == false) {

                        Utils.T(requireActivity(), "Scanning stopped")
                        binding.ivScan?.setImageResource(R.drawable.icon_play)
                    }

                } else {

                    Utils.E("Resume ")

                    GetProgressBar.getInstance(requireContext())?.show()

                    val sharedPreferences = requireActivity().getSharedPreferences(frequencyData, Context.MODE_PRIVATE)

                    val defaultValue = 0 // Default value if the key doesn't exist
                    val frequencyIndex = sharedPreferences.getInt(frequencyIndex, defaultValue)

                    rFIDSetUp?.onResume(device[0].address,frequencyIndex, object : PairStatusCallback {

                        override fun pairSuccess() {


                            lifecycleScope.launch {
                                changeToPause()
                            }

//                            Utils.T(requireActivity(),"Scanning started")


                        }

                        override fun pairFail() {

                            GetProgressBar.getInstance(requireContext())?.dismiss()


                            binding.ivScan?.setImageResource(R.drawable.icon_play)

                        }

                    })


                }
            }

            override fun onDeviceNotFound() {

                Utils.T(requireActivity(), "No device is Connected ")
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    suspend fun refresh() {

        withContext(Dispatchers.Main) {

            GetProgressBar.getInstance(requireContext())?.show()

            binding.ivScan?.setImageResource(R.drawable.icon_play)
            binding.tvSelected?.text = requireActivity().getString(R.string._0)
            binding.tvUnknown?.text = requireActivity().getString(R.string._0)

            unKnownList.clear()
            scannedProductList.clear()

//            lifecycleScope.launch(Dispatchers.Main) {
//                init()
//            }

            val stockLocationUUID =
                if (selectedStockLocationIndex == 0) Constants.All else stockLocationDataList[selectedStockLocationIndex - 1].stockLocationUUID

            val expectedJob = lifecycleScope.launch(Dispatchers.Main) {
                stockLocationUUID?.let { setExpectedRecyclerView(it) }
            }

            adapterUnKnown?.notifyDataSetChanged()
            adapterScanned?.notifyDataSetChanged()

            expectedJob.join()

            GetProgressBar.getInstance(requireActivity())?.dismiss()

            binding.tvMissing?.text = expectedProductList.size.toString()
            binding.tvTotal?.text = expectedProductList.size.toString()

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onClick(v: View?) {

        if (v == binding.mcvScan) {

            //Checks the Android Version And  Launch Custom Permission ,according to Version
            lifecycleScope.launch(Dispatchers.Main) {
                checkAndroidVersionAndLaunchPermission()
            }
        } else if (v == binding.mcvRefresh) {

            Utils.E("Status is ... ${rFIDSetUp?.getScanningStatus()}")

//            GetProgressBar.getInstance(requireContext())?.show()

            if (rFIDSetUp?.getScanningStatus() == true) {
                rFIDSetUp?.onPause(object : PairStatusCallback {

                    override fun pairSuccess() {

                        lifecycleScope.launch {
                            refresh()
                            refresh()

                            Utils.E("refresh is ... ${rFIDSetUp?.getScanningStatus()}")

                        }
                    }

                    override fun pairFail() {

                        Utils.E("fail is ... ${rFIDSetUp?.getScanningStatus()}")

                        lifecycleScope.launch {
                            changeToPlay()

                        }
                    }
                })

            } else {

                lifecycleScope.launch {
                    refresh()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onTagRead(tagInfo: UHFTAGInfo) {

        val stockLocationUUID = if (selectedStockLocationIndex == 0) Constants.All else (stockLocationDataList[selectedStockLocationIndex-1].stockLocationUUID)?:Constants.All

        if (Utils.isRFIDExist(tagInfo.epc,Constants.All) == true) {

            if (Utils.isRFIDExist(tagInfo.epc,stockLocationUUID) == true) {

                val productDataModel = Utils.getProductThroughRFIDCode(tagInfo.epc,stockLocationUUID)

                val result =
                    scannedProductList.any { it.productRFIDCode == productDataModel.productRFIDCode }

                if (!result) {

                    val isRemoved = expectedProductList.removeIf { it.productRFIDCode == tagInfo.epc }

                    if (isRemoved) {

                        Utils.E(
                            " removed from expected list ${
                                expectedProductList.contains(
                                    productDataModel
                                )
                            }"
                        )

                        scannedProductList.add(0, productDataModel)
                        adapterScanned?.notifyItemInserted(0)
                        adapterExpected?.notifyDataSetChanged()

                        binding.tvSelected?.text = scannedProductList.size.toString()

                    } else {

                        Utils.E("not removed from expected list")

                    }

//                adapterScanned?.notifyDataSetChanged()

                }

            }

        } else {

            if (!unKnownList.contains(tagInfo.epc)) {

                unKnownList.add(0, tagInfo.epc)
                adapterUnKnown?.notifyItemInserted(0)
                binding.tvUnknown?.text = unKnownList.size.toString()
                binding.tvTotal?.text =
                    (unKnownList.size + scannedProductList.size + expectedProductList.size).toString()
                // adapterScanned?.notifyDataSetChanged()
            }
        }

        binding.tvMissing?.text = expectedProductList.size.toString()
    }

    override fun onError(message: String) {
        // Handle errors
//        Utils.T(requireContext(), message)

    }

}
