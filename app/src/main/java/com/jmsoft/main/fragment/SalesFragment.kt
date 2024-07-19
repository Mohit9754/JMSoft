package com.jmsoft.main.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.Utility.Database.OrderDataModel
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.DialogOpenSettingBinding
import com.jmsoft.databinding.FragmentSalesBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.ConfirmOrderAdapter
import com.jmsoft.main.adapter.NewOrderAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SalesFragment : Fragment(),View.OnClickListener {

    private val binding by lazy { FragmentSalesBinding.inflate(layoutInflater) }

    private var confirmOrderList = ArrayList<OrderDataModel>()

    private var newOrderList = ArrayList<OrderDataModel>()

    private var searchFilterList = ArrayList<OrderDataModel>()

    private var getConfirmOrder = true

    private var isNewOrderStatus = true

    private var newOrderAdapter:NewOrderAdapter? = null

    private var confirmOrderAdapter:ConfirmOrderAdapter? = null

    // Permission for External Storage
    private val permissionsForExternalStorage = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    // Initialize your permission result launcher
    @RequiresApi(Build.VERSION_CODES.R)
    val storagePermissionResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            // Check if permission is granted
            if (Environment.isExternalStorageManager()) {

                // Permission granted. Now resume your workflow.
                // Call your method or handle the permission granted state here
//                generatePDF()
            }
            else {

                requestStoragePermission()
            }
        }
        else if (result.resultCode == Activity.RESULT_CANCELED) {

            // Handle case where user cancels the permission request
            if (Environment.isExternalStorageManager()) {

                // Permission granted. Now resume your workflow.
                // Call your method or handle the permission granted state here
//                generatePDF()
            }
            else {

                requestStoragePermission()
            }
        }
    }

    // Checks All the necessary permission related to External Storage
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

            // Generate pdf
//            generatePDF()

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

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
            getString(R.string.we_need_your_permission_to_access_storage_services_in_order_to_provide_the_full_functionality_of_our_app_your_cooperation_is_appreciated)
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

    // Set confirm order recycler view
    private  fun setConfirmOrderRecyclerView() {

        if (confirmOrderList.isNotEmpty()) {

            binding.rvConfirmOrder?.visibility = View.VISIBLE

            confirmOrderAdapter = ConfirmOrderAdapter(requireActivity(),confirmOrderList)
            binding.rvConfirmOrder?.layoutManager = GridLayoutManager(context, 3) // 3 is the number of columns
            binding.rvConfirmOrder?.adapter = confirmOrderAdapter

        }
        else {

            GetProgressBar.getInstance(requireActivity())?.dismiss()

            binding.rvNewOrder?.visibility = View.GONE
            binding.llEmptySales?.visibility = View.VISIBLE
        }
    }

    // Set new order recycler view
    private  fun setNewOrderRecyclerView() {

        if (newOrderList.isNotEmpty()) {

            newOrderAdapter = NewOrderAdapter(requireActivity(),newOrderList)
            binding.rvNewOrder?.layoutManager = GridLayoutManager(context, 4) // 3 is the number of columns
            binding.rvNewOrder?.adapter = newOrderAdapter

        }
        else {

            GetProgressBar.getInstance(requireActivity())?.dismiss()

            binding.rvConfirmOrder?.visibility = View.GONE
            binding.rvNewOrder?.visibility = View.GONE
            binding.llEmptySales?.visibility = View.VISIBLE
        }
    }

    private suspend fun getConfirmOrderList() {

        val result = lifecycleScope.async(Dispatchers.IO) {
            return@async Utils.GetSession().userUUID?.let { Utils.getOrders(it,Constants.Confirm) }
        }

        this.confirmOrderList  = result.await()!!

        GetProgressBar.getInstance(requireActivity())?.dismiss()

        setConfirmOrderRecyclerView()

    }

    private suspend fun getNewOrderList() {

        val result = lifecycleScope.async(Dispatchers.IO) {
            return@async Utils.GetSession().userUUID?.let { Utils.getOrders(it,Constants.New) }
        }

        this.newOrderList  = result.await()!!

    }

    // Method to request MANAGE_EXTERNAL_STORAGE permission
    @RequiresApi(Build.VERSION_CODES.R)
    fun requestStoragePermission() {

        val packageName = requireContext().packageName
        val uri = Uri.parse("package:$packageName")

        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
        storagePermissionResultLauncher.launch(intent)
    }

    private fun requestPermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

            // For Android 10 and below, request traditional storage permissions
            customPermissionLauncher.launch(permissionsForExternalStorage)

        } else {

            // For Android 11 and above, request manage external storage permission
            if (Environment.isExternalStorageManager()) {

//                generatePDF()

            } else {

                requestStoragePermission()

            }
        }

    }

    override fun onResume() {
        super.onResume()
        getConfirmOrder = true
        binding.etSearch?.setText("")

    }


    // Set search
    private fun setSearch() {

        binding.etSearch?.addTextChangedListener( object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                searchFilterList.clear()

                if (binding.etSearch?.text?.isNotEmpty() == true) {

                    for (orderData in if (isNewOrderStatus) newOrderList else confirmOrderList) {

//                      Utils.E("Size of list new is ${newOrderList.size}")
                        val productUUIDList = orderData.productUUIDUri?.split(",")

                        val addressDataModel = orderData.addressUUID?.let {
                            Utils.getAddressThroughAddressUUID(
                                it
                            )
                        }

                        if (productUUIDList != null) {

                            for (productUUID in productUUIDList) {

                                val productDataModel = Utils.getProductThroughProductUUID(productUUID)

                                val metalTypeName = productDataModel.metalTypeUUID?.let {
                                    Utils.getMetalTypeNameThroughMetalTypeUUID(
                                        it
                                    )
                                }

                                if (productDataModel.productName?.contains(
                                        binding.etSearch?.text.toString().trim(),
                                        true
                                    ) == true
                                    ||
                                    productDataModel.productDescription?.contains(
                                        binding.etSearch?.text.toString().trim(),
                                        true
                                    ) == true
                                    ||
                                    productDataModel.productOrigin?.contains(
                                        binding.etSearch?.text.toString().trim(),
                                        true
                                    ) == true
                                    ||
                                    productDataModel.productRFIDCode?.contains(
                                        binding.etSearch?.text.toString().trim(),
                                        true
                                    ) == true
                                    ||
                                    productDataModel.productBarcodeData?.contains(
                                        binding.etSearch?.text.toString().trim(),
                                        true
                                    ) == true
                                    ||
                                    orderData.orderNo?.contains(
                                        binding.etSearch?.text.toString().trim(),
                                        true
                                    ) == true
                                    ||
                                    orderData.date?.contains(
                                        binding.etSearch?.text.toString().trim(),
                                        true
                                    ) == true
                                    ||
                                    addressDataModel?.firstName?.contains(
                                        binding.etSearch?.text.toString().trim(),
                                        true
                                    ) == true
                                    ||
                                    addressDataModel?.lastName?.contains(
                                        binding.etSearch?.text.toString().trim(),
                                        true
                                    ) == true
                                    ||
                                    addressDataModel?.address?.contains(
                                        binding.etSearch?.text.toString().trim(),
                                        true
                                    ) == true
                                    ||
                                    metalTypeName?.contains(
                                        binding.etSearch?.text.toString().trim(),
                                        true
                                    ) == true

                                ) {

                                    if(!searchFilterList.contains(orderData)) {

                                        searchFilterList.add(orderData)

                                    }
                                }
                            }
                        }
                    }

                    if (searchFilterList.isNotEmpty()) {

//                        binding.mcvProductList?.visibility = View.VISIBLE
                        binding.llEmptySales?.visibility = View.GONE

                        Utils.E("Size of list is ${searchFilterList.size}")

                        if (isNewOrderStatus)
                            newOrderAdapter?.filterProductDataList(searchFilterList)
                        else
                            confirmOrderAdapter?.filterProductDataList(searchFilterList)

                    }
                    else {

                        if (isNewOrderStatus)
                            newOrderAdapter?.filterProductDataList(searchFilterList)
                        else
                            confirmOrderAdapter?.filterProductDataList(searchFilterList)

                        binding.llEmptySales?.visibility = View.VISIBLE


//                        binding.llEmptyProduct?.visibility = View.VISIBLE
                    }
                }

                else {
                    removeSearch()
                }

            }

            override fun afterTextChanged(s: Editable?) {}

        })
    }

    private fun removeSearch() {

        binding.llEmptySales?.visibility = View.GONE

        if (isNewOrderStatus)
            newOrderAdapter?.filterProductDataList(newOrderList)
        else
            confirmOrderAdapter?.filterProductDataList(confirmOrderList)
    }

    private suspend fun init() {

        // set new order recycler view
        val job = lifecycleScope.launch(Dispatchers.IO) {
            getNewOrderList()
        }

        job.join()

        binding.etSearch?.let {
            binding.mcvSearch?.let { it1 ->
                Utils.setFocusChangeListener(requireActivity(),
                    it, it1
                )
            }
        }

        setSearch()

        setNewOrderRecyclerView()

        requestPermission()

        binding.mcvBackBtn?.setOnClickListener(this)

        binding.mcvConfirmOrder?.setOnClickListener(this)

        binding.mcvNewOrder?.setOnClickListener(this)

    }

    @SuppressLint("ResourceAsColor")
    private fun makeSelected(materialCardView: MaterialCardView,textView: TextView) {

        materialCardView.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.theme))
        textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))

    }

    @SuppressLint("ResourceAsColor")
    private fun makeUnSelected(materialCardView: MaterialCardView, textView: TextView) {

        materialCardView.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.separator_line_color))
        textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.product_background_color))
    }

    override fun onClick(v: View?) {

        if (v == binding.mcvBackBtn) {
            (requireActivity() as DashboardActivity).navController?.popBackStack(R.id.home, false)
        }

        else if (v == binding.mcvNewOrder) {

            isNewOrderStatus = true
            removeSearch()
            binding.etSearch?.setText("")


            binding.mcvNewOrder?.let { binding.tvNewOrder?.let { it1 -> makeSelected(it, it1) } }

            binding.mcvConfirmOrder?.let { binding.tvConfirmOrder?.let { it1 ->
                makeUnSelected(it,
                    it1
                )
            } }

            binding.llEmptySales?.visibility = View.GONE
//            binding.rvNewOrder?.visibility  = View.VISIBLE
            binding.rvConfirmOrder?.visibility  = View.GONE

            if (newOrderList.isNotEmpty()) {

                binding.rvNewOrder?.visibility  = View.VISIBLE
//                binding.rvConfirmOrder?.visibility  = View.GONE
            }
            else {

                binding.llEmptySales?.visibility  = View.VISIBLE
            }
        }

        else if (v == binding.mcvConfirmOrder) {

            isNewOrderStatus = false
            removeSearch()
            binding.etSearch?.setText("")

            binding.mcvConfirmOrder?.let { binding.tvConfirmOrder?.let { it1 -> makeSelected(it, it1) } }

            binding.mcvNewOrder?.let { binding.tvNewOrder?.let { it1 ->
                makeUnSelected(it,
                    it1
                )
            } }

            binding.llEmptySales?.visibility = View.GONE
//            binding.rvConfirmOrder?.visibility  = View.VISIBLE
            binding.rvNewOrder?.visibility  = View.GONE

            if (getConfirmOrder) {

                GetProgressBar.getInstance(requireActivity())?.show()

                lifecycleScope.launch(Dispatchers.Main) {
                    getConfirmOrderList()
                }

                getConfirmOrder = false
            }
            else {

                if (confirmOrderList.isNotEmpty()) {

                    binding.rvConfirmOrder?.visibility  = View.VISIBLE
                }
                else {

                    binding.rvConfirmOrder?.visibility  = View.GONE
                    binding.llEmptySales?.visibility  = View.VISIBLE
                }

            }

        }

    }

}