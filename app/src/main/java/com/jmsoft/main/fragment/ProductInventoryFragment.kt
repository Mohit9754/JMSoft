@file:Suppress("DEPRECATION")

package com.jmsoft.main.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
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
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jmsoft.R
import com.jmsoft.Utility.UtilityTools.CustomQRViewWithLabel
import com.jmsoft.Utility.database.CategoryDataModel
import com.jmsoft.Utility.database.CollectionDataModel
import com.jmsoft.Utility.database.MetalTypeDataModel
import com.jmsoft.Utility.database.ProductDataModel
import com.jmsoft.Utility.database.StockLocationDataModel
import com.jmsoft.Utility.UtilityTools.BluetoothUtils
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.Utility.UtilityTools.ProductUUIDList
import com.jmsoft.Utility.UtilityTools.RFIDSetUp
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.KeyboardUtils.hideKeyboard
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.basic.UtilityTools.Utils.DecimalDigitsInputFilter
import com.jmsoft.basic.validation.ResultReturn
import com.jmsoft.basic.validation.Validation
import com.jmsoft.basic.validation.ValidationModel
import com.jmsoft.databinding.DialogAddMetalTypeBinding
import com.jmsoft.databinding.DialogOpenSettingBinding
import com.jmsoft.databinding.DialogProfileBinding
import com.jmsoft.databinding.FragmentProductInventoryBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.AddImageAdapter
import com.jmsoft.main.adapter.CategoryDropdownAdapter
import com.jmsoft.main.adapter.CollectionDropdownAdapter
import com.jmsoft.main.adapter.MetalTypeDropdownAdapter
import com.jmsoft.main.adapter.SelectedCollectionAdapter
import com.jmsoft.main.adapter.StockLocationDropdownAdapter
import com.jmsoft.main.`interface`.CollectionStatusCallback
import com.jmsoft.main.`interface`.ConnectedDeviceCallback
import com.jmsoft.main.`interface`.PairStatusCallback
import com.jmsoft.main.`interface`.SelectedCallback
import com.jmsoft.main.model.SelectedCollectionModel
import com.rscja.deviceapi.entity.UHFTAGInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductInventoryFragment : Fragment(), View.OnClickListener, RFIDSetUp.RFIDCallback {

    private lateinit var binding: FragmentProductInventoryBinding

    private var selectedCollectionList = ArrayList<SelectedCollectionModel>()

    private var selectedCollectionUUID = mutableListOf<String>()

    private var selectedCollectionAdapter: SelectedCollectionAdapter? = null

    // for Opening the Camera Dialog
    private var forCameraSettingDialog = 100

    // for Opening the Gallery Dialog
    private var forGallerySettingDialog = 200

    private var productImageView: ImageView? = null

    private var addImageAdapter: AddImageAdapter? = null

    private var productDataModel: ProductDataModel? = null

    private var metalTypeDropdownAdapter: MetalTypeDropdownAdapter? = null

    private var stockLocationDropdownAdapter: StockLocationDropdownAdapter? = null

    private var maxImageLimit = 5 // Maximum images allowed

    private val selectedProductImage: ArrayList<Any> = ArrayList()

    private val selectedProductImageBitmap: ArrayList<Bitmap> = ArrayList()

    private val barcodeImage: ArrayList<Bitmap> = ArrayList()

    private var metalTypeDropdownList = ArrayList<MetalTypeDataModel>()

    private var stockLocationDropdownList = ArrayList<StockLocationDataModel>()

    private var selectedMetalTypeUUID: String? = null

    private var selectedStockLocationUUID: String? = null

    private var dialogAddMetalTypeBinding: DialogAddMetalTypeBinding? = null

    private var dialogAddCategoryBinding: DialogAddMetalTypeBinding? = null

    private var selectedCategoryUUID: String? = null

    private var collectionDropdownAdapter: CollectionDropdownAdapter? = null

    private var categoryDropdownAdapter: CategoryDropdownAdapter? = null

    private var barcodeData: String? = null

    private var categoryDropdownList = ArrayList<CategoryDataModel>()

    private var collectionDataList = ArrayList<CollectionDataModel>()

    private var isCollectionShow = false

    private var dialogMetalType: Dialog? = null

    private var dialogCategory: Dialog? = null

    private var rfidSetUp: RFIDSetUp? = null

    private var productUUIDIndex: Int = -1

    private var addDuplicate = false

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

//    private val PERMISSIONS_REQUEST_CODE = 100

    // Gallery result launcher
    @SuppressLint("NotifyDataSetChanged")
    private var galleryActivityResultLauncher: ActivityResultLauncher<Intent?>? =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedUris: MutableList<Uri> = mutableListOf()

                // Check if multiple images were selected
                result.data?.clipData?.let { clipData ->
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        selectedUris.add(uri)
                    }
                }

                // Check if a single image was selected (fallback for older devices or single selections)
                if (result.data?.clipData == null) {
                    result.data?.data?.let { uri ->
                        selectedUris.add(uri)
                    }
                }
                // Check if selected images meet the minimum and maximum limits
                if (selectedUris.size > maxImageLimit - selectedProductImage.size) {

                    Utils.T(
                        requireActivity(),
                        "Maximum ${maxImageLimit - selectedProductImage.size} images allowed"
                    )
                    return@registerForActivityResult
                }

                // Update selectedImageUris list with valid selections
                selectedProductImage.addAll(selectedUris)

                addImageAdapter?.notifyDataSetChanged()
                binding.tvProductImageError.visibility = View.GONE

                // Now you have selected image URIs respecting the min and max limits
                // Handle them as needed in your app
            }
        }

    //Gallery Permission Launcher

    private var galleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean? ->
        if (isGranted == true) {

//            val galleryIntent =
//                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//            galleryActivityResultLauncher?.launch(galleryIntent)

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Enable multiple selection

            // Set min and max selection limits
//            intent.putExtra(Intent.EXTRA_MIN, 2)
//            intent.putExtra(Intent.EXTRA_MAXIMUM, 5)

            galleryActivityResultLauncher?.launch(intent)

        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)) {

                    showOpenSettingDialog(forGallerySettingDialog)
                }
            } else {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {

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

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraActivityResultLauncher.launch(cameraIntent)

        } else {

            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {

                showOpenSettingDialog(forCameraSettingDialog)
            }
        }
    }

    // Camera result Launcher
    @SuppressLint("NotifyDataSetChanged")
    private var cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == Activity.RESULT_OK) {

            binding.tvProductImageError.visibility = View.GONE
            productImageView?.setImageBitmap(result.data?.extras?.get("data") as Bitmap?)
            productImageView?.drawable?.let { selectedProductImage.add(it.toBitmap()) }

            addImageAdapter?.notifyDataSetChanged()
            binding.tvProductImageError.visibility = View.GONE

        }
    }

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

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding = FragmentProductInventoryBinding.inflate(layoutInflater)

        //set the Clicks And initialization
        lifecycleScope.launch(Dispatchers.Main) {
            init()
        }

        return binding.root
    }

    // Set metal type dropdown
    private fun setMetalTypeRecyclerView() {

        metalTypeDropdownList = Utils.getAllMetalType()

        metalTypeDropdownAdapter = MetalTypeDropdownAdapter(
            requireActivity(),
            metalTypeDropdownList,
            this,
            object : SelectedCallback {

                override fun selected(data: Any) {

                    val metalTypeDataModel = data as MetalTypeDataModel

                    selectedMetalTypeUUID = metalTypeDataModel.metalTypeUUID
                    binding.tvMetalType.text = metalTypeDataModel.metalTypeName
                    binding.tvMetalTypeError.visibility = View.GONE
                    binding.ivMetalType.let { Utils.rotateView(it, 0f) }
                    binding.mcvMetalTypeList.let { Utils.collapseView(it) }

                }

                override fun unselect() {

                    selectedMetalTypeUUID = null
                    binding.tvMetalType.text = ""
                    binding.tvMetalTypeError.visibility = View.GONE
//                    binding.mcvMetalTypeList?.visibility = View.GONE
                    showOrHideMetalTypeDropDown()

                }
            }
        )

        if (selectedMetalTypeUUID != null) {
            metalTypeDropdownAdapter?.selectedMetalTypePosition =
                metalTypeDropdownList.indexOfFirst { it.metalTypeUUID == selectedMetalTypeUUID }
        }

        binding.rvMetalType.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        binding.rvMetalType.adapter = metalTypeDropdownAdapter

    }

    // Set stock location dropdown
    private fun setStockLocationRecyclerView() {

        stockLocationDropdownList = Utils.getAllStockLocation()

        stockLocationDropdownAdapter = StockLocationDropdownAdapter(
            requireActivity(),
            stockLocationDropdownList,
            false,
            object : SelectedCallback {

                override fun selected(data: Any) {

                    val stockLocationDataModel = data as StockLocationDataModel

                    if (binding.tvStockLocation.text.toString() != stockLocationDataModel.stockLocationName) {

                        selectedStockLocationUUID = stockLocationDataModel.stockLocationUUID
                        binding.tvStockLocation.text = stockLocationDataModel.stockLocationName
                        binding.tvStockLocationError.visibility = View.GONE
                        binding.ivStockLocation.let { Utils.rotateView(it, 0f) }
                        binding.mcvStockLocationList.let { Utils.collapseView(it) }

                    }

                }

                override fun unselect() {

                    selectedStockLocationUUID = null
                    binding.tvStockLocation.text = ""
                    binding.tvStockLocationError.visibility = View.GONE
//                    binding.mcvMetalTypeList?.visibility = View.GONE
                    showOrHideStockLocationDropDown()

                }
            }
        )

        if (selectedStockLocationUUID != null) {
            stockLocationDropdownAdapter?.selectedStockLocationPosition =
                stockLocationDropdownList.indexOfFirst { it.stockLocationUUID == selectedStockLocationUUID }
        }

        binding.rvStockLocation.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        binding.rvStockLocation.adapter = stockLocationDropdownAdapter

    }

    // Set category dropdown
    private fun setCategoryRecyclerView() {

        categoryDropdownList = Utils.getAllCategory()

        categoryDropdownAdapter = CategoryDropdownAdapter(
            requireActivity(),
            categoryDropdownList,
            this,
            object : SelectedCallback {

                override fun selected(data: Any) {

                    val categoryDataModel = data as CategoryDataModel

                    selectedCategoryUUID = categoryDataModel.categoryUUID
                    binding.tvCategory.text = categoryDataModel.categoryName
                    binding.tvCategoryError.visibility = View.GONE
                    binding.ivCategory.let { Utils.rotateView(it, 0f) }
                    binding.mcvCategoryList.let { Utils.collapseView(it) }

                }

                override fun unselect() {

                    selectedCategoryUUID = null
                    binding.tvCategory.text = ""
                    binding.tvCategoryError.visibility = View.GONE

                    showOrHideCategoryDropDown()

                }
            }
        )

        if (selectedCategoryUUID != null) {
            val position =
                categoryDropdownList.indexOfFirst { it.categoryUUID == selectedCategoryUUID }
            categoryDropdownAdapter?.selectedPosition = position
            binding.tvCategory.text = categoryDropdownList[position].categoryName
        }

        binding.rvCategory.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        binding.rvCategory.adapter = categoryDropdownAdapter

    }

    // Set collection drop down
    private fun setCollectionRecyclerView() {

        collectionDataList = Utils.getAllCollection()

        collectionDropdownAdapter = CollectionDropdownAdapter(requireActivity(),
            collectionDataList,
            object : CollectionStatusCallback {

                @SuppressLint("NotifyDataSetChanged")
                override fun collectionSelected(
                    selectedCollectionModel: SelectedCollectionModel,
                    closeDropDown: Boolean
                ) {

                    if (closeDropDown) {

                        binding.ivCollection.let { Utils.rotateView(it, 0f) }
                        binding.mcvCollectionList.let { Utils.collapseView(it) }

                    }

                    binding.tvCollection.visibility = View.GONE
                    binding.tvCollectionError.visibility = View.GONE

                    if (!selectedCollectionList.contains(selectedCollectionModel)) {

                        selectedCollectionList.clear()
                        selectedCollectionList.add(selectedCollectionModel)
                        selectedCollectionAdapter?.notifyDataSetChanged()
                    }

                }

                @SuppressLint("NotifyDataSetChanged")
                override fun collectionUnSelected(selectedCollectionModel: SelectedCollectionModel) {

                    selectedCollectionList.remove(selectedCollectionModel)

                    if (selectedCollectionList.isEmpty()) {
                        binding.tvCollection.visibility = View.VISIBLE
                    }

                    selectedCollectionAdapter?.notifyDataSetChanged()

                }
            }
        )

        if (selectedCollectionUUID.isNotEmpty()) {
            collectionDropdownAdapter?.selectedCollectionUUID = selectedCollectionUUID
        }

        binding.rvCollectionList.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)

        binding.rvCollectionList.adapter = collectionDropdownAdapter

    }

    // Set selected collection item
    private fun setSelectedCollectionRecyclerView() {

        selectedCollectionAdapter =
            SelectedCollectionAdapter(requireActivity(), selectedCollectionList, binding)

        binding.rvCollectionSelectedList.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)

        binding.rvCollectionSelectedList.adapter = selectedCollectionAdapter

        binding.mcvCollectionList.visibility = View.GONE

    }

    // Set product images
    fun setProductImageRecyclerView() {

        addImageAdapter = AddImageAdapter(
            requireActivity(),
            selectedProductImage,
            this,
            selectedProductImageBitmap
        )
        binding.rvProductImage.layoutManager =
            GridLayoutManager(requireActivity(), 3) // Span Count is set to 3
        binding.rvProductImage.adapter = addImageAdapter
    }

    // Set focus change listener
    private fun setFocusChangeListener() {

        binding.etProductName.let {
            binding.mcvProductName.let { it1 ->
                Utils.setFocusChangeListener(
                    requireActivity(),
                    it, it1
                )
            }
        }

        binding.etOrigin.let {
            binding.mcvOrigin.let { it1 ->
                Utils.setFocusChangeListener(
                    requireActivity(),
                    it, it1
                )
            }
        }

        binding.etWeight.let {
            binding.mcvWeight.let { it1 ->
                Utils.setFocusChangeListener(
                    requireActivity(),
                    it, it1
                )
            }
        }

        binding.etCarat.let {
            binding.mcvCarat.let { it1 ->
                Utils.setFocusChangeListener(
                    requireActivity(),
                    it, it1
                )
            }
        }

        binding.etPrice.let {
            binding.mcvPrice.let { it1 ->
                Utils.setFocusChangeListener(
                    requireActivity(),
                    it, it1
                )
            }
        }

        binding.etDescription.let {
            binding.mcvDescription.let { it1 ->
                Utils.setFocusChangeListener(
                    requireActivity(),
                    it, it1
                )
            }
        }

        binding.etRFIDCode.let {
            binding.mcvRFIDCode.let { it1 ->
                Utils.setFocusChangeListener(
                    requireActivity(),
                    it, it1
                )
            }
        }

        binding.etBarcode.let {
            binding.mcvBarcode.let { it1 ->
                Utils.setFocusChangeListener(
                    requireActivity(),
                    it, it1
                )
            }
        }

        binding.etCost.let {
            binding.mcvCost.let { it1 ->
                Utils.setFocusChangeListener(
                    requireActivity(),
                    it, it1
                )
            }
        }
    }

    // Set text change listener
    private fun setTextChangeListener() {

        binding.etProductName.let {
            binding.tvProductNameError.let { it1 ->
                Utils.setTextChangeListener(
                    it,
                    it1
                )
            }
        }

        binding.etOrigin.let {
            binding.tvOriginError.let { it1 ->
                Utils.setTextChangeListener(
                    it,
                    it1
                )
            }
        }

        binding.etWeight.let {
            binding.tvWeightError.let { it1 ->
                Utils.setTextChangeListener(
                    it,
                    it1
                )
            }
        }

        binding.etCarat.let {
            binding.tvCaratError.let { it1 ->
                Utils.setTextChangeListener(
                    it,
                    it1
                )
            }
        }

        binding.etPrice.let {
            binding.tvPriceError.let { it1 ->
                Utils.setTextChangeListener(
                    it,
                    it1
                )
            }
        }

        binding.etDescription.let {
            binding.tvDescriptionError.let { it1 ->
                Utils.setTextChangeListener(
                    it,
                    it1
                )
            }
        }

        binding.etRFIDCode.let {
            binding.tvRFIDCodeError.let { it1 ->
                Utils.setTextChangeListener(
                    it,
                    it1
                )
            }
        }

        binding.etCost.let {
            binding.tvCostError.let { it1 ->
                Utils.setTextChangeListener(
                    it,
                    it1
                )
            }
        }

    }

    private fun setSelectedStockLocation(stockLocationUUID: String) {

        val stockLocationDataModel = Utils.getStockLocation(stockLocationUUID)

        if (stockLocationDataModel.stockLocationName != null) {
            binding.tvStockLocation.text = stockLocationDataModel.stockLocationName
        }
        selectedStockLocationUUID = stockLocationUUID
    }

    private fun newProduct() {

        binding.mcvAddDuplicate.visibility = View.GONE

        binding.mcvPrint.visibility = View.GONE


        val stockLocationUUID = arguments?.getString(Constants.stockLocationUUID)

        stockLocationUUID?.let { setSelectedStockLocation(it) }

        val productRfidCode = arguments?.getString(Constants.rfidCode)

        if (productRfidCode != null) {
            binding.etRFIDCode.setText(productRfidCode)
        }

        val productListSize = ProductUUIDList.getSize()

        productUUIDIndex = productListSize

        binding.llPageIndicator.visibility = View.VISIBLE
//            binding.mcvPrevious.visibility = View.GONE
        binding.mcvNext.visibility = View.GONE

        if (productListSize == 0) {
            binding.mcvPrevious.visibility = View.GONE
        }

        binding.tvPageDetail.text =
            getString(
                R.string.page_to,
                (productListSize + 1).toString(),
                (productListSize + 1).toString()
            )

        GetProgressBar.getInstance(requireActivity())?.dismiss()


    }

    // Check add or edit status
    private suspend fun checkAddOrEditState() {

        ProductUUIDList.getData()

        val productUUID = arguments?.getString(Constants.productUUID)

        if (productUUID != null) {

            val result = lifecycleScope.async(Dispatchers.IO) {
                return@async Utils.getProductThroughProductUUID(productUUID)
            }

            val productData = result.await()

            withContext(Dispatchers.Main) {

                val addDuplicateStatus = arguments?.getBoolean(Constants.addDuplicate)

                if (addDuplicateStatus == true) {

                    addDuplicate = true
                    newProduct()
                } else {

                    binding.etRFIDCode.setText(productData.productRFIDCode)

                    productDataModel = productData

                    binding.mcvAddDuplicate.visibility = View.VISIBLE

                    binding.mcvPrint.visibility = View.VISIBLE


                    binding.llPageIndicator.visibility = View.VISIBLE

                    productUUIDIndex = ProductUUIDList.getIndexOf(productUUID)

                    if (productUUIDIndex == 0) {
                        binding.mcvPrevious.visibility = View.GONE
                    }

                    if (productUUIDIndex + 1 == ProductUUIDList.getSize()) {

                        if (!ProductUUIDList.getStatus()) {
                            binding.mcvNext.visibility = View.GONE
                        }
                    }

                    val pageFlag = if (ProductUUIDList.getStatus()) 1 else 0

                    binding.tvPageDetail.text = getString(
                        R.string.page_to,
                        (productUUIDIndex + 1).toString(),
                        (ProductUUIDList.getSize() + pageFlag).toString()
                    )

                }

                binding.etProductName.setText(productData.productName)
                binding.etProductName.setText(productData.productName)

                binding.mcvMetalTypeList.visibility = View.VISIBLE
                selectedMetalTypeUUID = productData.metalTypeUUID

                selectedCollectionUUID =
                    productData.collectionUUID?.split(",")?.toMutableList() ?: mutableListOf()

                productData.stockLocationUUID?.let { setSelectedStockLocation(it) }

                binding.etOrigin.setText(productData.productOrigin)
                binding.etWeight.setText(productData.productWeight.toString())
                binding.etCarat.setText(productData.productCarat.toString())
                binding.etPrice.setText(productData.productPrice.toString())
                binding.etCost.setText(productData.productCost.toString())

//            binding.mcvCategoryList?.visibility = View.VISIBLE
                selectedCategoryUUID = productData.categoryUUID

                binding.etDescription.setText(productData.productDescription)

                binding.mcvBarcodeImage.visibility = View.VISIBLE

                val barcodeBitmap = productData.productBarcodeUri?.let {
                    Utils.getImageFromInternalStorage(
                        requireActivity(),
                        it
                    )
                }
                val productImageUri = productData.productImageUri?.split(",") ?: listOf()

                selectedProductImage.clear()

                for (imageUri in productImageUri) {
                    Utils.getImageFromInternalStorage(requireActivity(), imageUri.trim())
                        ?.let { selectedProductImage.add(it) }
                }
                binding.ivBarcodeImage.setImageBitmap(barcodeBitmap)
                barcodeBitmap?.let { barcodeImage.add(it) }

                binding.etBarcode.setText(productData.productBarcodeData)
                barcodeData = productData.productBarcodeData

                GetProgressBar.getInstance(requireActivity())?.dismiss()

            }

        } else {

            newProduct()

        }
    }

    //set the Clicks And initialization
    private suspend fun init() {

        (requireActivity() as DashboardActivity).binding?.mcvSearch?.visibility = View.GONE

        // Check add or edit status
        val job = lifecycleScope.launch(Dispatchers.Main) {
            checkAddOrEditState()
        }

        job.join()

        //Initialize RFID
        rfidSetUp = RFIDSetUp(requireContext(), this)

        // Set focus change listener
        setFocusChangeListener()

        // Set text change listener
        setTextChangeListener()

        // Set filter of only 2 digit after point
        binding.etWeight.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter())

        // Set filter of only 2 digit after point
        binding.etPrice.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter())

        // Set filter of only 2 digit after point
        binding.etCost.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter())

        // Set metal type dropdown
        setMetalTypeRecyclerView()

        setStockLocationRecyclerView()

        // Set category dropdown
        setCategoryRecyclerView()

        // Set collection drop down
        setCollectionRecyclerView()

        // Set selected collection item
        setSelectedCollectionRecyclerView()

        // Set product images   
        setProductImageRecyclerView()

        binding.llMetalType.setOnClickListener(this)

        binding.llStockLocation.setOnClickListener(this)

        binding.mcvCollection.setOnClickListener(this)

        binding.llCategory.setOnClickListener(this)

        binding.ivCollection.setOnClickListener(this)

        binding.mcvSave.setOnClickListener(this)

        // Set Click on Back Button
        binding.mcvBackBtn.setOnClickListener(this)

        binding.mcvAddMetalType.setOnClickListener(this)

        binding.mcvAddStockLocation.setOnClickListener(this)

        binding.mcvBarcodeBtn.setOnClickListener(this)

        binding.ivCross.setOnClickListener(this)

        binding.root.setOnClickListener(this)

        binding.mcvAddCategory.setOnClickListener(this)

        binding.mcvRFIDCodeBtn.setOnClickListener(this)

        binding.mcvPrevious.setOnClickListener(this)

        binding.mcvNext.setOnClickListener(this)

        binding.mcvPrint.setOnClickListener(this)


        binding.mcvAddDuplicate.setOnClickListener(this)

        if (productDataModel != null) {
            showOrHideCollectionDropDown()
            showOrHideCollectionDropDown()
        }

    }

    // Edit Profile Dialog
    fun showImageSelectionDialog(imageView: ImageView?) {

        productImageView = imageView

        val dialog = Dialog(requireActivity())
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogProfileBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialogBinding.tvMessage.text =
            requireActivity().getString(R.string.to_proceed_with_adding_or_updating_your_product_n_picture_please_select_an_image_source)

        dialogBinding.mcvCamera.setOnClickListener {
            dialog.dismiss()
            //Camera Launcher
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)

        }

        dialogBinding.mcvGallery.setOnClickListener {

            dialog.dismiss()

            //Gallery Launcher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

        }
        dialog.setCancelable(true)
        dialog.show()
    }

    // make a single string of selected collection uuid list
    private fun getSelectedCollectionUUID(): String {

        val collectionUUID = ArrayList<String>()

        for (collectionData in selectedCollectionList) {
            collectionData.collectionDataModel.collectionUUID?.let { collectionUUID.add(it) }
        }
        return collectionUUID.joinToString().replace(" ", "")

    }

    // make a single string of selected product uuid list
    private fun getProductImageUri(): String {

        val imageUri = ArrayList<String>()

        for (imageBitmap in selectedProductImageBitmap) {
            imageUri.add(Utils.getPictureUri(requireActivity(), imageBitmap))
        }
        return imageUri.joinToString().replace(" ", "")

    }

    // Add or update product
    @SuppressLint("NotifyDataSetChanged")
    private suspend fun addOrUpdateProduct(
        productDataModel: ProductDataModel?,
        isSaveButtonClicked: Boolean,
        isNextButtonClicked: Boolean
    ) {

        val productData = ProductDataModel()

        productData.productUUID =
            if (productDataModel != null) productDataModel.productUUID else Utils.generateUUId()

        withContext(Dispatchers.Main) {

            productData.productName = binding.etProductName.text.toString().trim()
            productData.productOrigin = binding.etOrigin.text.toString().trim()
            productData.productCarat = binding.etCarat.text.toString().toInt()
            productData.productWeight =
                Utils.roundToTwoDecimalPlaces(binding.etWeight.text.toString().toDouble())
            productData.productPrice =
                Utils.roundToTwoDecimalPlaces(binding.etPrice.text.toString().toDouble())
            productData.productCost =
                Utils.roundToTwoDecimalPlaces(binding.etCost.text.toString().toDouble())
            productData.productDescription = binding.etDescription.text.toString().trim()
            productData.productRFIDCode = binding.etRFIDCode.text.toString().trim()

        }

        productData.metalTypeUUID = selectedMetalTypeUUID
        productData.collectionUUID = getSelectedCollectionUUID()
        productData.categoryUUID = selectedCategoryUUID
        productData.productBarcodeData = barcodeData
        productData.productBarcodeUri = Utils.getPictureUri(requireActivity(), barcodeImage[0])
        productData.productImageUri = getProductImageUri()
        productData.stockLocationUUID = selectedStockLocationUUID ?: ""

        if (productDataModel != null) {

            productDataModel.productBarcodeUri?.let {
                Utils.deleteImageFromInternalStorage(
                    requireActivity(),
                    it
                )
            }

            val productImages = productDataModel.productImageUri?.split(",")

            if (productImages != null) {

                for (product in productImages) {

                    if (product != Constants.Default_Image) {
                        Utils.deleteImageFromInternalStorage(requireActivity(), product)
                    }
                }
            }

            lifecycleScope.launch(Dispatchers.IO) {

//                Utils.E("Name of the thread ${Thread.currentThread().name}")
                Utils.updateProduct(productData)
            }

            withContext(Dispatchers.Main) {
                Utils.T(
                    requireActivity(),
                    requireActivity().getString(R.string.updated_successfully)
                )
            }
        } else {

            lifecycleScope.launch(Dispatchers.IO) {
//                Utils.E("Name of the thread ${Thread.currentThread().name}")

                Utils.addProduct(productData)
            }

            withContext(Dispatchers.Main) {
                Utils.T(requireActivity(), requireActivity().getString(R.string.added_successfully))
            }

        }

        withContext(Dispatchers.Main) {

            if (isSaveButtonClicked) {

                if (addDuplicate) {
                    (requireActivity() as DashboardActivity).navController?.popBackStack()
                }

                (requireActivity() as DashboardActivity).navController?.popBackStack()

            } else {

                if (isNextButtonClicked) {
                    changePage(1)
                } else {
                    changePage(-1)
                }
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

    // Open Setting Dialog
    private fun showOpenSettingDialog(dialogCode: Int) {

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

    /* validate input details */
    private fun validate(isSaveButtonClicked: Boolean, isNextButtonClicked: Boolean) {

        val errorValidationModel: MutableList<ValidationModel> = ArrayList()

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.Empty, binding.etProductName, binding.tvProductNameError
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.EmptyTextView, binding.tvMetalType, binding.tvMetalTypeError
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.NoSpecialChar, binding.etOrigin, binding.tvOriginError
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.Empty, binding.etWeight, binding.tvWeightError
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.Empty, binding.etCarat, binding.tvCaratError
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.Empty, binding.etPrice, binding.tvPriceError
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.Empty, binding.etCost, binding.tvCostError
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.EmptyTextView, binding.tvCategory, binding.tvCategoryError
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.Empty, binding.etDescription, binding.tvDescriptionError
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.Barcode,
                barcodeImage.size,
                binding.tvBarcodeError,
                binding.etBarcode
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.AtLeastTwo,
                selectedProductImage.size,
                binding.tvProductImageError,
                null
            )
        )

        val validation: Validation? = Validation.instance

        val resultReturn: ResultReturn? =
            validation?.CheckValidation(requireActivity(), errorValidationModel)

        if (resultReturn?.aBoolean == true) {

            lifecycleScope.launch(Dispatchers.Default) {
                addOrUpdateProduct(productDataModel, isSaveButtonClicked, isNextButtonClicked)
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

                if (validation?.textViewPointer != null) {

                    if (validation.textViewPointer == binding.tvMetalType) {

                        binding.ivMetalType.let { Utils.rotateView(it, 0f) }
                        binding.mcvMetalTypeList.let { Utils.expandView(it) }
                    } else if (validation.textViewPointer == binding.tvCategory) {

                        binding.ivCategory.let { Utils.rotateView(it, 0f) }
                        binding.mcvCategoryList.let { Utils.expandView(it) }
                    }

                    validation.textViewPointer = null
                } else {

                    validation?.EditTextPointer?.requestFocus()

                    val imm =
                        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(validation?.EditTextPointer, InputMethodManager.SHOW_IMPLICIT)

                    validation?.EditTextPointer = null

                }
            }
        }
    }

    // Add category
    private fun addCategory() {

        val isCategoryExist =
            Utils.isCategoryExist(Utils.capitalizeData(dialogAddCategoryBinding?.etMetalType?.text.toString()))

        if (isCategoryExist == true) {

            dialogAddCategoryBinding?.let {
                Utils.showError(
                    requireActivity(), it.tvMetalTypeError,
                    getString(R.string.category_already_exist)
                )
            }

        } else {

            val categoryDataModel = CategoryDataModel()
            categoryDataModel.categoryUUID = Utils.generateUUId()
            categoryDataModel.categoryName =
                Utils.capitalizeData(dialogAddCategoryBinding?.etMetalType?.text.toString())

            Utils.addCategory(categoryDataModel)

            Utils.T(
                requireActivity(),
                requireActivity().getString(R.string.added_successfully)
            )

            setCategoryRecyclerView()

            binding.mcvCategoryList.visibility = View.GONE
            showOrHideCategoryDropDown()

            dialogCategory?.dismiss()
        }
    }

    // Add metal type
    private fun addMetalType() {

        val isMetalTypeExist =
            Utils.isMetalTypeExist(Utils.capitalizeData(dialogAddMetalTypeBinding?.etMetalType?.text.toString()))

        if (isMetalTypeExist == true) {

            dialogAddMetalTypeBinding?.let {
                Utils.showError(
                    requireActivity(), it.tvMetalTypeError,
                    getString(R.string.metal_type_already_exist)
                )
            }
        } else {

            val metalTypeDataModel = MetalTypeDataModel()
            metalTypeDataModel.metalTypeUUID = Utils.generateUUId()
            metalTypeDataModel.metalTypeName =
                Utils.capitalizeData(dialogAddMetalTypeBinding?.etMetalType?.text.toString())

            Utils.addMetalTypeInTheMetalTypeTable(metalTypeDataModel)

            Utils.T(
                requireActivity(),
                requireActivity().getString(R.string.added_successfully)
            )
            setMetalTypeRecyclerView()

            binding.mcvMetalTypeList.visibility = View.GONE
            showOrHideMetalTypeDropDown()

            dialogMetalType?.dismiss()
        }
    }

    // Update metal type
    private fun updateMetalType(metalTypeUUID: String, position: Int) {

        val metalTypeDataModel = MetalTypeDataModel()
        metalTypeDataModel.metalTypeUUID = metalTypeUUID
        metalTypeDataModel.metalTypeName =
            Utils.capitalizeData(dialogAddMetalTypeBinding?.etMetalType?.text.toString())

        val isCollectionExistAccept = Utils.isMetalTypeExistAccept(metalTypeDataModel)

        if (isCollectionExistAccept == true) {

            dialogAddMetalTypeBinding?.let {

                Utils.showError(
                    requireActivity(), it.tvMetalTypeError,
                    getString(R.string.metal_type_already_exist)
                )
            }
        } else {

            Utils.updateMetalType(
                metalTypeUUID,
                Utils.capitalizeData(dialogAddMetalTypeBinding?.etMetalType?.text.toString())
            )
            metalTypeDropdownList[position].metalTypeName =
                Utils.capitalizeData(dialogAddMetalTypeBinding?.etMetalType?.text.toString())
            metalTypeDropdownAdapter?.notifyItemChanged(position)


            dialogMetalType?.dismiss()
        }
    }

    // Metal type dialog for add or edit
    @SuppressLint("NotifyDataSetChanged")
    fun showAddOrEditMetalTypeDialog(position: Int?, metalTypeUUID: String?) {

        dialogMetalType = Dialog(requireActivity())
        dialogMetalType?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogMetalType?.setCanceledOnTouchOutside(true)
        dialogMetalType?.requestWindowFeature(Window.FEATURE_NO_TITLE)

        dialogAddMetalTypeBinding = DialogAddMetalTypeBinding.inflate(LayoutInflater.from(context))
        dialogMetalType?.setContentView(dialogAddMetalTypeBinding!!.root)

        if (position != null && metalTypeUUID != null) {
            dialogAddMetalTypeBinding!!.tvTitle.text = getString(R.string.edit_metal_type)
            dialogAddMetalTypeBinding!!.etMetalType.setText(metalTypeDropdownList[position].metalTypeName)
        } else {
            dialogAddMetalTypeBinding!!.tvTitle.text =
                requireActivity().getString(R.string.add_metal_type)
        }

        dialogAddMetalTypeBinding!!.tvName.text = requireActivity().getString(R.string.metal_type)
        dialogAddMetalTypeBinding!!.etMetalType.hint =
            requireActivity().getString(R.string.enter_metal_type)

        Utils.setFocusChangeListener(
            requireActivity(),
            dialogAddMetalTypeBinding!!.etMetalType, dialogAddMetalTypeBinding!!.mcvMetalType
        )
        Utils.setTextChangeListener(
            dialogAddMetalTypeBinding!!.etMetalType,
            dialogAddMetalTypeBinding!!.tvMetalTypeError
        )

        dialogAddMetalTypeBinding!!.mcvSave.setOnClickListener {

            val errorValidationModels: MutableList<ValidationModel> = ArrayList()

            errorValidationModels.add(
                ValidationModel(
                    Validation.Type.Empty,
                    dialogAddMetalTypeBinding!!.etMetalType,
                    dialogAddMetalTypeBinding!!.tvMetalTypeError
                )
            )

            val validation: Validation? = Validation.instance
            val resultReturn: ResultReturn? =
                validation?.CheckValidation(requireActivity(), errorValidationModels)
            if (resultReturn?.aBoolean == true) {

                if (position != null && metalTypeUUID != null) {

                    updateMetalType(metalTypeUUID, position)

                } else {

                    // add metal type
                    addMetalType()
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

        dialogAddMetalTypeBinding!!.mcvCancel.setOnClickListener {
            dialogMetalType?.dismiss()
        }

        dialogMetalType?.setCancelable(true)
        dialogMetalType?.show()
    }

    // Update category
    private fun updateCategory(categoryUUID: String, position: Int) {

        val categoryDataModel = CategoryDataModel()
        categoryDataModel.categoryUUID = categoryUUID
        categoryDataModel.categoryName =
            Utils.capitalizeData(dialogAddCategoryBinding?.etMetalType?.text.toString())

        val isCategoryExistAccept = Utils.isCategoryExistAccept(categoryDataModel)

        if (isCategoryExistAccept == true) {

            dialogAddCategoryBinding?.let {
                Utils.showError(
                    requireActivity(), it.tvMetalTypeError,
                    getString(R.string.category_already_exist)
                )
            }
        } else {

            Utils.updateCategory(categoryDataModel)

            categoryDropdownList[position].categoryName =
                Utils.capitalizeData(dialogAddCategoryBinding?.etMetalType?.text.toString())
            categoryDropdownAdapter?.notifyItemChanged(position)

            Utils.T(requireActivity(), requireActivity().getString(R.string.updated_successfully))

            dialogCategory?.dismiss()
        }
    }

    // Category dialog for add or edit
    fun showAddOrEditCategoryDialog(position: Int?, categoryUUID: String?) {

        dialogCategory = Dialog(requireActivity())
        dialogCategory?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogCategory?.setCanceledOnTouchOutside(true)
        dialogCategory?.requestWindowFeature(Window.FEATURE_NO_TITLE)

        dialogAddCategoryBinding = DialogAddMetalTypeBinding.inflate(LayoutInflater.from(context))
        dialogCategory?.setContentView(dialogAddCategoryBinding!!.root)

        if (position != null && categoryUUID != null) {
            dialogAddCategoryBinding!!.tvTitle.text = getString(R.string.edit_category)
            dialogAddCategoryBinding!!.etMetalType.setText(categoryDropdownList[position].categoryName)
        } else {
            dialogAddCategoryBinding!!.tvTitle.text =
                requireActivity().getString(R.string.add_category)
        }

        dialogAddCategoryBinding!!.tvName.text = requireActivity().getString(R.string.category)
        dialogAddCategoryBinding!!.etMetalType.hint =
            requireActivity().getString(R.string.enter_category_name)

        Utils.setFocusChangeListener(
            requireActivity(),
            dialogAddCategoryBinding!!.etMetalType, dialogAddCategoryBinding!!.mcvMetalType
        )
        Utils.setTextChangeListener(
            dialogAddCategoryBinding!!.etMetalType,
            dialogAddCategoryBinding!!.tvMetalTypeError
        )

        dialogAddCategoryBinding!!.mcvSave.setOnClickListener {

            val errorValidationModels: MutableList<ValidationModel> = ArrayList()

            errorValidationModels.add(
                ValidationModel(
                    Validation.Type.Empty,
                    dialogAddCategoryBinding!!.etMetalType,
                    dialogAddCategoryBinding!!.tvMetalTypeError
                )
            )

            val validation: Validation? = Validation.instance
            val resultReturn: ResultReturn? =
                validation?.CheckValidation(requireActivity(), errorValidationModels)
            if (resultReturn?.aBoolean == true) {

                if (position != null && categoryUUID != null) {

                    updateCategory(categoryUUID, position)


                } else {
                    addCategory()
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

        dialogAddCategoryBinding!!.mcvCancel.setOnClickListener {
            dialogCategory?.dismiss()
        }

        dialogCategory?.setCancelable(true)
        dialogCategory?.show()

    }

    // Show or hide metal type drop down
    private fun showOrHideMetalTypeDropDown() {

        if (binding.mcvMetalTypeList.visibility == View.VISIBLE) {

            binding.ivMetalType.let { Utils.rotateView(it, 0f) }
            binding.mcvMetalTypeList.let { Utils.collapseView(it) }

        } else {

            binding.ivMetalType.let { Utils.rotateView(it, 180f) }
            binding.mcvMetalTypeList.let { Utils.expandView(it) }

        }
    }

    // Show or hide Stock Location drop down
    private fun showOrHideStockLocationDropDown() {

        if (binding.mcvStockLocationList.visibility == View.VISIBLE) {

            binding.ivStockLocation.let { Utils.rotateView(it, 0f) }
            binding.mcvStockLocationList.let { Utils.collapseView(it) }

        } else {

            binding.ivStockLocation.let { Utils.rotateView(it, 180f) }
            binding.mcvStockLocationList.let { Utils.expandView(it) }

        }
    }

    // Show or hide collection drop down
    private fun showOrHideCollectionDropDown() {

        if (collectionDataList.isNotEmpty()) {

            if (binding.mcvCollectionList.visibility == View.VISIBLE) {

                binding.ivCollection.let { Utils.rotateView(it, 0f) }
                binding.mcvCollectionList.let { Utils.collapseView(it) }

            } else {

                binding.ivCollection.let { Utils.rotateView(it, 180f) }
                binding.mcvCollectionList.let { Utils.expandView(it) }
            }
        } else {

            isCollectionShow = if (isCollectionShow) {
                binding.ivCollection.let { Utils.rotateView(it, 0f) }
                false
            } else {
                binding.ivCollection.let { Utils.rotateView(it, 180f) }
                true
            }
        }
    }

    // Show or hide category drop down
    private fun showOrHideCategoryDropDown() {

        if (binding.mcvCategoryList.visibility == View.VISIBLE) {

            binding.ivCategory.let { Utils.rotateView(it, 0f) }
            binding.mcvCategoryList.let { Utils.collapseView(it) }

        } else {

            binding.ivCategory.let { Utils.rotateView(it, 180f) }
            binding.mcvCategoryList.let { Utils.expandView(it) }

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

    private fun changePage(num: Int) {

        if (productUUIDIndex != -1) {

            //GetProgressBar.getInstance(requireActivity())?.show()

            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.productInventory, true)
                .build()

            if (num > 0) {

                if (ProductUUIDList.getSize() == productUUIDIndex + 1) {

                    (context as DashboardActivity).navController?.navigate(
                        R.id.productInventory,
                        null,
                        navOptions
                    )
                } else {

                    val bundle = Bundle()
                    // Giving the product UUID
                    bundle.putString(
                        Constants.productUUID,
                        ProductUUIDList.getProductUUID(productUUIDIndex + num)
                    )
                    (context as DashboardActivity).navController?.navigate(
                        R.id.productInventory,
                        bundle,
                        navOptions
                    )
                }

            } else {

                val bundle = Bundle()
                // Giving the product UUID
                bundle.putString(
                    Constants.productUUID,
                    ProductUUIDList.getProductUUID(productUUIDIndex + num)
                )

                (context as DashboardActivity).navController?.navigate(
                    R.id.productInventory,
                    bundle,
                    navOptions
                )

            }
        }
    }

    //Handles All the Clicks
    @SuppressLint("NotifyDataSetChanged", "CutPasteId")
    override fun onClick(v: View?) {

        if (v == binding.llMetalType) {
            showOrHideMetalTypeDropDown()
        } else if (v == binding.llStockLocation) {
            showOrHideStockLocationDropDown()
        } else if (v == binding.llCategory) {
            showOrHideCategoryDropDown()
        } else if (v == binding.mcvAddDuplicate) {

            GetProgressBar.getInstance(requireActivity())?.show()

            ProductUUIDList.setStatus(false)

            val bundle = Bundle()
            // Giving the product UUID
            bundle.putString(Constants.productUUID, productDataModel?.productUUID)
            bundle.putBoolean(Constants.addDuplicate, true)

            (context as DashboardActivity).navController?.navigate(
                R.id.productInventory,
                bundle
            )

        } else if (v == binding.mcvRFIDCodeBtn) {

            checkAndroidVersionAndLaunchPermission()

        } else if (v == binding.mcvCollection || v == binding.ivCollection) {
            showOrHideCollectionDropDown()
        } else if (v == binding.mcvAddCategory) {
            showAddOrEditCategoryDialog(null, null)
        } else if (v == binding.mcvSave) {

            GetProgressBar.getInstance(requireActivity())?.show()

            validate(isSaveButtonClicked = true, isNextButtonClicked = false)
        }

        // When Back button clicked
        else if (v == binding.mcvBackBtn) {

            GetProgressBar.getInstance(requireActivity())?.show()

            (requireActivity() as DashboardActivity).navController?.popBackStack()

        } else if (v == binding.mcvAddMetalType) {

            showAddOrEditMetalTypeDialog(null, null)
        } else if (v == binding.mcvAddStockLocation) {
//            showAddOrUpdateStockLocationDialog(null,null)
        } else if (v == binding.mcvBarcodeBtn) {

            if (binding.etBarcode.text?.isNotEmpty() == true) {

                val barcodeBitmap = Utils.genBarcodeBitmap(
                    requireActivity(),
                    binding.etBarcode.text.toString().trim()
                )

                if (barcodeBitmap != null) {

                    barcodeData = binding.etBarcode.text.toString().trim()

                    binding.mcvBarcodeImage.visibility = View.VISIBLE
                    binding.tvBarcodeError.visibility = View.GONE

                    binding.ivBarcodeImage.setImageBitmap(barcodeBitmap)
                    barcodeBitmap.let { barcodeImage.add(it) }

                }
            } else {

                binding.tvBarcodeError.let {
                    Utils.showError(
                        requireActivity(),
                        it, requireActivity().getString(R.string.empty_error)
                    )
                }
            }
        } else if (v == binding.ivCross) {

            barcodeImage.clear()
            binding.etBarcode.setText("")
            binding.mcvBarcodeImage.visibility = View.GONE

        } else if (v == binding.root) {
            hideKeyboard(requireActivity())
        } else if (v == binding.mcvPrevious) {

            GetProgressBar.getInstance(requireActivity())?.show()

            validate(isSaveButtonClicked = false, isNextButtonClicked = false)


        } else if (v == binding.mcvNext) {

            GetProgressBar.getInstance(requireActivity())?.show()

            validate(isSaveButtonClicked = false, isNextButtonClicked = true)


        }

        else if (v == binding.mcvPrint) {

            val itemLayout =
                LayoutInflater.from(requireContext()).inflate(R.layout.item_product_qr, null)


            val weight = binding.etWeight.text.toString()
            val dimension = binding.etCarat.text.toString()
            val price = binding.etPrice.text.toString()

            /*   val customLabelView = itemLayout.findViewById<CustomLabelView>(R.id.customLabelView)
               customLabelView.updateWeightText("W: $weight")
               customLabelView.updateDimensionText("D: $dimension")
               customLabelView.updatePriceText("P: $price")
   */
            val customQRLabelView =
                itemLayout.findViewById<CustomQRViewWithLabel>(R.id.customQRViewWithText)
            customQRLabelView.updateWeightText("W: $weight")
            customQRLabelView.updateDimensionText("D: $dimension")
            customQRLabelView.updatePriceText("P: $price")


            val qrData = binding.etBarcode.text.toString() // Get data from EditText

            customQRLabelView.barcodeText(binding.etBarcode.text.toString())
            // Generate the QR code bitmap
            val qrBitmap = Utils.generateQRCode(qrData) // Generate the QR code from the input data

            /* // Find the CustomQRView and set the bitmap
             val customQRView: CustomQRView =
                 itemLayout.findViewById(R.id.customQRView) // Make sure to use the correct ID of your CustomQRView
             qrBitmap?.let {
                 customQRView.setQRCodeBitmap(it) // Set the generated QR code bitmap to your custom view
             }*/

            val customQRViewWithText: CustomQRViewWithLabel =
                itemLayout.findViewById(R.id.customQRViewWithText) // Make sure to use the correct ID of your CustomQRView
            qrBitmap?.let {
                customQRViewWithText.setQRCodeBitmap(it) // Set the generated QR code bitmap to your custom view
            }

            // Convert layout to bitmap
            val bitmap = Utils.getBitmapFromView(itemLayout)

            // Print layout as PDF
            Utils.printLayout(requireContext(), bitmap)
        }
    }

    override fun onTagRead(tagInfo: UHFTAGInfo) {

        // Handle RFID tag data
        //   Utils.T(requireContext(), "Tag read: ${tagInfo.epc}")
        binding.etRFIDCode.setText(tagInfo.epc)

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