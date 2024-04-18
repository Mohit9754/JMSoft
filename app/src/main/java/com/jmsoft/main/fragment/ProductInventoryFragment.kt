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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.jmsoft.R
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.basic.validation.ResultReturn
import com.jmsoft.basic.validation.Validation
import com.jmsoft.basic.validation.ValidationModel
import com.jmsoft.databinding.DialogAddMetalTypeBinding
import com.jmsoft.databinding.DialogOpenSettingBinding
import com.jmsoft.databinding.FragmentProductInventoryBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.AddImageAdapter
import com.jmsoft.main.adapter.CategoryDropdownAdapter
import com.jmsoft.main.adapter.CollectionDropdownAdapter
import com.jmsoft.main.adapter.MetalTypeDropdownAdapter
import com.jmsoft.main.adapter.SelectedCollectionAdapter
import com.jmsoft.main.`interface`.CollectionStatusCallback

class ProductInventoryFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentProductInventoryBinding

    private var selectedCollectionList = ArrayList<String>()

    private var selectedCollectionAdapter: SelectedCollectionAdapter? = null

    // for Opening the Camera Dialog
    private var forCameraSettingDialog = 100

    // for Opening the Gallery Dialog
    private var forGallerySettingDialog = 200

    private var productImageView:ImageView? = null

    private var addImageAdapter:AddImageAdapter? = null

    private var fragmentState:String? = null

    private var metalTypeListAdapter:MetalTypeDropdownAdapter? = null

    private var maxImageLimit = 5 // Maximum images allowed

    private val selectedImage: ArrayList<Any> = ArrayList()

    private val barcodeImage:ArrayList<Bitmap> = ArrayList()

    private val metalTypeList = ArrayList<String>()

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

            if (selectedUris.size > maxImageLimit - selectedImage.size){

                Utils.T(requireActivity(), "Maximum ${maxImageLimit - selectedImage.size} images allowed")
                return@registerForActivityResult
            }



            // Update selectedImageUris list with valid selections
//            selectedImage.clear()
            selectedImage.addAll(selectedUris)


            addImageAdapter?.notifyDataSetChanged()

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
                productImageView?.drawable?.let { selectedImage.add(it.toBitmap()) }

                addImageAdapter?.notifyDataSetChanged()

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
        init()

        return binding.root
    }

    private fun setMetalTypeRecyclerView() {

        metalTypeList.add("Silver")
        metalTypeList.add("Platinum")
        metalTypeList.add("Gold")
        metalTypeList.add("Diamond")

        metalTypeListAdapter = MetalTypeDropdownAdapter(
            requireActivity(),
            metalTypeList,
            binding,
            this
        )

        binding.rvMetalType?.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        binding.rvMetalType?.adapter = metalTypeListAdapter

    }

    private fun setCategoryRecyclerView() {

        val categoryAdapter = CategoryDropdownAdapter(
            requireActivity(),
            arrayListOf("Ring", "Earrings", "necklace", "Bangle"),
            binding
        )

        binding.rvCategory?.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        binding.rvCategory?.adapter = categoryAdapter

    }

    private fun setCollectionRecyclerView() {

        val collectionAdapter = CollectionDropdownAdapter(requireActivity(),
            arrayListOf("Wedding", "engagement", "anniversary", "Bangle"),

            object : CollectionStatusCallback {

                @SuppressLint("NotifyDataSetChanged")
                override fun collectionSelected(collectionName: String) {

                    binding.tvCollection?.visibility = View.GONE
                    binding.tvCollectionError?.visibility = View.GONE

                    selectedCollectionList.add(collectionName)
                    selectedCollectionAdapter?.notifyDataSetChanged()

                    Utils.E("$collectionName")
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun collectionUnSelected(collectionName: String) {

                    selectedCollectionList.remove(collectionName)

                    if (selectedCollectionList.size == 0) {
                        binding.tvCollection?.visibility = View.VISIBLE
                    }

                    selectedCollectionAdapter?.notifyDataSetChanged()

                }
            }
        )

        binding.rvCollectionList?.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)

        binding.rvCollectionList?.adapter = collectionAdapter

    }


    private fun setSelectedCollectionRecyclerView() {

        selectedCollectionAdapter =
            SelectedCollectionAdapter(requireActivity(), selectedCollectionList, binding)

        binding.rvCollectionSelectedList?.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)

        binding.rvCollectionSelectedList?.adapter = selectedCollectionAdapter

    }

    fun setProductImageRecyclerView(){

        addImageAdapter = AddImageAdapter(requireActivity(),selectedImage,this)
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

        binding.etCost?.let {
            binding.mcvCost?.let { it1 ->
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

        binding.etCost?.let { binding.tvCostError?.let { it1 ->
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

    private fun checkState(){
        fragmentState = arguments?.getString(Constants.state)
    }

    //set the Clicks And initialization
    private fun init() {

        checkState()

        setFocusChangeListener()

        setTextChangeListener()

        setMetalTypeRecyclerView()

        setCategoryRecyclerView()

        setCollectionRecyclerView()

        setSelectedCollectionRecyclerView()

        setProductImageRecyclerView()

        binding.ivMetalType?.setOnClickListener(this)

        binding.ivCategory?.setOnClickListener(this)

        binding.ivCollection?.setOnClickListener(this)

        binding.mcvSave?.setOnClickListener(this)

        // Set Click on Back Button
        binding.mcvBackBtn?.setOnClickListener(this)

        binding.mcvAddMetalType?.setOnClickListener(this)

        binding.mcvBarcodeBtn?.setOnClickListener(this)

        binding.ivCross?.setOnClickListener(this)

    }


    // Edit Profile Dialog
    fun showImageSelectionDialog(imageView: ImageView?) {
        productImageView = imageView

        val dialog = Dialog(requireActivity())
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_profile)
        dialog.findViewById<MaterialCardView>(R.id.mcvCamera).setOnClickListener {

            dialog.dismiss()

            //Camera Launcher
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)

        }
        dialog.findViewById<MaterialCardView>(R.id.mcvGallery).setOnClickListener {

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
                Validation.Type.EmptyArrayList, selectedCollectionList.size, binding.tvCollectionError
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.Empty, binding.etOrigin, binding.tvOriginError
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
                Validation.Type.Empty, binding.etRFIDCode, binding.tvRFIDCodeError
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.Barcode, barcodeImage.size, binding.tvBarcodeError
            )
        )

        errorValidationModel.add(
            ValidationModel(
                Validation.Type.AtLeastTwo, selectedImage.size, binding.tvProductImageError
            )
        )

        val validation: Validation? = Validation.instance

        val resultReturn: ResultReturn? =
            validation?.CheckValidation(requireActivity(), errorValidationModel)
        if (resultReturn?.aBoolean == true) {



            Utils.T(requireActivity(),"Added Successfully")



        } else {

            resultReturn?.errorTextView?.visibility = View.VISIBLE

            if (resultReturn?.type === Validation.Type.EmptyString) {
                resultReturn.errorTextView?.text = resultReturn.errorMessage
            } else {
                resultReturn?.errorTextView?.text = validation?.errorMessage
                val animation =
                    AnimationUtils.loadAnimation(requireActivity(), R.anim.top_to_bottom)
                resultReturn?.errorTextView?.startAnimation(animation)
                validation?.EditTextPointer?.requestFocus()

                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(validation?.EditTextPointer, InputMethodManager.SHOW_IMPLICIT)
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun showAddOrEditMetalTypeDialog(position: Int?) {

        val dialog = Dialog(requireActivity())
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dialogMetalBinding = DialogAddMetalTypeBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(dialogMetalBinding.root)

        if (position != null){
            dialogMetalBinding.tvTitle.text = getString(R.string.edit_metal_type)
            dialogMetalBinding.etMetalType.setText(metalTypeList[position])
        } else {
            dialogMetalBinding.tvTitle.text = requireActivity().getString(R.string.add_metal_type)
        }

        dialogMetalBinding.tvName.text = requireActivity().getString(R.string.metal_type)
        dialogMetalBinding.etMetalType.hint = requireActivity().getString(R.string.enter_metal_type)

        Utils.setFocusChangeListener(requireActivity(),dialogMetalBinding.etMetalType,dialogMetalBinding.mcvMetalType)
        Utils.setTextChangeListener(dialogMetalBinding.etMetalType,dialogMetalBinding.tvMetalTypeError)

        dialogMetalBinding.mcvSave.setOnClickListener {

            val errorValidationModels: MutableList<ValidationModel> = ArrayList()

            errorValidationModels.add(
                ValidationModel(
                    Validation.Type.Empty, dialogMetalBinding.etMetalType, dialogMetalBinding.tvMetalTypeError
                )
            )

            val validation: Validation? = Validation.instance
            val resultReturn: ResultReturn? =
                validation?.CheckValidation(requireActivity(), errorValidationModels)
            if (resultReturn?.aBoolean == true) {

                dialog.dismiss()

                if (position != null) {

                    metalTypeList[position] = dialogMetalBinding.etMetalType.text.toString().trim()
                    metalTypeListAdapter?.notifyItemChanged(position)

                } else {

                    metalTypeList.add(dialogMetalBinding.etMetalType.text.toString().trim())
                    metalTypeListAdapter?.notifyDataSetChanged()
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

        dialogMetalBinding.mcvCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.show()

    }

    private fun genBarcodeBitmap(data:String, img: ImageView):Bitmap? {

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




//Handles All the Clicks
    @SuppressLint("NotifyDataSetChanged")
    override fun onClick(v: View?) {

        if (v == binding.ivMetalType) {

            if (binding.mcvMetalTypeList?.visibility == View.VISIBLE) {

                binding.ivMetalType?.let { Utils.rotateView(it, 180f) }
                binding.mcvMetalTypeList?.let { Utils.collapseView(it) }

            } else {

                binding.ivMetalType?.let { Utils.rotateView(it, 0f) }
                binding.mcvMetalTypeList?.let { Utils.expandView(it) }


            }

        }

        else if (v == binding.ivCategory) {

            if (binding.mcvCategoryList?.visibility == View.VISIBLE) {

                binding.ivCategory?.let { Utils.rotateView(it, 180f) }
                binding.mcvCategoryList?.let { Utils.collapseView(it) }

            } else {

                binding.ivCategory?.let { Utils.rotateView(it, 0f) }
                binding.mcvCategoryList?.let { Utils.expandView(it) }
//                metalTypeListAdapter?.notifyDataSetChanged()
            }
        }

        else if (v == binding.ivCollection) {

            if (binding.mcvCollectionList?.visibility == View.VISIBLE) {

                binding.ivCollection?.let { Utils.rotateView(it, 180f) }
                binding.mcvCollectionList?.let { Utils.collapseView(it) }

            } else {

                binding.ivCollection?.let { Utils.rotateView(it, 0f) }
                binding.mcvCollectionList?.let { Utils.expandView(it) }
            }
        }

        else if (v == binding.mcvSave){

            validate()
        }

        // When Back button clicked
        else if (v == binding.mcvBackBtn) {
            (requireActivity() as DashboardActivity).navController?.popBackStack()
        }

        else if (v == binding.mcvAddMetalType) {

            showAddOrEditMetalTypeDialog(null)
        }

        else if (v == binding.mcvBarcodeBtn){

            if (binding.etBarcode?.text?.isNotEmpty() == true){

                if (binding.etBarcode != null && binding.ivBarcodeImage != null ){

                    val barcodeBitmap = genBarcodeBitmap(binding.etBarcode!!.text.toString().trim(),
                        binding.ivBarcodeImage!!
                    )
                    if (barcodeBitmap != null) {

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


    }
}