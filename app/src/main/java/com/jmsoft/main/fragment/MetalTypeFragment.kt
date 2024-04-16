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
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.Utility.Database.ProductDataModel
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Constants.Companion.category
import com.jmsoft.basic.UtilityTools.Constants.Companion.collection
import com.jmsoft.basic.UtilityTools.Constants.Companion.metalType
import com.jmsoft.basic.UtilityTools.Constants.Companion.product
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.basic.validation.ResultReturn
import com.jmsoft.basic.validation.Validation
import com.jmsoft.basic.validation.ValidationModel
import com.jmsoft.databinding.DialogAddMetalTypeBinding
import com.jmsoft.databinding.DialogOpenSettingBinding
import com.jmsoft.databinding.FragmentMetalTypeBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.CategoryAdapter
import com.jmsoft.main.adapter.CollectionAdapter
import com.jmsoft.main.adapter.MetalTypeAdapter
import com.jmsoft.main.adapter.ProductListAdapter
import com.jmsoft.main.`interface`.EditInventoryCallback

class MetalTypeFragment : Fragment(),View.OnClickListener {

    private lateinit var binding: FragmentMetalTypeBinding

    private val metalTypeList = ArrayList<String>()

    private val collectionList = ArrayList<String>()

    private val categoryList = ArrayList<String>()

    private val productList = ArrayList<ProductDataModel>()

    private var metalTypeAdapter:MetalTypeAdapter? = null

    private var collectionAdapter:CollectionAdapter? = null

    private var categoryAdapter:CategoryAdapter? = null

    private var productListAdapter:ProductListAdapter? = null

    private var fragmentState:String? = null

    // for Opening the Camera Dialog
    private var forCameraSettingDialog = 100

    // for Opening the Gallery Dialog
    private var forGallerySettingDialog = 200

    private lateinit var editProfileDialog: Dialog

    private lateinit var dialogMetalBinding:DialogAddMetalTypeBinding

    //Gallery Permission Launcher
    private var galleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean? ->
        if (isGranted == true) {

            editProfileDialog.dismiss()
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryActivityResultLauncher?.launch(galleryIntent)

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)) {

                    editProfileDialog.dismiss()
                    showOpenSettingDialog(forGallerySettingDialog)
                }
            } else {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    editProfileDialog.dismiss()
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
            editProfileDialog.dismiss()

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraActivityResultLauncher.launch(cameraIntent)
        } else {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                editProfileDialog.dismiss()
                showOpenSettingDialog(forCameraSettingDialog)
            }
        }
    }

    //Gallery  Launcher
    private var galleryActivityResultLauncher: ActivityResultLauncher<Intent?>? =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val image_uri: Uri? = result.data?.data

                dialogMetalBinding.ivCollectionImage.setImageURI(image_uri)

//                updateProfile(binding.ivProfile?.drawable?.toBitmap())

            }
        }

    //Camera Launcher
    private var cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result != null) {
            if (result.resultCode == Activity.RESULT_OK) {
                dialogMetalBinding.ivCollectionImage.setImageBitmap(result.data?.extras?.get("data") as Bitmap?)
//                updateProfile(binding.ivProfile?.drawable?.toBitmap())

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentMetalTypeBinding.inflate(layoutInflater)

        //set the Clicks And initialization
        init()

        return binding.root
    }

    // Edit Profile Dialog
    private fun showImageSelectionDialog() {

        editProfileDialog = Dialog(requireActivity())
        editProfileDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        editProfileDialog.setCanceledOnTouchOutside(true)
        editProfileDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        editProfileDialog.setContentView(R.layout.dialog_profile)
        editProfileDialog.findViewById<MaterialCardView>(R.id.mcvCamera).setOnClickListener {

            //Camera Launcher
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)

        }
        editProfileDialog.findViewById<MaterialCardView>(R.id.mcvGallery).setOnClickListener {

            //Gallery Launcher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

        }
        editProfileDialog.setCancelable(true)
        editProfileDialog.show()
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

    // Set Metal Type Recycler View
    private fun setMetalTypeRecyclerView(){

        metalTypeList.add("Gold")
        metalTypeList.add("Silver")
        metalTypeList.add("Diamond")
        metalTypeList.add("Gold")
        metalTypeList.add("Silver")
        metalTypeList.add("Diamond")
        metalTypeList.add("Gold")
        metalTypeList.add("Silver")
        metalTypeList.add("Diamond")
        metalTypeList.add("Gold")
        metalTypeList.add("Silver")
        metalTypeList.add("Diamond")
        metalTypeList.add("Gold")
        metalTypeList.add("Silver")
        metalTypeList.add("Diamond")
        metalTypeList.add("Gold")
        metalTypeList.add("Silver")
        metalTypeList.add("Diamond")
        metalTypeList.add("Gold")
        metalTypeList.add("Silver")
        metalTypeList.add("Diamond")
        metalTypeList.add("Gold")
        metalTypeList.add("Silver")
        metalTypeList.add("Diamond")
        metalTypeList.add("Gold")
        metalTypeList.add("Silver")
        metalTypeList.add("Diamond")

        metalTypeAdapter = MetalTypeAdapter(requireActivity(),metalTypeList, object : EditInventoryCallback {

            override fun editInventory(position: Int) {

                showInventoryDialog(position)
            }
        })

        binding.rvMetalType?.layoutManager = GridLayoutManager(requireActivity(), 2) // Span Count is set to 3
        binding.rvMetalType?.adapter = metalTypeAdapter
    }


    // Set Collection Recycler View
    private fun setCollectionRecyclerView() {

        collectionList.add("Royal Gold Ring")
        collectionList.add("Royal Gold Ring")
        collectionList.add("Royal Gold Ring")
        collectionList.add("Royal Gold Ring")
        collectionList.add("Royal Gold Ring")
        collectionList.add("Royal Gold Ring")
        collectionList.add("Royal Gold Ring")
        collectionList.add("Royal Gold Ring")
        collectionList.add("Royal Gold Ring")
        collectionList.add("Royal Gold Ring")
        collectionList.add("Royal Gold Ring")
        collectionList.add("Royal Gold Ring")
        collectionList.add("Royal Gold Ring")
        collectionList.add("Royal Gold Ring")
        collectionList.add("Royal Gold Ring")
        collectionList.add("Royal Gold Ring")
        collectionList.add("Royal Gold Ring")

        collectionAdapter = CollectionAdapter(requireActivity(),collectionList, object : EditInventoryCallback {

            override fun editInventory(position: Int) {

                showInventoryDialog(position)
            }
        })

        binding.rvMetalType?.layoutManager = GridLayoutManager(requireActivity(), 2) // Span Count is set to 3
        binding.rvMetalType?.adapter = collectionAdapter

    }


    //setting the selector on material card view
    private fun setFocusChangeLis(editText: EditText, materialCardView: MaterialCardView) {

        editText.setOnFocusChangeListener { _, hasFocus ->

            if (hasFocus) {
                materialCardView.strokeColor = requireActivity().getColor(R.color.theme)
            } else {
                materialCardView.strokeColor = requireActivity().getColor(R.color.text_hint)
            }
        }
    }

    // Add Metal Type Dialog
    @SuppressLint("NotifyDataSetChanged")
    private fun showInventoryDialog(position: Int?) {

        val dialog = Dialog(requireActivity())
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        dialogMetalBinding = DialogAddMetalTypeBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(dialogMetalBinding.root)

        if (fragmentState == metalType){

            if (position != null){
                dialogMetalBinding.tvTitle.text = getString(R.string.edit_metal_type)
            } else {
                dialogMetalBinding.tvTitle.text = requireActivity().getString(R.string.add_collection)
            }

            dialogMetalBinding.tvName.text = requireActivity().getString(R.string.metal_type)
            dialogMetalBinding.etMetalType.hint = requireActivity().getString(R.string.enter_metal_type)

        }
        else if (fragmentState == collection) {

            if (position != null){

                dialogMetalBinding.tvTitle.text = getString(R.string.update_collection)

            }else{
                dialogMetalBinding.tvTitle.text = requireActivity().getString(R.string.add_collection)
            }

            dialogMetalBinding.tvName.text = requireActivity().getString(R.string.collection)
            dialogMetalBinding.etMetalType.hint = getString(R.string.enter_collection_name)
            dialogMetalBinding.llAddImage.visibility = View.VISIBLE

            dialogMetalBinding.llCollectionImage.setOnClickListener {
                showImageSelectionDialog()
            }

        }

        else if (fragmentState == category){

            if (position != null){
                dialogMetalBinding.tvTitle.text = getString(R.string.edit_category)
            }
            else {
                dialogMetalBinding.tvTitle.text = requireActivity().getString(R.string.add_category)
            }

            dialogMetalBinding.tvName.text = requireActivity().getString(R.string.category)
            dialogMetalBinding.etMetalType.hint = requireActivity().getString(R.string.enter_category_name)

        }


        setFocusChangeLis(dialogMetalBinding.etMetalType,dialogMetalBinding.mcvMetalType)

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

                if (fragmentState == metalType){

                    if (position != null){

                        metalTypeList[position] = dialogMetalBinding.etMetalType.text.toString().trim()
                        metalTypeAdapter?.notifyItemChanged(position)

                    } else {

                        metalTypeList.add(dialogMetalBinding.etMetalType.text.toString().trim())
                        metalTypeAdapter?.notifyDataSetChanged()
                    }

                }
                else if (fragmentState == collection){

                    if (position != null){

                        collectionList[position] = dialogMetalBinding.etMetalType.text.toString().trim()
                        collectionAdapter?.notifyItemChanged(position)

                    } else {
                        collectionList.add(dialogMetalBinding.etMetalType.text.toString().trim())
                        collectionAdapter?.notifyDataSetChanged()
                    }

                }

                else if (fragmentState == category){

                    categoryList.add(dialogMetalBinding.etMetalType.text.toString().trim())
                    categoryAdapter?.notifyDataSetChanged()
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

    private fun checkState(){

        // getting the state
        fragmentState = arguments?.getString(Constants.state)

        if (fragmentState == metalType){

            binding.tvTitle?.text = requireActivity().getString(R.string.metal_type)
            binding.tvButtonName?.text = requireActivity().getString(R.string.add_metal_type)

            // Set Metal Type Recycler View
            setMetalTypeRecyclerView()
        }

        else if (fragmentState == collection) {

            binding.tvTitle?.text = requireActivity().getString(R.string.collection)
            binding.tvButtonName?.text = requireActivity().getString(R.string.add_collection)

            // Set Collection Recycler View
            setCollectionRecyclerView()

        }

        else if (fragmentState == category) {

            binding.tvTitle?.text = requireActivity().getString(R.string.category)
            binding.tvButtonName?.text = getString(R.string.add_category)

            // Set Collection Recycler View
            setCategoryRecyclerView()
        }

        else if (fragmentState == product){

            binding.tvTitle?.text = requireActivity().getString(R.string.product)
            binding.tvButtonName?.text = getString(R.string.add_product)
            binding.mcvProductDetailName?.visibility = View.VISIBLE

            // Set Product Recycler View
            setProductRecyclerView()

        }

    }

    // Set Product Recycler View
    private fun setProductRecyclerView(){


        productListAdapter = ProductListAdapter(requireActivity(), arrayListOf("first","second","third","foureht","fifth"))

        binding.rvMetalType?.layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        binding.rvMetalType?.adapter = productListAdapter

    }

    private fun setCategoryRecyclerView(){

        categoryList.add("Ring")
        categoryList.add("Earing")
        categoryList.add("Necklace")
        categoryList.add("Ring")
        categoryList.add("Earing")
        categoryList.add("Necklace")
        categoryList.add("Ring")
        categoryList.add("Earing")
        categoryList.add("Necklace")
        categoryList.add("Ring")
        categoryList.add("Earing")
        categoryList.add("Necklace")
        categoryList.add("Ring")
        categoryList.add("Earing")
        categoryList.add("Necklace")
        categoryList.add("Ring")
        categoryList.add("Earing")
        categoryList.add("Necklace")
        categoryList.add("Ring")
        categoryList.add("Earing")
        categoryList.add("Necklace")
        categoryList.add("Ring")
        categoryList.add("Earing")
        categoryList.add("Necklace")
        categoryList.add("Ring")
        categoryList.add("Earing")
        categoryList.add("Necklace")

        categoryAdapter = CategoryAdapter(requireActivity(),categoryList,object : EditInventoryCallback {

            override fun editInventory(position: Int) {
                showInventoryDialog(position)
            }

        })
        binding.rvMetalType?.layoutManager = GridLayoutManager(requireActivity(), 2) // Span Count is set to 3
        binding.rvMetalType?.adapter = categoryAdapter
    }

    //set the Clicks And initialization
    private fun init() {

        checkState()

        // Set Click on Back Button
        binding.mcvBackBtn?.setOnClickListener(this)

        // Set Click on Add Metal Type Button
        binding.mcvAddMetalType?.setOnClickListener(this)

    }

    override fun onClick(v: View?) {

        // When Back button clicked
        if (v == binding.mcvBackBtn) {
            (requireActivity() as DashboardActivity).navController?.popBackStack()
        }

        // When Add Metal type Button click
        else if(v == binding.mcvAddMetalType){
            showInventoryDialog(null)
        }
    }
}