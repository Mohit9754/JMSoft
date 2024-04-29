package com.jmsoft.main.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
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
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.jmsoft.R
import com.jmsoft.Utility.Database.CategoryDataModel
import com.jmsoft.Utility.Database.CollectionDataModel
import com.jmsoft.Utility.Database.MetalTypeDataModel
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.KeyboardUtils.hideKeyboard
import com.jmsoft.basic.UtilityTools.Utils
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
import com.jmsoft.main.`interface`.CollectionStatusCallback
import com.jmsoft.main.`interface`.SelectedCallback
import com.jmsoft.main.model.SelectedCollectionModel

class ProductInventoryFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentProductInventoryBinding

    private var selectedCollectionList = ArrayList<SelectedCollectionModel>()

    private var selectedCollectionUUID = mutableListOf<String>()

    private var selectedCollectionAdapter: SelectedCollectionAdapter? = null

    // for Opening the Camera Dialog
    private var forCameraSettingDialog = 100

    // for Opening the Gallery Dialog
    private var forGallerySettingDialog = 200

    private var productImageView:ImageView? = null

    private var addImageAdapter:AddImageAdapter? = null

    private var productUUID:String? = null

    private var metalTypeDropdownAdapter:MetalTypeDropdownAdapter? = null

    private var maxImageLimit = 5 // Maximum images allowed

    private val selectedProductImage: ArrayList<Any> = ArrayList()

    private val selectedProductImageBitmap: ArrayList<Bitmap> = ArrayList()

    private val barcodeImage:ArrayList<Bitmap> = ArrayList()

    private var metalTypeDropdownList = ArrayList<MetalTypeDataModel>()

    private var selectedMetalTypeUUID:String? = null

    private var dialogAddMetalTypeBinding:DialogAddMetalTypeBinding? = null

    private var dialogAddCategoryBinding:DialogAddMetalTypeBinding? = null

    private var selectedCategoryUUID:String? = null

    private var collectionDropdownAdapter:CollectionDropdownAdapter? = null
    private var categoryDropdownAdapter:CategoryDropdownAdapter? = null

    private var barcodeData:String? = null

    private var categoryDropdownList = ArrayList<CategoryDataModel>()

    private var collectionDataList = ArrayList<CollectionDataModel>()

    private var isCollectionShow = false

    private var prgogressBarDialog:Dialog? = null

    @SuppressLint("NotifyDataSetChanged")
    private var galleryActivityResultLauncher: ActivityResultLauncher<Intent?>? = registerForActivityResult(
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

//            if (selectedUris.size < minImageLimit - selectedImage.size ) {
//
//                Utils.T(requireActivity(), "Please select at least ${minImageLimit - selectedImage.size} images")
//                return@registerForActivityResult
//            }

            if (selectedUris.size > maxImageLimit - selectedProductImage.size){

                Utils.T(requireActivity(), "Maximum ${maxImageLimit - selectedProductImage.size} images allowed")
                return@registerForActivityResult
            }



            // Update selectedImageUris list with valid selections
//            selectedImage.clear()
            selectedProductImage.addAll(selectedUris)


            addImageAdapter?.notifyDataSetChanged()
            binding.tvProductImageError?.visibility = View.GONE

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

    //Camera Permission Launcher
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


    //Gallery  Launcher
//    @SuppressLint("NotifyDataSetChanged")
//    private var galleryActivityResultLauncher: ActivityResultLauncher<Intent?>? =
//        registerForActivityResult(
//            ActivityResultContracts.StartActivityForResult()
//        ) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                val image_uri: Uri? = result.data?.data
//
//                    binding.tvProductImageError?.visibility = View.GONE
//                    productImageView?.setImageURI(image_uri)
//                    productImageView?.drawable?.let { productImageBitmap.add(it.toBitmap()) }
//                    addImageAdapter?.notifyDataSetChanged()
//
//            }
//        }

    //Camera Launcher
    @SuppressLint("NotifyDataSetChanged")
    private var cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result != null) {
            if (result.resultCode == Activity.RESULT_OK) {

                binding.tvProductImageError?.visibility = View.GONE
                productImageView?.setImageBitmap(result.data?.extras?.get("data") as Bitmap?)
                productImageView?.drawable?.let { selectedProductImage.add(it.toBitmap()) }

                addImageAdapter?.notifyDataSetChanged()
                binding.tvProductImageError?.visibility = View.GONE

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding = FragmentProductInventoryBinding.inflate(layoutInflater)

        val progressBarDialog = Utils.pdfProgressDialog(requireActivity())

        //set the Clicks And initialization
        init()

        progressBarDialog.dismiss()

        return binding.root
    }

    private fun setMetalTypeRecyclerView() {

        metalTypeDropdownList = Utils.getAllMetalType()

        metalTypeDropdownAdapter = MetalTypeDropdownAdapter(
            requireActivity(),
            metalTypeDropdownList,
            this,
            object : SelectedCallback {

                override fun selected(data: Any) {

                    val metalTypeDataModel =  data as MetalTypeDataModel

                    selectedMetalTypeUUID = metalTypeDataModel.metalTypeUUID
                    binding.tvMetalType?.text = metalTypeDataModel.metalTypeName
                    binding.tvMetalTypeError?.visibility = View.GONE
                    binding.ivMetalType?.let { Utils.rotateView(it,0f) }
                    binding.mcvMetalTypeList?.let { Utils.collapseView(it) }
                }

                override fun unselect() {

                    selectedMetalTypeUUID = null
                    binding.tvMetalType?.text  = ""
                    binding.tvMetalTypeError?.visibility = View.GONE
//                    binding.mcvMetalTypeList?.visibility = View.GONE
                    showOrHideMetalTypeDropDown()

                }
            }
        )

        if (selectedMetalTypeUUID != null) {
            metalTypeDropdownAdapter?.selectedPosition = metalTypeDropdownList.indexOfFirst { it.metalTypeUUID == selectedMetalTypeUUID }
        }

        binding.rvMetalType?.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        binding.rvMetalType?.adapter = metalTypeDropdownAdapter

    }

    private fun setCategoryRecyclerView() {

        categoryDropdownList = Utils.getAllCategory()

        categoryDropdownAdapter = CategoryDropdownAdapter(
            requireActivity(),
            categoryDropdownList,
            this,
            object :SelectedCallback {

                override fun selected(data: Any) {

                    val categoryDataModel =  data as CategoryDataModel

                    selectedCategoryUUID = categoryDataModel.categoryUUID
                    binding.tvCategory?.text = categoryDataModel.categoryName
                    binding.tvCategoryError?.visibility = View.GONE
                    binding.ivCategory?.let { Utils.rotateView(it,0f) }
                    binding.mcvCategoryList?.let { Utils.collapseView(it) }

                }

                override fun unselect() {

                    selectedCategoryUUID = null
                    binding.tvCategory?.text  = ""
                    binding.tvCategoryError?.visibility = View.GONE

                    showOrHideCategoryDropDown()

                }
            }
        )

        if (selectedCategoryUUID != null) {
            val position = categoryDropdownList.indexOfFirst { it.categoryUUID == selectedCategoryUUID }
            categoryDropdownAdapter?.selectedPosition = position
            binding.tvCategory?.text = categoryDropdownList[position].categoryName
        }

        binding.rvCategory?.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        binding.rvCategory?.adapter = categoryDropdownAdapter

    }

    private fun setCollectionRecyclerView() {

        collectionDataList = Utils.getAllCollection()

        collectionDropdownAdapter = CollectionDropdownAdapter(requireActivity(),
            collectionDataList,
            object : CollectionStatusCallback {

                @SuppressLint("NotifyDataSetChanged")
                override fun collectionSelected(selectedCollectionModel: SelectedCollectionModel) {

                    binding.tvCollection?.visibility = View.GONE
                    binding.tvCollectionError?.visibility = View.GONE

                    if(!selectedCollectionList.contains(selectedCollectionModel)){

                        selectedCollectionList.add(selectedCollectionModel)
                        selectedCollectionAdapter?.notifyDataSetChanged()
                    }

                }

                @SuppressLint("NotifyDataSetChanged")
                override fun collectionUnSelected(selectedCollectionModel: SelectedCollectionModel) {

                    selectedCollectionList.remove(selectedCollectionModel)

                    if (selectedCollectionList.isEmpty()) {
                        binding.tvCollection?.visibility = View.VISIBLE
                    }

                    selectedCollectionAdapter?.notifyDataSetChanged()

                }
            }
        )

        if (selectedCollectionUUID.isNotEmpty()) {
           collectionDropdownAdapter?.selectedCollectionUUID = selectedCollectionUUID
        }

        binding.rvCollectionList?.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)

        binding.rvCollectionList?.adapter = collectionDropdownAdapter

    }

    private fun setSelectedCollectionRecyclerView() {

        selectedCollectionAdapter =
            SelectedCollectionAdapter(requireActivity(), selectedCollectionList, binding)

        binding.rvCollectionSelectedList?.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)

        binding.rvCollectionSelectedList?.adapter = selectedCollectionAdapter

        binding.mcvCollectionList?.visibility  = View.GONE

    }

    fun setProductImageRecyclerView() {

        addImageAdapter = AddImageAdapter(requireActivity(),selectedProductImage,this,selectedProductImageBitmap)
        binding.rvProductImage?.layoutManager = GridLayoutManager(requireActivity(), 3) // Span Count is set to 3
        binding.rvProductImage?.adapter = addImageAdapter
    }

    private fun setFocusChangeListener(){

        binding.etProductName?.let {
            binding.mcvProductName?.let { it1 ->
                Utils.setFocusChangeListener(requireActivity(),
                    it, it1
                )
            }
        }

        binding.etOrigin?.let {
            binding.mcvOrigin?.let { it1 ->
                Utils.setFocusChangeListener(requireActivity(),
                    it, it1
                )
            }
        }

        binding.etWeight?.let {
            binding.mcvWeight?.let { it1 ->
                Utils.setFocusChangeListener(requireActivity(),
                    it, it1
                )
            }
        }

        binding.etCarat?.let {
            binding.mcvCarat?.let { it1 ->
                Utils.setFocusChangeListener(requireActivity(),
                    it, it1
                )
            }
        }

        binding.etPrice?.let {
            binding.mcvPrice?.let { it1 ->
                Utils.setFocusChangeListener(requireActivity(),
                    it, it1
                )
            }
        }

        binding.etDescription?.let {
            binding.mcvDescription?.let { it1 ->
                Utils.setFocusChangeListener(requireActivity(),
                    it, it1
                )
            }
        }

        binding.etRFIDCode?.let {
            binding.mcvRFIDCode?.let { it1 ->
                Utils.setFocusChangeListener(requireActivity(),
                    it, it1
                )
            }
        }

        binding.etBarcode?.let {
            binding.mcvBarcode?.let { it1 ->
                Utils.setFocusChangeListener(requireActivity(),
                    it, it1
                )
            }
        }
    }

    private fun setTextChangeListener(){

        binding.etProductName?.let { binding.tvProductNameError?.let { it1 ->
            Utils.setTextChangeListener(it,
                it1
            )
        } }

        binding.etOrigin?.let { binding.tvOriginError?.let { it1 ->
            Utils.setTextChangeListener(it,
                it1
            )
        } }

        binding.etWeight?.let { binding.tvWeightError?.let { it1 ->
            Utils.setTextChangeListener(it,
                it1
            )
        } }

        binding.etCarat?.let { binding.tvCaratError?.let { it1 ->
            Utils.setTextChangeListener(it,
                it1
            )
        } }

        binding.etPrice?.let { binding.tvPriceError?.let { it1 ->
            Utils.setTextChangeListener(it,
                it1
            )
        } }

        binding.etDescription?.let { binding.tvDescriptionError?.let { it1 ->
            Utils.setTextChangeListener(it,
                it1
            )
        } }

        binding.etRFIDCode?.let { binding.tvRFIDCodeError?.let { it1 ->
            Utils.setTextChangeListener(it,
                it1
            )
        } }
    }

    private fun checkAddOrEditState() {

        productUUID = arguments?.getString(Constants.productUUID)

        if (productUUID != null) {

            val productData = Utils.getProductThroughProductUUID(productUUID!!)

            binding.etProductName?.setText(productData.productName)
            binding.etProductName?.setText(productData.productName)

            binding.mcvMetalTypeList?.visibility = View.VISIBLE
            selectedMetalTypeUUID = productData.metalTypeUUID

            selectedCollectionUUID = productData.collectionUUID?.split(",")?.toMutableList() ?: mutableListOf()

            binding.etOrigin?.setText(productData.productOrigin)
            binding.etWeight?.setText(productData.productWeight.toString())
            binding.etCarat?.setText(productData.productCarat.toString())
            binding.etPrice?.setText(productData.productCost.toString())

//            binding.mcvCategoryList?.visibility = View.VISIBLE
            selectedCategoryUUID = productData.categoryUUID

            binding.etDescription?.setText(productData.productDescription)
            binding.etRFIDCode?.setText(productData.productRFIDCode)
            binding.etRFIDCode?.setText(productData.productRFIDCode)

            binding.mcvBarcodeImage?.visibility  = View.VISIBLE

            val barcodeBitmap = productData.productBarcodeUri?.let {
                Utils.getImageFromInternalStorage(requireActivity(),
                    it
                )
            }
            binding.ivBarcodeImage?.setImageBitmap(barcodeBitmap)
            barcodeBitmap?.let { barcodeImage.add(it) }

            binding.etBarcode?.setText(productData.productBarcodeData)
            barcodeData = productData.productBarcodeData

            val productImageUri = productData.productImageUri?.split(",") ?: listOf()

            for (imageUri in productImageUri) {
                Utils.getImageFromInternalStorage(requireActivity(),imageUri.trim())
                    ?.let { selectedProductImage.add(it) }
            }
        }
    }

    //set the Clicks And initialization
    private fun init() {

        checkAddOrEditState()

        setFocusChangeListener()

        setTextChangeListener()

        setMetalTypeRecyclerView()

        setCategoryRecyclerView()

        setCollectionRecyclerView()

        setSelectedCollectionRecyclerView()

        setProductImageRecyclerView()

        binding.llMetalType?.setOnClickListener(this)

        binding.mcvCollection?.setOnClickListener(this)

        binding.llCategory?.setOnClickListener(this)

        binding.ivCollection?.setOnClickListener(this)

        binding.mcvSave?.setOnClickListener(this)

        // Set Click on Back Button
        binding.mcvBackBtn?.setOnClickListener(this)

        binding.mcvAddMetalType?.setOnClickListener(this)

        binding.mcvBarcodeBtn?.setOnClickListener(this)

        binding.ivCross?.setOnClickListener(this)

        binding.root.setOnClickListener(this)

        binding.mcvAddCategory?.setOnClickListener(this)

        if (productUUID != null) {
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
        dialogBinding.tvMessage.text = requireActivity().getString(R.string.to_proceed_with_adding_or_updating_your_product_n_picture_please_select_an_image_source)

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

    private fun getSelectedCollectionUUID(): String {

        val collectionUUID = ArrayList<String>()

        for (collectionData in selectedCollectionList) {

            collectionData.collectionDataModel.collectionUUID?.let { collectionUUID.add(it) }

        }
        return collectionUUID.joinToString().replace(" ", "")

    }

    private fun getProductImageUri(): String {

        val imageUri = ArrayList<String>()

        for (imageBitmap in selectedProductImageBitmap) {
            imageUri.add(Utils.getPictureUri(requireActivity(),imageBitmap))
        }
        return imageUri.joinToString().replace(" ", "")
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addOrUpdateProduct(productUUID:String?){

        val productDataModel = ProductDataModel()

        productDataModel.productUUID = productUUID ?: Utils.generateUUId()
        productDataModel.productName = binding.etProductName?.text.toString().trim()
        productDataModel.metalTypeUUID = selectedMetalTypeUUID
        productDataModel.collectionUUID = getSelectedCollectionUUID()
        productDataModel.productOrigin = binding.etOrigin?.text.toString().trim()
        productDataModel.productWeight = Utils.roundToTwoDecimalPlaces(binding.etWeight?.text.toString().toDouble())
        productDataModel.productCarat = binding.etCarat?.text.toString().toInt()
        productDataModel.productCost = Utils.roundToTwoDecimalPlaces(binding.etPrice?.text.toString().toDouble())
        productDataModel.categoryUUID = selectedCategoryUUID
        productDataModel.productDescription = binding.etDescription?.text.toString().trim()
        productDataModel.productRFIDCode = binding.etRFIDCode?.text.toString().trim()
        productDataModel.productBarcodeData = barcodeData
        productDataModel.productBarcodeUri = Utils.getPictureUri(requireActivity(), barcodeImage[0])
        productDataModel.productImageUri = getProductImageUri()

        if (productUUID != null) {
            Utils.updateProduct(productDataModel)
            Utils.T(requireActivity(),requireActivity().getString(R.string.updated_successfully))
        }

        else {
            Utils.addProduct(productDataModel)
            Utils.T(requireActivity(),requireActivity().getString(R.string.added_successfully))
        }

        (requireActivity() as DashboardActivity).navController?.popBackStack()

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

    private fun validate(){

        val errorValidationModel: MutableList<ValidationModel> = ArrayList()

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.letterAndDigit, binding.etProductName, binding.tvProductNameError
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.EmptyTextView, binding.tvMetalType, binding.tvMetalTypeError
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.letterAndDigit, binding.etOrigin, binding.tvOriginError
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
                Validation.Type.letterAndDigit, binding.etRFIDCode, binding.tvRFIDCodeError
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.Barcode, barcodeImage.size, binding.tvBarcodeError
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.AtLeastTwo, selectedProductImage.size, binding.tvProductImageError
            )
        )

        val validation: Validation? = Validation.instance

        val resultReturn: ResultReturn? =
            validation?.CheckValidation(requireActivity(), errorValidationModel)
        if (resultReturn?.aBoolean == true) {

            addOrUpdateProduct(productUUID)

        } else {

            prgogressBarDialog?.dismiss()

            resultReturn?.errorTextView?.visibility = View.VISIBLE

            if (resultReturn?.type === Validation.Type.EmptyString) {
                resultReturn.errorTextView?.text = resultReturn.errorMessage
            } else {
                resultReturn?.errorTextView?.text = validation?.errorMessage
                val animation =
                    AnimationUtils.loadAnimation(requireActivity(), R.anim.top_to_bottom)
                resultReturn?.errorTextView?.startAnimation(animation)

                if (validation?.textViewPointer != null ) {

                    if (validation.textViewPointer == binding.tvMetalType) {

                        binding.ivMetalType?.let { Utils.rotateView(it, 0f) }
                        binding.mcvMetalTypeList?.let { Utils.expandView(it) }
                    }
                    else if (validation.textViewPointer == binding.tvCategory){

                        binding.ivCategory?.let { Utils.rotateView(it, 0f) }
                        binding.mcvCategoryList?.let { Utils.expandView(it) }
                    }

                    validation.textViewPointer = null
                }

                else {

                    validation?.EditTextPointer?.requestFocus()

                    val imm =
                        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(validation?.EditTextPointer , InputMethodManager.SHOW_IMPLICIT)

                    validation?.EditTextPointer = null

                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        prgogressBarDialog?.dismiss()
    }

    private fun addCategory(dialog: Dialog) {

        val isCategoryExist =
            Utils.isCategoryExist(Utils.capitalizeData(dialogAddCategoryBinding?.etMetalType?.text.toString()))

        if (isCategoryExist == true) {

            dialogAddCategoryBinding?.let {
                Utils.showError(
                    requireActivity(), it.tvMetalTypeError ,
                    getString(R.string.category_already_exist)
                )
            }

        }
        else {

            val categoryDataModel = CategoryDataModel()
            categoryDataModel.categoryUUID = Utils.generateUUId()
            categoryDataModel.categoryName = Utils.capitalizeData(dialogAddCategoryBinding?.etMetalType?.text.toString())

            Utils.addCategory(categoryDataModel)

            Utils.T(
                requireActivity(),
                requireActivity().getString(R.string.added_successfully)
            )
            setCategoryRecyclerView()

            binding.mcvCategoryList?.visibility  = View.GONE
            showOrHideCategoryDropDown()

            dialog.dismiss()
        }
    }

    private fun addMetalType(dialog: Dialog) {

        val isMetalTypeExist =
            Utils.isMetalTypeExist(Utils.capitalizeData(dialogAddMetalTypeBinding?.etMetalType?.text.toString()))

        if (isMetalTypeExist == true) {

            dialogAddMetalTypeBinding?.let {
                Utils.showError(
                    requireActivity(), it.tvMetalTypeError ,
                    getString(R.string.metal_type_already_exist)
                )
            }
        }
        else {

            val metalTypeDataModel = MetalTypeDataModel()
            metalTypeDataModel.metalTypeUUID = Utils.generateUUId()
            metalTypeDataModel.metalTypeName = Utils.capitalizeData(dialogAddMetalTypeBinding?.etMetalType?.text.toString())

            Utils.addMetalTypeInTheMetalTypeTable(metalTypeDataModel)

            Utils.T(
                requireActivity(),
                requireActivity().getString(R.string.added_successfully)
            )
            setMetalTypeRecyclerView()

            binding.mcvMetalTypeList?.visibility  = View.GONE
            showOrHideMetalTypeDropDown()

            dialog.dismiss()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun showAddOrEditMetalTypeDialog(position: Int?,metalTypeUUID: String?) {

        val dialog = Dialog(requireActivity())
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        dialogAddMetalTypeBinding = DialogAddMetalTypeBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(dialogAddMetalTypeBinding!!.root)

        if (position != null && metalTypeUUID != null ) {
            dialogAddMetalTypeBinding!!.tvTitle.text = getString(R.string.edit_metal_type)
            dialogAddMetalTypeBinding!!.etMetalType.setText(metalTypeDropdownList[position].metalTypeName)
        } else {
            dialogAddMetalTypeBinding!!.tvTitle.text = requireActivity().getString(R.string.add_metal_type)
        }

        dialogAddMetalTypeBinding!!.tvName.text = requireActivity().getString(R.string.metal_type)
        dialogAddMetalTypeBinding!!.etMetalType.hint = requireActivity().getString(R.string.enter_metal_type)

        Utils.setFocusChangeListener(requireActivity(),
            dialogAddMetalTypeBinding!!.etMetalType, dialogAddMetalTypeBinding!!.mcvMetalType)
        Utils.setTextChangeListener(dialogAddMetalTypeBinding!!.etMetalType,
            dialogAddMetalTypeBinding!!.tvMetalTypeError)

        dialogAddMetalTypeBinding!!.mcvSave.setOnClickListener {

            val errorValidationModels: MutableList<ValidationModel> = ArrayList()

            errorValidationModels.add(
                ValidationModel(
                    Validation.Type.Empty, dialogAddMetalTypeBinding!!.etMetalType, dialogAddMetalTypeBinding!!.tvMetalTypeError
                )
            )

            val validation: Validation? = Validation.instance
            val resultReturn: ResultReturn? =
                validation?.CheckValidation(requireActivity(), errorValidationModels)
            if (resultReturn?.aBoolean == true) {

                if (position != null && metalTypeUUID != null) {

                    Utils.updateMetalType(
                        metalTypeUUID,
                        Utils.capitalizeData(dialogAddMetalTypeBinding!!.etMetalType.text.toString())
                    )
                    metalTypeDropdownList[position].metalTypeName =
                        Utils.capitalizeData(dialogAddMetalTypeBinding!!.etMetalType.text.toString())
                    metalTypeDropdownAdapter?.notifyItemChanged(position)

                    dialog.dismiss()


                } else {

                    addMetalType(dialog)
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
                    imm.showSoftInput(validation?.EditTextPointer , InputMethodManager.SHOW_IMPLICIT)

                }
            }
        }

        dialogAddMetalTypeBinding!!.mcvCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.show()
    }

    fun showAddOrEditCategoryDialog(position: Int?, categoryUUID: String?) {

        val dialog = Dialog(requireActivity())
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        dialogAddCategoryBinding = DialogAddMetalTypeBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(dialogAddCategoryBinding!!.root)

        if (position != null && categoryUUID != null ) {
            dialogAddCategoryBinding!!.tvTitle.text = getString(R.string.edit_category)
            dialogAddCategoryBinding!!.etMetalType.setText(categoryDropdownList[position].categoryName)
        } else {
            dialogAddCategoryBinding!!.tvTitle.text = requireActivity().getString(R.string.add_category)
        }

        dialogAddCategoryBinding!!.tvName.text = requireActivity().getString(R.string.category)
        dialogAddCategoryBinding!!.etMetalType.hint = requireActivity().getString(R.string.enter_category_name)

        Utils.setFocusChangeListener(requireActivity(),
            dialogAddCategoryBinding!!.etMetalType, dialogAddCategoryBinding!!.mcvMetalType)
        Utils.setTextChangeListener(dialogAddCategoryBinding!!.etMetalType,
            dialogAddCategoryBinding!!.tvMetalTypeError)

        dialogAddCategoryBinding!!.mcvSave.setOnClickListener {

            val errorValidationModels: MutableList<ValidationModel> = ArrayList()

            errorValidationModels.add(
                ValidationModel(
                    Validation.Type.Empty, dialogAddCategoryBinding!!.etMetalType, dialogAddCategoryBinding!!.tvMetalTypeError
                )
            )

            val validation: Validation? = Validation.instance
            val resultReturn: ResultReturn? =
                validation?.CheckValidation(requireActivity(), errorValidationModels)
            if (resultReturn?.aBoolean == true) {

                if (position != null && categoryUUID != null) {

                    val categoryDataModel = CategoryDataModel()
                    categoryDataModel.categoryUUID = categoryUUID
                    categoryDataModel.categoryName = Utils.capitalizeData(dialogAddCategoryBinding!!.etMetalType.text.toString())

                    Utils.updateCategory(categoryDataModel)

                    categoryDropdownList[position].categoryName =
                        Utils.capitalizeData(dialogAddCategoryBinding!!.etMetalType.text.toString())
                    categoryDropdownAdapter?.notifyItemChanged(position)

                    dialog.dismiss()

                } else {
                    addCategory(dialog)
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
                    imm.showSoftInput(validation?.EditTextPointer , InputMethodManager.SHOW_IMPLICIT)

                }
            }
        }

        dialogAddCategoryBinding!!.mcvCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.show()

    }

    private fun genBarcodeBitmap(data:String):Bitmap? {

        // Getting input value from the EditText
        if (data.isNotEmpty()) {
            // Initializing a MultiFormatWriter to encode the input value
            val mwriter = MultiFormatWriter()

            try {
                // Generating a barcode matrix
                val matrix = mwriter.encode(data, BarcodeFormat.CODE_128, 60, 30)

                // Creating a bitmap to represent the barcode
                val bitmap = Bitmap.createBitmap(60, 30, Bitmap.Config.RGB_565)

                // Iterating through the matrix and set pixels in the bitmap
                for (i in 0 until 60) {
                    for (j in 0 until 30) {
                        bitmap.setPixel(i, j, if (matrix[i, j]) Color.BLACK else Color.WHITE)
                    }
                }
                // Setting the bitmap as the image resource of the ImageView
                return  bitmap

            } catch (e: Exception) {

                Utils.T(requireActivity(), "Exception $e")

            }
        } else {
            // Showing an error message if the EditText is empty
        }
        return null
    }

    private fun showOrHideMetalTypeDropDown(){

        if (binding.mcvMetalTypeList?.visibility == View.VISIBLE) {

            binding.ivMetalType?.let { Utils.rotateView(it, 0f) }
            binding.mcvMetalTypeList?.let { Utils.collapseView(it) }

        } else {

            binding.ivMetalType?.let { Utils.rotateView(it, 180f) }
            binding.mcvMetalTypeList?.let { Utils.expandView(it) }

        }
    }

    private fun showOrHideCollectionDropDown() {

        if (collectionDataList.isNotEmpty()) {

            if (binding.mcvCollectionList?.visibility == View.VISIBLE) {

                binding.ivCollection?.let { Utils.rotateView(it, 0f) }
                binding.mcvCollectionList?.let { Utils.collapseView(it) }

            } else {

                binding.ivCollection?.let { Utils.rotateView(it, 180f) }
                binding.mcvCollectionList?.let { Utils.expandView(it) }
            }
        }

        else {

            if (isCollectionShow) {
                binding.ivCollection?.let { Utils.rotateView(it, 0f) }
                isCollectionShow = false
            }
            else {
                binding.ivCollection?.let { Utils.rotateView(it, 180f) }
                isCollectionShow = true
            }
        }
    }

    private fun showOrHideCategoryDropDown() {

        if (binding.mcvCategoryList?.visibility == View.VISIBLE) {

            binding.ivCategory?.let { Utils.rotateView(it, 0f) }
            binding.mcvCategoryList?.let { Utils.collapseView(it) }

        } else {

            binding.ivCategory?.let { Utils.rotateView(it, 180f) }
            binding.mcvCategoryList?.let { Utils.expandView(it) }

        }
    }

    //Handles All the Clicks
    @SuppressLint("NotifyDataSetChanged")
    override fun onClick(v: View?) {

        if (v == binding.llMetalType) {
            showOrHideMetalTypeDropDown()
        }

        else if (v == binding.llCategory) {
            showOrHideCategoryDropDown()
        }

        else if (v == binding.mcvCollection || v == binding.ivCollection) {
            showOrHideCollectionDropDown()
        }

        else if(v == binding.mcvAddCategory) {
            showAddOrEditCategoryDialog(null,null)
        }

        else if (v == binding.mcvSave) {

            prgogressBarDialog = Utils.initProgressDialog(requireActivity())
            validate()
        }

        // When Back button clicked
        else if (v == binding.mcvBackBtn) {
            (requireActivity() as DashboardActivity).navController?.popBackStack()
        }

        else if (v == binding.mcvAddMetalType) {

            showAddOrEditMetalTypeDialog(null,null)
        }

        else if (v == binding.mcvBarcodeBtn){

            if (binding.etBarcode?.text?.isNotEmpty() == true){

                if (binding.etBarcode != null && binding.ivBarcodeImage != null ){

                    val barcodeBitmap = genBarcodeBitmap(binding.etBarcode!!.text.toString().trim())

                    if (barcodeBitmap != null) {

                        barcodeData = binding.etBarcode!!.text.toString().trim()

                        binding.mcvBarcodeImage?.visibility  = View.VISIBLE
                        binding.tvBarcodeError?.visibility = View.GONE

                        binding.ivBarcodeImage!!.setImageBitmap(barcodeBitmap)
                        barcodeBitmap.let { barcodeImage.add(it) }

                    }
                }
            }
            else {

                binding.tvBarcodeError?.let {
                    Utils.showError(requireActivity(),
                        it,requireActivity().getString(R.string.empty_error))
                }
            }
        }

        else if (v == binding.ivCross){

            barcodeImage.clear()
            binding.etBarcode?.setText("")
            binding.mcvBarcodeImage?.visibility = View.GONE

        }

        else if (v == binding.root){

            hideKeyboard(requireActivity())
        }
    }
}