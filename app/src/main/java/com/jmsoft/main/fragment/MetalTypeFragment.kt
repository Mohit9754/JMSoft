@file:Suppress("DEPRECATION")

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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.utility.database.CategoryDataModel
import com.jmsoft.utility.database.CollectionDataModel
import com.jmsoft.utility.database.MetalTypeDataModel
import com.jmsoft.utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Constants.Companion.category
import com.jmsoft.basic.UtilityTools.Constants.Companion.collection
import com.jmsoft.basic.UtilityTools.Constants.Companion.metalType
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.basic.validation.ResultReturn
import com.jmsoft.basic.validation.Validation
import com.jmsoft.basic.validation.ValidationModel
import com.jmsoft.databinding.DialogAddMetalTypeBinding
import com.jmsoft.databinding.DialogOpenSettingBinding
import com.jmsoft.databinding.FragmentMetalTypeBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.CategoryListAdapter
import com.jmsoft.main.adapter.CollectionListAdapter
import com.jmsoft.main.adapter.MetalTypeListAdapter
import com.jmsoft.main.`interface`.EditInventoryCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MetalTypeFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentMetalTypeBinding

    private var metalTypeDataList = ArrayList<MetalTypeDataModel>()

    private var collectionDataList = ArrayList<CollectionDataModel>()

    private var categoryDataList = ArrayList<CategoryDataModel>()

    private var metalTypeAdapter: MetalTypeListAdapter? = null

    private var collectionAdapter: CollectionListAdapter? = null

    private var categoryAdapter: CategoryListAdapter? = null

    private var fragmentState: String? = null

    // for Opening the Camera Dialog
    private var forCameraSettingDialog = 100

    // for Opening the Gallery Dialog
    private var forGallerySettingDialog = 200

    private lateinit var editProfileDialog: Dialog

    private lateinit var dialogMetalBinding: DialogAddMetalTypeBinding

    private var dialogInventory: Dialog? = null

    private var isCollectionImageSelected = false

    // Gallery Permission Launcher
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
                val imageUri: Uri? = result.data?.data

                dialogMetalBinding.llAddImageSection.visibility = View.GONE
                dialogMetalBinding.mcvCrossBtn.visibility = View.VISIBLE
                isCollectionImageSelected = true
                dialogMetalBinding.ivCollectionImage.setImageURI(imageUri)
                dialogMetalBinding.tvCollectionImageError.visibility = View.GONE

            }
        }

    // Camera Launcher
    private var cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            dialogMetalBinding.llAddImageSection.visibility = View.GONE
            dialogMetalBinding.ivCollectionImage.setImageBitmap(result.data?.extras?.get("data") as Bitmap?)
            dialogMetalBinding.mcvCrossBtn.visibility = View.VISIBLE
            isCollectionImageSelected = true
            dialogMetalBinding.tvCollectionImageError.visibility = View.GONE

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentMetalTypeBinding.inflate(layoutInflater)

        val progressBar = Utils.initProgressDialog(requireActivity())

        //set the Clicks And initialization

        lifecycleScope.launch(Dispatchers.Main) {
            init()
        }

        progressBar.dismiss()

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
    private fun setMetalTypeRecyclerView() {

        metalTypeDataList = Utils.getAllMetalType()

        if (metalTypeDataList.isNotEmpty()) {

            binding.mcvMetalTypeList?.visibility = View.VISIBLE
            binding.llEmptyInventory?.visibility = View.GONE

            metalTypeAdapter = MetalTypeListAdapter(
                requireActivity(),
                metalTypeDataList,
                binding,
                object : EditInventoryCallback {

                    override fun editInventory(inventoryUUID: String, position: Int) {

                        showInventoryDialog(inventoryUUID, position)
                    }
                })
            binding.rvMetalType?.layoutManager =
                GridLayoutManager(requireActivity(), 2) // Span Count is set to 3
            binding.rvMetalType?.adapter = metalTypeAdapter

        } else {

            binding.mcvMetalTypeList?.visibility = View.GONE
            binding.llEmptyInventory?.visibility = View.VISIBLE
            binding.tvEmptyMsg?.text = getString(R.string.metal_type_is_empty)

        }
    }

    // Set Collection Recycler View
    private fun setCollectionRecyclerView() {

        collectionDataList = Utils.getAllCollection()

        if (collectionDataList.isNotEmpty()) {

            binding.mcvMetalTypeList?.visibility = View.VISIBLE
            binding.llEmptyInventory?.visibility = View.GONE

            collectionAdapter = CollectionListAdapter(
                requireActivity(),
                collectionDataList,
                binding,
                object : EditInventoryCallback {

                    override fun editInventory(inventoryUUID: String, position: Int) {

                        showInventoryDialog(inventoryUUID, position)

                    }
                })

            binding.rvMetalType?.layoutManager =
                GridLayoutManager(requireActivity(), 2) // Span Count is set to 3
            binding.rvMetalType?.adapter = collectionAdapter

        } else {

            binding.mcvMetalTypeList?.visibility = View.GONE
            binding.llEmptyInventory?.visibility = View.VISIBLE
            binding.tvEmptyMsg?.text = getString(R.string.collection_is_empty)

        }
    }



    // Update metal type
    private fun updateMetalType(metalTypeUUID: String, position: Int) {

        val metalTypeDataModel = MetalTypeDataModel()
        metalTypeDataModel.metalTypeUUID = metalTypeUUID
        metalTypeDataModel.metalTypeName = Utils.capitalizeData(dialogMetalBinding.etMetalType.text.toString())

        val isCollectionExistAccept = Utils.isMetalTypeExistAccept(metalTypeDataModel)

        if (isCollectionExistAccept == true) {

            Utils.showError(
                requireActivity(), dialogMetalBinding.tvMetalTypeError,
                getString(R.string.metal_type_already_exist)
            )


        } else {

            Utils.updateMetalType(
                metalTypeUUID,
                Utils.capitalizeData(dialogMetalBinding.etMetalType.text.toString())
            )
            metalTypeDataList[position].metalTypeName =
                Utils.capitalizeData(dialogMetalBinding.etMetalType.text.toString())
            metalTypeAdapter?.notifyItemChanged(position)

            dialogInventory?.dismiss()
        }
    }

    // Update category
    private fun updateCategory(categoryUUID: String, position: Int) {

        val categoryDataModel = CategoryDataModel()
        categoryDataModel.categoryUUID = categoryUUID
        categoryDataModel.categoryName =
            Utils.capitalizeData(dialogMetalBinding.etMetalType.text.toString())

        val isCategoryExistAccept = Utils.isCategoryExistAccept(categoryDataModel)

        if (isCategoryExistAccept == true) {

            Utils.showError(
                requireActivity(), dialogMetalBinding.tvMetalTypeError,
                getString(R.string.category_already_exist)
            )
        }
        else {

            Utils.updateCategory(categoryDataModel)

            categoryDataList[position].categoryName =
                Utils.capitalizeData(dialogMetalBinding.etMetalType.text.toString())
            categoryAdapter?.notifyItemChanged(position)

            Utils.T(requireActivity(), requireActivity().getString(R.string.updated_successfully))

            dialogInventory?.dismiss()
        }
    }

    // Update collection
    private fun updateCollection(collectionUUID: String, position: Int) {

        val collectionDataModel = CollectionDataModel()
        collectionDataModel.collectionUUID = collectionUUID
        collectionDataModel.collectionName =
            Utils.capitalizeData(dialogMetalBinding.etMetalType.text.toString())

        val imageUri = Utils.getPictureUri(requireActivity(),dialogMetalBinding.ivCollectionImage.drawable.toBitmap())

        collectionDataModel.collectionImageUri = imageUri

//        val progressBar = Utils.initProgressDialog(requireActivity())

        val isCollectionExitAccept = Utils.isCollectionExistAccept(collectionDataModel)

        if (isCollectionExitAccept == true ) {

            Utils.showError(
                requireActivity(), dialogMetalBinding.tvMetalTypeError,
                getString(R.string.collection_already_exist)
            )

            GetProgressBar.getInstance(requireContext())?.dismiss()

        }
        else {

            collectionDataList[position].collectionImageUri?.let {
                Utils.deleteImageFromInternalStorage(requireActivity(),
                    it ) }

            Utils.updateCollection(collectionDataModel)

            collectionDataList[position].collectionName =
                Utils.capitalizeData(dialogMetalBinding.etMetalType.text.toString())

            collectionDataList[position].collectionImageUri = imageUri

            collectionAdapter?.notifyItemChanged(position)

            Utils.T(requireActivity(), requireActivity().getString(R.string.updated_successfully))

            dialogInventory?.dismiss()

            GetProgressBar.getInstance(requireContext())?.dismiss()

        }
//        progressBar.dismiss()

    }

    // add metal type
    private fun addMetalType() {

        val isMetalTypeExist =
            Utils.isMetalTypeExist(Utils.capitalizeData(dialogMetalBinding.etMetalType.text.toString()))

        if (isMetalTypeExist == true) {

            Utils.showError(
                requireActivity(), dialogMetalBinding.tvMetalTypeError,
                getString(R.string.metal_type_already_exist)
            )

        } else {

            val metalTypeDataModel = MetalTypeDataModel()
            metalTypeDataModel.metalTypeUUID = Utils.generateUUId()
            metalTypeDataModel.metalTypeName =
                Utils.capitalizeData(dialogMetalBinding.etMetalType.text.toString())

            Utils.addMetalTypeInTheMetalTypeTable(metalTypeDataModel)

            Utils.T(
                requireActivity(),
                requireActivity().getString(R.string.added_successfully)
            )

            dialogInventory?.dismiss()

            setMetalTypeRecyclerView()
        }
    }

    // add category
    private fun addCategory() {

        val isCategoryExist =
            Utils.isCategoryExist(Utils.capitalizeData(dialogMetalBinding.etMetalType.text.toString()))

        if (isCategoryExist == true) {

            Utils.showError(
                requireActivity(), dialogMetalBinding.tvMetalTypeError,
                getString(R.string.category_already_exist)
            )
        } else {

            val categoryDataModel = CategoryDataModel()
            categoryDataModel.categoryUUID = Utils.generateUUId()
            categoryDataModel.categoryName =
                Utils.capitalizeData(dialogMetalBinding.etMetalType.text.toString())

            Utils.addCategory(categoryDataModel)

            Utils.T(
                requireActivity(),
                requireActivity().getString(R.string.added_successfully)
            )

            dialogInventory?.dismiss()

            setCategoryRecyclerView()
        }
    }

    // Set state of the inventory dialog
    private fun setInventoryDialogState(inventoryUUID: String?, position: Int?) {

        when (fragmentState) {
            metalType -> {

                if (position != null && inventoryUUID != null) {

                    dialogMetalBinding.tvTitle.text = getString(R.string.edit_metal_type)
                    dialogMetalBinding.etMetalType.setText(metalTypeDataList[position].metalTypeName)

                } else {

                    dialogMetalBinding.tvTitle.text =
                        requireActivity().getString(R.string.add_metal_type)

                }

                dialogMetalBinding.tvName.text = requireActivity().getString(R.string.metal_type)
                dialogMetalBinding.etMetalType.hint =
                    requireActivity().getString(R.string.enter_metal_type)

            }
            collection -> {

                if (position != null && inventoryUUID != null) {

                    dialogMetalBinding.tvTitle.text = getString(R.string.update_collection)
                    dialogMetalBinding.etMetalType.setText(collectionDataList[position].collectionName)

                    dialogMetalBinding.llAddImageSection.visibility = View.GONE
                    dialogMetalBinding.mcvCrossBtn.visibility = View.VISIBLE
                    isCollectionImageSelected = true

                    dialogMetalBinding.ivCollectionImage.setImageBitmap(collectionDataList[position].collectionImageUri?.let {
                        Utils.getImageFromInternalStorage(
                            requireActivity(),
                            it
                        )
                    })

                } else {

                    dialogMetalBinding.tvTitle.text =
                        requireActivity().getString(R.string.add_collection)
                }

                dialogMetalBinding.tvName.text = requireActivity().getString(R.string.collection)
                dialogMetalBinding.etMetalType.hint = getString(R.string.enter_collection_name)
                dialogMetalBinding.llAddImage.visibility = View.VISIBLE

                dialogMetalBinding.llAddImageSection.setOnClickListener {
                    showImageSelectionDialog()
                }

                dialogMetalBinding.mcvCrossBtn.setOnClickListener {

                    dialogMetalBinding.ivCollectionImage.setImageDrawable(null)
                    dialogMetalBinding.llAddImageSection.visibility = View.VISIBLE
                    dialogMetalBinding.mcvCrossBtn.visibility = View.GONE
                    isCollectionImageSelected = false

                }
            }
            category -> {

                if (position != null && inventoryUUID != null) {

                    dialogMetalBinding.tvTitle.text = getString(R.string.edit_category)
                    dialogMetalBinding.etMetalType.setText(categoryDataList[position].categoryName)

                } else {
                    dialogMetalBinding.tvTitle.text = requireActivity().getString(R.string.add_category)
                }

                dialogMetalBinding.tvName.text = requireActivity().getString(R.string.category)
                dialogMetalBinding.etMetalType.hint =
                    requireActivity().getString(R.string.enter_category_name)

            }
        }

        Utils.setFocusChangeLis(requireActivity(),dialogMetalBinding.etMetalType, dialogMetalBinding.mcvMetalType)
        Utils.addTextChangedListener(
            dialogMetalBinding.etMetalType,
            dialogMetalBinding.tvMetalTypeError
        )

    }

    // add collection
    @SuppressLint("NotifyDataSetChanged")
    private fun addCollection() {

        val isCollectionExist =
            Utils.isCollectionExist(Utils.capitalizeData(dialogMetalBinding.etMetalType.text.toString()))

        if (isCollectionExist == true) {

            Utils.showError(
                requireActivity(), dialogMetalBinding.tvMetalTypeError,
                getString(R.string.collection_already_exist)
            )

            GetProgressBar.getInstance(requireContext())?.dismiss()


        } else {

            val collectionDataModel = CollectionDataModel()
            collectionDataModel.collectionUUID = Utils.generateUUId()
            collectionDataModel.collectionName =
                Utils.capitalizeData(dialogMetalBinding.etMetalType.text.toString())
            collectionDataModel.collectionImageUri = Utils.getPictureUri(
                requireActivity(),
                dialogMetalBinding.ivCollectionImage.drawable.toBitmap()
            )

            Utils.addCollection(collectionDataModel)

            Utils.T(
                requireActivity(),
                requireActivity().getString(R.string.added_successfully)
            )

            setCollectionRecyclerView()

            dialogInventory?.dismiss()
//            GetProgressBar.getInstance(requireContext())?.dismiss()


        }
    }

    // Add Add and Update inventory Dialog
    @SuppressLint("NotifyDataSetChanged", "DefaultLocale")
    private fun showInventoryDialog(inventoryUUID: String?, position: Int?) {

        dialogInventory = Dialog(requireActivity())

        dialogMetalBinding = DialogAddMetalTypeBinding.inflate(LayoutInflater.from(context))

        dialogInventory?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogInventory?.setCanceledOnTouchOutside(true)
        dialogInventory?.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val parent = dialogMetalBinding.root.parent as? ViewGroup
        parent?.removeView(dialogMetalBinding.root)

        dialogInventory?.setContentView(dialogMetalBinding.root)

        setInventoryDialogState(inventoryUUID, position)

        dialogMetalBinding.mcvSave.setOnClickListener {

            val errorValidationModels: MutableList<ValidationModel> = ArrayList()

            if (fragmentState == collection) {

                errorValidationModels.add(
                    ValidationModel(
                        Validation.Type.ImageSelect,
                        isCollectionImageSelected,
                        dialogMetalBinding.tvCollectionImageError
                    )
                )
            }

            errorValidationModels.add(
                ValidationModel(
                    Validation.Type.Empty,
                    dialogMetalBinding.etMetalType,
                    dialogMetalBinding.tvMetalTypeError
                )
            )

            val validation: Validation? = Validation.instance
            val resultReturn: ResultReturn? =
                validation?.CheckValidation(requireActivity(), errorValidationModels)
            if (resultReturn?.aBoolean == true) {

                if (fragmentState == metalType) {

                    if (position != null && inventoryUUID != null) {

                        updateMetalType(inventoryUUID, position)

//                        dialogInventory?.dismiss()

                    } else {

                        addMetalType()
                    }

                } else if (fragmentState == collection) {

                    GetProgressBar.getInstance(requireContext())?.show()

                    if (position != null && inventoryUUID != null) {

                        updateCollection(inventoryUUID, position)

//                        dialogInventory?.dismiss()


                    } else {

                        addCollection()

                    }

                } else if (fragmentState == category) {

                    if (position != null && inventoryUUID != null) {

                        updateCategory(inventoryUUID, position)

//                        dialogInventory?.dismiss()

                    } else {

                        addCategory()
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

        dialogMetalBinding.mcvCancel.setOnClickListener {
            dialogInventory?.dismiss()
        }

        dialogInventory?.setCancelable(true)
        dialogInventory?.show()

    }

    // Checks state of the fragment
    private fun checkState() {

        // getting the state
        fragmentState = arguments?.getString(Constants.state)

        when (fragmentState) {
            metalType -> {

                binding.tvTitle?.text = requireActivity().getString(R.string.metal_type)
                binding.tvButtonName?.text = requireActivity().getString(R.string.add_metal_type)

                // Set Metal Type Recycler View
                setMetalTypeRecyclerView()
            }
            collection -> {

                binding.tvTitle?.text = requireActivity().getString(R.string.collection)
                binding.tvButtonName?.text = requireActivity().getString(R.string.add_collection)

                // Set Collection Recycler View
                setCollectionRecyclerView()

            }
            category -> {

                binding.tvTitle?.text = requireActivity().getString(R.string.category)
                binding.tvButtonName?.text = getString(R.string.add_category)

                // Set Collection Recycler View
                setCategoryRecyclerView()
            }
        }

    }

    // Set category recycler view
    private fun setCategoryRecyclerView() {

        categoryDataList = Utils.getAllCategory()

        if (categoryDataList.isNotEmpty()) {

            binding.mcvMetalTypeList?.visibility = View.VISIBLE
            binding.llEmptyInventory?.visibility = View.GONE

            categoryAdapter =
                CategoryListAdapter(
                    requireActivity(),
                    categoryDataList,
                    binding,
                    object : EditInventoryCallback {

                        override fun editInventory(inventoryUUID: String, position: Int) {

                            showInventoryDialog(inventoryUUID, position)

                        }

                    })
            binding.rvMetalType?.layoutManager =
                GridLayoutManager(requireActivity(), 2) // Span Count is set to 3
            binding.rvMetalType?.adapter = categoryAdapter


        } else {

            binding.mcvMetalTypeList?.visibility = View.GONE
            binding.llEmptyInventory?.visibility = View.VISIBLE
            binding.tvEmptyMsg?.text = getString(R.string.category_is_empty)

        }
    }

    //set the Clicks And initialization
    private suspend fun init() {

        // Checks state of the fragment
        val job = lifecycleScope.launch(Dispatchers.Main) {
            checkState()
        }

        // Set Click on Back Button
        binding.mcvBackBtn?.setOnClickListener(this)

        // Set Click on Add Metal Type Button
        binding.mcvAddMetalType?.setOnClickListener(this)

        job.join()

        GetProgressBar.getInstance(requireActivity())?.dismiss()

    }

    // Handle all the clicks
    override fun onClick(v: View?) {

        // When Back button clicked
        if (v == binding.mcvBackBtn) {
            (requireActivity() as DashboardActivity).navController?.popBackStack()
        }

        // When Add Metal type Button click
        else if (v == binding.mcvAddMetalType) {

            // Add Add and Update inventory Dialog
            showInventoryDialog(null, null)

        }
    }
}