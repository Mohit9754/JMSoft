package com.jmsoft.main.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.Utility.database.ContactDataModel
import com.jmsoft.Utility.database.ProductDataModel
import com.jmsoft.Utility.database.PurchasingDataModel
import com.jmsoft.Utility.UtilityTools.BluetoothUtils
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.Utility.UtilityTools.RFIDSetUp
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.basic.validation.ResultReturn
import com.jmsoft.basic.validation.Validation
import com.jmsoft.basic.validation.ValidationModel
import com.jmsoft.databinding.DialogOpenSettingBinding
import com.jmsoft.databinding.FragmentAddPurchaseBinding
import com.jmsoft.databinding.ItemAddSupplierBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.AdapterSelectedProductPurchasing
import com.jmsoft.main.adapter.SupplierDropDownAdapter
import com.jmsoft.main.`interface`.ConnectedDeviceCallback
import com.jmsoft.main.`interface`.PairStatusCallback
import com.jmsoft.main.`interface`.SelectedCallback
import com.rscja.deviceapi.entity.UHFTAGInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Suppress("DEPRECATION")
class AddPurchaseFragment : Fragment(), View.OnClickListener, SelectedCallback,
    RFIDSetUp.RFIDCallback {

    private lateinit var binding: FragmentAddPurchaseBinding

    private var supplierList = ArrayList<ContactDataModel>()

    private var productNameDropDownAdapter: SupplierDropDownAdapter? = null

    private var selectedSupplierUUID: String? = null

    private var purchasingDataModel: PurchasingDataModel? = null

    private var selectedProductUUIDList = ArrayList<String>()

    private var selectedProductImageList = ArrayList<String>()

    private var selectedProductNameList = ArrayList<String>()

    private var selectedProductRFIDList = ArrayList<String>()

    private var selectedProductWeightList = ArrayList<String>()

    private var selectedProductPriceList = ArrayList<String>()

    private var productDataModelList = ArrayList<ProductDataModel>()

    private var isAddStatus = true

    private var isProfileSelected = false

    private var addSupplierBinding: ItemAddSupplierBinding? = null

    private var editProfileDialog: Dialog? = null

    private var addSupplierDialog: Dialog? = null

    private var supplierName: String? = null
    private var date: String? = null

    // for Opening the Camera Dialog
    private var forCameraSettingDialog = 100

    // for Opening the Gallery Dialog
    private var forGallerySettingDialog = 200

    private var rfidSetUp: RFIDSetUp? = null

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

    private var etRFIDCode: EditText? = null

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

    // Gallery Permission Launcher
    private var galleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean? ->

        if (isGranted == true) {

            editProfileDialog?.dismiss()
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryActivityResultLauncher?.launch(galleryIntent)

        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)) {

                    editProfileDialog?.dismiss()
                    showOpenSettingDialog(forGallerySettingDialog)
                }
            } else {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    editProfileDialog?.dismiss()
                    showOpenSettingDialog(forGallerySettingDialog)
                }
            }
        }
    }

    // Camera Permission Launcher
    private var cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean? ->
        if (isGranted == true) {
            editProfileDialog?.dismiss()

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraActivityResultLauncher.launch(cameraIntent)
        } else {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                editProfileDialog?.dismiss()
                showOpenSettingDialog(forCameraSettingDialog)
            }
        }
    }

    // Gallery result  Launcher
    private var galleryActivityResultLauncher: ActivityResultLauncher<Intent?>? =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri: Uri? = result.data?.data
                addSupplierBinding?.ivProfile?.setImageURI(imageUri)
                isProfileSelected = true

            }
        }

    //Camera result Launcher
    private var cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            addSupplierBinding?.ivProfile?.setImageBitmap(result.data?.extras?.get("data") as Bitmap?)
            isProfileSelected = true
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentAddPurchaseBinding.inflate(layoutInflater)

        init()

        return binding.root
    }

    // Open Setting Dialog
    fun showOpenSettingDialog(dialogCode: Int) {

        val dialog = Dialog(requireActivity())
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogOpenSettingBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.tvTitle.text =
            if (dialogCode == forCameraSettingDialog) {

                getString(R.string.camera_permission_denied)
            } else {
                getString(R.string.photo_library_permission_denied)
            }
        dialogBinding.tvMessage.text =
            if (dialogCode == forCameraSettingDialog) {

                getString(R.string.camera_access_is_needed_in_order_to_capture_profile_picture_please_enable_it_from_the_settings)
            } else {
                getString(R.string.photo_library_access_is_needed_in_order_to_access_media_to_be_used_in_the_app_please_enable_it_from_the_settings)
            }

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

    // open setting dialog
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

    // set focus change listener
    private fun setFocusChangeListener() {

        binding.etOrderNo?.let {
            binding.mcvOrderNo?.let { it1 ->
                Utils.setFocusChangeLis(
                    requireActivity(), it,
                    it1
                )
            }
        }
    }

    // set text change listener
    private fun setTextChangeListener() {

        binding.etOrderNo?.let {
            binding.tvOrderNoError?.let { it1 ->
                Utils.setTextChangeListener(
                    it,
                    it1
                )
            }
        }
    }

    // set supplier drop down
    @SuppressLint("SetTextI18n")
    private suspend fun setSupplierDropDown() {

        val result = lifecycleScope.async(Dispatchers.IO) {
            return@async Utils.GetSession().userUUID?.let { Utils.getAllContactThroughUserUUID(it) }
                ?: ArrayList()
        }

        supplierList = result.await()

        productNameDropDownAdapter = SupplierDropDownAdapter(
            requireActivity(),
            supplierList,
            this
        )

        if (selectedSupplierUUID != null) {

            val index = supplierList.indexOfFirst { it.contactUUID == selectedSupplierUUID }

            productNameDropDownAdapter?.selectedSupplierPosition = index

            val contactDataModel =
                supplierList[index].contactUUID?.let { Utils.getContactByUUID(it) }
            binding.tvSupplierError?.text =
                "${contactDataModel?.firstName} ${contactDataModel?.lastName}"

        }

        binding.rvSupplier?.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        binding.rvSupplier?.adapter = productNameDropDownAdapter

    }

    // check add or edit status
    @SuppressLint("SetTextI18n")
    fun checkAddOrEditStatus() {

        val purchasingUUID = arguments?.getString(Constants.purchasingUUID)

        if (purchasingUUID != null) {

            binding.tvTitle?.text = getString(R.string.update_purchase)

            isAddStatus = false

            purchasingDataModel = Utils.getPurchaseByUUID(purchasingUUID)

            binding.etOrderNo?.setText(purchasingDataModel?.orderNo)
            selectedSupplierUUID = purchasingDataModel?.supplierUUID

            val contactDataModel = purchasingDataModel?.supplierUUID?.let {
                Utils.getContactByUUID(
                    it
                )
            }

            binding.tvSupplier?.text =
                "${contactDataModel?.firstName} ${contactDataModel?.lastName}"

            binding.tvDate?.text = purchasingDataModel?.date

            if (purchasingDataModel?.purchaseStatus == Constants.confirm) {

                binding.etOrderNo?.isEnabled = false
                binding.mcvAddProduct?.visibility = View.GONE
                binding.mcvSave?.visibility = View.GONE

            } else {
                binding.mcvDate?.setOnClickListener(this)
                binding.llSupplier?.setOnClickListener(this)
            }

        } else {

            binding.mcvDate?.setOnClickListener(this)
            binding.llSupplier?.setOnClickListener(this)

            binding.tvSupplier?.text = supplierName
            binding.tvDate?.text = date
        }
    }

    // set selected product recycler view
    private suspend fun setSelectedProductRecyclerView() {

        selectedProductUUIDList = ArrayList(Utils.SelectedProductUUIDList.getProductList())

        Utils.E("List size is purchasing ${Utils.SelectedProductUUIDList.getSize()}")

        Utils.SelectedProductUUIDList.clearList()

        val result = lifecycleScope.async(Dispatchers.IO) {
            return@async setProductList()
        }

        result.await()

        if (productDataModelList.isNotEmpty()) {

            binding.nsvProduct?.visibility = View.VISIBLE
            binding.rlEmpty?.visibility = View.GONE

            val adapter = AdapterSelectedProductPurchasing(
                requireActivity(),
                productDataModelList,
                binding,
                isAddStatus,
                selectedProductUUIDList,
                purchasingDataModel?.purchaseStatus
            ) { editText -> etRFIDCode = editText; checkAndroidVersionAndLaunchPermission() }

            binding.rvProduct?.layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)

            binding.rvProduct?.adapter = adapter

        } else {

            GetProgressBar.getInstance(requireActivity())?.dismiss()

            binding.nsvProduct?.visibility = View.GONE
            binding.rlEmpty?.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()

        checkAddOrEditStatus()

        lifecycleScope.launch {
            setSelectedProductRecyclerView()
        }

    }

    override fun onPause() {
        super.onPause()
        rfidSetUp?.onPause(object : PairStatusCallback {
            override fun pairSuccess() {

            }

            override fun pairFail() {
            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()

        rfidSetUp?.onPause(object : PairStatusCallback {
            override fun pairSuccess() {

            }

            override fun pairFail() {
            }

        })

    }

    private fun init() {

        //Initialize RFID
        rfidSetUp = RFIDSetUp(requireContext(), this)

        setFocusChangeListener()

        setTextChangeListener()

        lifecycleScope.launch {
            setSupplierDropDown()
        }


        binding.mcvBackBtn?.setOnClickListener(this)

        binding.mcvSave?.setOnClickListener(this)

        binding.mcvAddProduct?.setOnClickListener(this)

        binding.mcvAddSupplier?.setOnClickListener(this)

        GetProgressBar.getInstance(requireActivity())?.dismiss()

    }

    // Show or hide Product name drop down
    private fun showOrHideSupplierDropDown() {

        if (binding.mcvSupplierList?.visibility == View.VISIBLE) {

            binding.ivSupplier.let { it?.let { it1 -> Utils.rotateView(it1, 0f) } }
            binding.mcvSupplierList.let { it?.let { it1 -> Utils.collapseView(it1) } }

        } else {

            binding.ivSupplier.let { it?.let { it1 -> Utils.rotateView(it1, 180f) } }
            binding.mcvSupplierList.let { it?.let { it1 -> Utils.expandView(it1) } }
        }
    }

    // date dilog
    private fun showDateDialog() {

        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireActivity(),
            { _, year, month, dayOfMonth ->
                // Create a Calendar object with the selected date
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)

                // Format the selected date as a string (e.g., "dd/MM/yyyy")
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
                val selectedDate = dateFormat.format(selectedCalendar.time)

                // Now you can use the selectedDate as needed
                binding.tvDate?.text = selectedDate
                date = selectedDate
                binding.tvDateError?.visibility = View.GONE

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()

    }

    // set product list
    private fun setProductList(): Boolean {

        productDataModelList.clear()

        val productUUIDList = purchasingDataModel?.productUUIDs?.split(",") ?: listOf()

        for (productUUID in selectedProductUUIDList) {
            // Check if the productUUID is already in productUUIDList
            if (productUUID !in productUUIDList) {
                val productDataModel = Utils.getProductThroughProductUUID(productUUID)
                productDataModelList.add(productDataModel)
            }
        }

        if (!isAddStatus) {

            val imageList = purchasingDataModel?.productImageUri?.split(",") ?: listOf()
            val productNameList = purchasingDataModel?.productNames?.split(",") ?: listOf()
            val productRFIDList = purchasingDataModel?.productRFIDs?.split(",") ?: listOf()
            val productWeightList = purchasingDataModel?.productWeights?.split(",") ?: listOf()
            val productPriceList = purchasingDataModel?.productPrices?.split(",") ?: listOf()

            for (i in imageList.indices) {

                val productDataModel = ProductDataModel()

                productDataModel.productImageUri = imageList[i]
                productDataModel.productName = productNameList[i]
                productDataModel.productRFIDCode = productRFIDList[i]
                productDataModel.productWeight = productWeightList[i].toDouble()
                productDataModel.productPrice = productPriceList[i].toDouble()
                selectedProductUUIDList.add(productUUIDList[i])

                productDataModelList.add(productDataModel)

            }
        }

        return true
    }

    // store image in internal storage
    private fun storeImageInInternalStorage(): Boolean {

        for (product in productDataModelList) {

            val imageList = product.productImageUri?.split(",")?.toList() ?: listOf()

            val imageUri = if (imageList[0] == Constants.Default_Image) {
                imageList[0]
            } else {
                val imageUri = Utils.generateUUId()
                val bitmap = Utils.getImageFromInternalStorage(requireActivity(), imageList[0])
                bitmap?.let { Utils.saveToInternalStorage(requireActivity(), it, imageUri) }
                imageUri
            }

            if (product.productName != null && product.productRFIDCode != null && product.productWeight != null && product.productPrice != null) {

                selectedProductImageList.add(imageUri)
                selectedProductNameList.add(product.productName!!)
                selectedProductRFIDList.add(product.productRFIDCode!!)
                selectedProductWeightList.add(product.productWeight!!.toString())
                selectedProductPriceList.add(product.productPrice!!.toString())

            }
        }

        return true
    }

    // add or edit purchase
    private suspend fun addOrEditPurchase() {

        val result = lifecycleScope.async(Dispatchers.IO) {
            return@async storeImageInInternalStorage()
        }

        result.await()

        val purchaseDataModel = PurchasingDataModel()

        purchaseDataModel.purchasingUUID =
            if (purchasingDataModel != null) purchasingDataModel!!.purchasingUUID else Utils.generateUUId()
        purchaseDataModel.orderNo = binding.etOrderNo?.text.toString().trim()
        purchaseDataModel.supplierUUID = selectedSupplierUUID
        purchaseDataModel.date = binding.tvDate?.text.toString()
        purchaseDataModel.totalAmount = Utils.removeThousandSeparators(
            binding.tvTotalPrice?.text.toString().replace("DH", "").trim()
        ).toDouble()

        purchaseDataModel.productImageUri =
            selectedProductImageList.joinToString(",").replace(" ", "")

        purchaseDataModel.productNames = selectedProductNameList.joinToString(",").replace(" ", "")

        purchaseDataModel.productRFIDs = selectedProductRFIDList.joinToString(",").replace(" ", "")

        purchaseDataModel.productWeights =
            selectedProductWeightList.joinToString(",").replace(" ", "")

        purchaseDataModel.productPrices =
            selectedProductPriceList.joinToString(",").replace(" ", "")

        purchaseDataModel.productUUIDs =
            selectedProductUUIDList.joinToString(",").replace(" ", "")

        if (purchasingDataModel != null) {

            Utils.updatePurchase(purchaseDataModel)
            Utils.T(requireActivity(), getString(R.string.purchase_updated_successfully))

        } else {

            purchaseDataModel.purchaseStatus = Constants.pending

            Utils.addPurchase(purchaseDataModel)
            Utils.T(requireActivity(), getString(R.string.purchase_added_successfully))

        }

        (requireActivity() as DashboardActivity).navController?.popBackStack()

    }

    // validate puchase detail
    private fun validatePurchaseDetail() {

        val errorValidationModel: MutableList<ValidationModel> = ArrayList()

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.Empty, binding.etOrderNo, binding.tvOrderNoError
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.EmptyTextView, binding.tvSupplier, binding.tvSupplierError
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.EmptyTextView, binding.tvDate, binding.tvDateError
            )
        )

        val validation: Validation? = Validation.instance

        val resultReturn: ResultReturn? =
            validation?.CheckValidation(requireActivity(), errorValidationModel)

        if (resultReturn?.aBoolean == true) {

            if (productDataModelList.isNotEmpty()) {

                GetProgressBar.getInstance(requireActivity())?.show()

                lifecycleScope.launch {
                    addOrEditPurchase()
                }

            } else {

                Utils.T(requireActivity(), getString(R.string.please_add_at_least_one_product))
            }

        } else {

            GetProgressBar.getInstance(requireActivity())?.dismiss()

            resultReturn?.errorTextView?.visibility = View.VISIBLE

            if (resultReturn?.type === Validation.Type.EmptyString) {
                resultReturn.errorTextView?.text = resultReturn.errorMessage

            } else {

                resultReturn?.errorTextView?.text = validation?.errorMessage
                val animation =
                    AnimationUtils.loadAnimation(requireActivity(), R.anim.top_to_bottom)
                resultReturn?.errorTextView?.startAnimation(animation)

                validation?.EditTextPointer?.requestFocus()

                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(validation?.EditTextPointer, InputMethodManager.SHOW_IMPLICIT)

                validation?.EditTextPointer = null

            }
        }
    }

    // add supplier
    private suspend fun addSupplier() {

        val contactDataModel = ContactDataModel()

        contactDataModel.contactUUID = Utils.generateUUId()

        contactDataModel.firstName = addSupplierBinding?.etFirstName?.text.toString().trim()
        contactDataModel.lastName = addSupplierBinding?.etLastName?.text.toString().trim()
        contactDataModel.phoneNumber = addSupplierBinding?.etPhoneNumber?.text.toString().trim()
        contactDataModel.emailAddress =
            addSupplierBinding?.etEmail?.text.toString().trim().toLowerCase(Locale.ROOT)
        contactDataModel.type = addSupplierBinding?.tvType?.text.toString().trim()
        contactDataModel.userUUID = Utils.GetSession().userUUID

        val result = lifecycleScope.async(Dispatchers.IO) {

            Utils.insertContact(contactDataModel)

            return@async true
        }

        result.await()

        setSupplierDropDown()

        showOrHideSupplierDropDown()

        GetProgressBar.getInstance(requireActivity())?.dismiss()

    }

    // validate supplier detail
    private fun validateSupplierDetail() {

        val errorValidationModels: MutableList<ValidationModel> = ArrayList()

        errorValidationModels.add(
            ValidationModel(
                Validation.Type.Empty,
                addSupplierBinding?.etFirstName,
                addSupplierBinding?.tvFirstNameError
            )
        )

        errorValidationModels.add(
            ValidationModel(
                Validation.Type.Empty,
                addSupplierBinding?.etLastName,
                addSupplierBinding?.tvLastNameError
            )
        )

        errorValidationModels.add(
            ValidationModel(
                Validation.Type.Phone,
                addSupplierBinding?.etPhoneNumber,
                addSupplierBinding?.tvPhoneNumberError
            )
        )

        errorValidationModels.add(
            ValidationModel(
                Validation.Type.Email,
                addSupplierBinding?.etEmail,
                addSupplierBinding?.tvEmailError
            )
        )

        errorValidationModels.add(
            ValidationModel(
                Validation.Type.EmptyTextView,
                addSupplierBinding?.tvType,
                addSupplierBinding?.tvTypeError
            )
        )

        val validation: Validation? = Validation.instance
        val resultReturn: ResultReturn? =
            validation?.CheckValidation(requireActivity(), errorValidationModels)
        if (resultReturn?.aBoolean == true) {

            val phoneNumber = addSupplierBinding?.etPhoneNumber?.text.toString().trim()
            val emailAddress =
                addSupplierBinding?.etEmail?.text.toString().trim().toLowerCase(Locale.ROOT)

            // Check if phone Number exist
            if (!Utils.isPhoneNumberExistInContactTable(phoneNumber)) {

                if (!Utils.isEmailExistInContactTable(emailAddress)) {

                    addSupplierDialog?.dismiss()

                    GetProgressBar.getInstance(requireActivity())?.show()

                    // Add new Contact
                    lifecycleScope.launch(Dispatchers.Main) {

                        addSupplier()
                    }

                } else {

                    addSupplierBinding?.let {
                        Utils.showError(
                            requireActivity(),
                            it.tvEmailError,
                            requireActivity().getString(R.string.email_already_exist)
                        )
                    }

                }

            } else {

                addSupplierBinding?.let {
                    Utils.showError(
                        requireActivity(),
                        it.tvPhoneNumberError,
                        requireActivity().getString(R.string.mobile_number_already_exist)
                    )
                }
            }

        } else {
            resultReturn?.errorTextView?.visibility = View.VISIBLE
            if (resultReturn?.type === Validation.Type.EmptyString) {
                resultReturn.errorTextView?.text = resultReturn.errorMessage
            } else {
                resultReturn?.errorTextView?.text = validation?.errorMessage
                val animation =
                    AnimationUtils.loadAnimation(context, R.anim.top_to_bottom)
                resultReturn?.errorTextView?.startAnimation(animation)

                validation?.EditTextPointer?.requestFocus()

                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(validation?.EditTextPointer, InputMethodManager.SHOW_IMPLICIT)

            }
        }
    }

    // show or hide type drip down
    private fun showOrHideTypeDropDown() {

        if (addSupplierBinding?.mcvTypeList?.visibility == View.VISIBLE) {

            addSupplierBinding?.ivType.let { it?.let { it1 -> Utils.rotateView(it1, 0f) } }

            if (addSupplierBinding?.mcvTypeList != null) {
                Utils.collapseView(addSupplierBinding?.mcvTypeList!!)
            }

        } else {

            addSupplierBinding?.ivType?.let { Utils.rotateView(it, 180f) }
            addSupplierBinding?.mcvTypeList?.let { Utils.expandView(it) }
        }

    }

    // show edit profile dialog
    private fun showEditProfileDialog() {

        editProfileDialog = Dialog(requireActivity())
        editProfileDialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        editProfileDialog?.setCanceledOnTouchOutside(true)
        editProfileDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        editProfileDialog?.setContentView(R.layout.dialog_profile)
        editProfileDialog?.findViewById<MaterialCardView>(R.id.mcvCamera)?.setOnClickListener {

            //Camera Launcher
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)

        }
        editProfileDialog?.findViewById<MaterialCardView>(R.id.mcvGallery)?.setOnClickListener {

            //Gallery Launcher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

        }
        editProfileDialog?.setCancelable(true)
        editProfileDialog?.show()
    }

    // add supplier dialog
    private fun showAddSupplierDialog() {

        addSupplierDialog = Dialog(requireActivity())
        addSupplierDialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        addSupplierDialog?.setCanceledOnTouchOutside(true)
        addSupplierDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)

        addSupplierBinding = ItemAddSupplierBinding.inflate(LayoutInflater.from(context))
        addSupplierDialog?.setContentView(addSupplierBinding!!.root)

        addSupplierBinding?.llLegalEntity?.setOnClickListener(this)
        addSupplierBinding?.llPhysicalPerson?.setOnClickListener(this)

        Utils.setFocusAndTextChangeListener(
            requireActivity(),
            addSupplierBinding!!.etFirstName, addSupplierBinding!!.mcvFirstName,
            addSupplierBinding!!.tvFirstNameError
        )

        Utils.setFocusAndTextChangeListener(
            requireActivity(),
            addSupplierBinding!!.etLastName,
            addSupplierBinding!!.mcvLastName,
            addSupplierBinding!!.tvLastNameError
        )

        Utils.setFocusAndTextChangeListener(
            requireActivity(),
            addSupplierBinding!!.etPhoneNumber, addSupplierBinding!!.mcvPhoneNumber,
            addSupplierBinding!!.tvPhoneNumberError
        )

        Utils.setFocusAndTextChangeListener(
            requireActivity(),
            addSupplierBinding!!.etEmail,
            addSupplierBinding!!.mcvEmail,
            addSupplierBinding!!.tvEmailError
        )

        addSupplierBinding!!.llType.setOnClickListener {
            showOrHideTypeDropDown()
        }
        addSupplierBinding!!.ivProfile.setOnClickListener {
            showEditProfileDialog()
        }

        addSupplierBinding!!.mcvSave.setOnClickListener {

            validateSupplierDetail()

        }

        addSupplierDialog?.setCancelable(true)
        addSupplierDialog?.show()

    }

    //Checks the Android Version And  Launch Custom Permission ,according to Version
    private fun checkAndroidVersionAndLaunchPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            customPermissionLauncher.launch(permissionsForVersionAbove11)
        } else {
            customPermissionLauncher.launch(permissionsForVersionBelow12)

        }
    }

    // check connected device
    private fun checkConnectedDevice() {

        BluetoothUtils.getConnectedDevice(requireActivity(), object : ConnectedDeviceCallback {

            @SuppressLint("MissingPermission")
            override fun onDeviceFound(device: ArrayList<BluetoothDevice>) {


                Utils.E("Status is ${rfidSetUp?.getScanningStatus()}")

                if (rfidSetUp?.getScanningStatus() == true) {

                    GetProgressBar.getInstance(requireContext())?.show()

                    rfidSetUp?.onPause(object : PairStatusCallback {

                        override fun pairSuccess() {

                            GetProgressBar.getInstance(requireContext())?.dismiss()

                            Utils.E("Status is success ${rfidSetUp?.getScanningStatus()}")

//                            lifecycleScope.launch {
//                                changetoplay()
//                            }
                        }

                        override fun pairFail() {

                            GetProgressBar.getInstance(requireContext())?.dismiss()
                            Utils.E("Status is fail ${rfidSetUp?.getScanningStatus()}")

                        }

                    })

                    if (rfidSetUp?.getScanningStatus() == false) {

                        Utils.T(requireActivity(), "Scanning stopped")
//                        binding.ivScan?.setImageResource(R.drawable.icon_play)
                    }

                } else {

                    GetProgressBar.getInstance(requireContext())?.show()

                    val sharedPreferences = requireActivity().getSharedPreferences(
                        Constants.frequencyData,
                        Context.MODE_PRIVATE
                    )

                    val defaultValue = 0 // Default value if the key doesn't exist
                    val frequencyIndex =
                        sharedPreferences.getInt(Constants.frequencyIndex, defaultValue)

                    rfidSetUp?.onResume(
                        device[0].address,
                        fValues[frequencyIndex],
                        object : PairStatusCallback {

                            override fun pairSuccess() {

                                GetProgressBar.getInstance(requireContext())?.dismiss()


//                            Utils.T(requireActivity(),"Scanning started")


                            }

                            override fun pairFail() {

                                GetProgressBar.getInstance(requireContext())?.dismiss()


//                            binding.ivScan?.setImageResource(R.drawable.icon_play)

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
    override fun onClick(view: View?) {

        when (view) {

            binding.mcvBackBtn -> {

                (requireActivity() as DashboardActivity).navController?.popBackStack()

            }

            binding.llSupplier -> {
                showOrHideSupplierDropDown()
            }

            binding.mcvDate -> {
                showDateDialog()
            }

            binding.mcvSave -> {

                validatePurchaseDetail()
            }

            binding.mcvAddSupplier -> {
                showAddSupplierDialog()
            }

            binding.mcvAddProduct -> {

                Utils.SelectedProductUUIDList.setProductList(ArrayList(selectedProductUUIDList))

                //Giving the fragment status
                val bundle = Bundle()
                bundle.putBoolean(Constants.addPurchase, true)

                (requireActivity() as DashboardActivity).navController?.navigate(
                    R.id.product,
                    bundle
                )
            }

            addSupplierBinding?.llLegalEntity -> {

                addSupplierBinding?.llLegalEntity?.setBackgroundColor(requireActivity().getColor(R.color.selected_drop_down_color))
                addSupplierBinding?.llPhysicalPerson?.setBackgroundColor(
                    requireActivity().getColor(
                        R.color.white
                    )
                )
                addSupplierBinding?.tvType?.text =
                    addSupplierBinding?.tvLegalEntity?.text.toString()
                addSupplierBinding?.tvTypeError?.visibility = View.GONE

                showOrHideTypeDropDown()

            }

            addSupplierBinding?.llPhysicalPerson -> {

                addSupplierBinding?.llLegalEntity?.setBackgroundColor(requireActivity().getColor(R.color.white))
                addSupplierBinding?.llPhysicalPerson?.setBackgroundColor(
                    requireActivity().getColor(
                        R.color.selected_drop_down_color
                    )
                )
                addSupplierBinding?.tvType?.text =
                    addSupplierBinding?.tvPhysicalPerson?.text.toString()
                addSupplierBinding?.tvTypeError?.visibility = View.GONE

                showOrHideTypeDropDown()

            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun selected(data: Any) {

        val contactDataModel = data as ContactDataModel

        if (binding.tvSupplier?.text.toString() != "${contactDataModel.firstName} ${contactDataModel.lastName}") {

            selectedSupplierUUID = contactDataModel.contactUUID
            binding.tvSupplier?.text = "${contactDataModel.firstName} ${contactDataModel.lastName}"
            binding.tvSupplierError?.visibility = View.GONE
            supplierName = "${contactDataModel.firstName} ${contactDataModel.lastName}"

            showOrHideSupplierDropDown()
        }
    }

    override fun unselect() {}

    override fun onTagRead(tagInfo: UHFTAGInfo) {

        // Handle RFID tag data
        etRFIDCode?.setText(tagInfo.epc)

        rfidSetUp?.onPause(object : PairStatusCallback {

            override fun pairSuccess() {}

            override fun pairFail() {}

        })

    }

    override fun onError(message: String) {
        // Handle errors
        Utils.E(message)
    }

}