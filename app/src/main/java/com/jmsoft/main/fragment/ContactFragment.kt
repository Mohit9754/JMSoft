@file:Suppress("DEPRECATION")

package com.jmsoft.main.fragment

import android.Manifest
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.Utility.database.ContactDataModel
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.basic.validation.ResultReturn
import com.jmsoft.basic.validation.Validation
import com.jmsoft.basic.validation.ValidationModel
import com.jmsoft.databinding.DialogOpenSettingBinding
import com.jmsoft.databinding.FragmentContactBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.adapter.ContactAdapter
import com.jmsoft.main.`interface`.ContactSelectionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ContactFragment : Fragment(), View.OnClickListener {

    private val binding by lazy { FragmentContactBinding.inflate(layoutInflater) }

    private var errorValidationModels: MutableList<ValidationModel> = ArrayList()

    private lateinit var editProfileDialog: Dialog

    private var isProfileSelected = false

    private var contactAdapter: ContactAdapter? = null

    private var selectedContactData: ContactDataModel? = null

    // for Opening the Camera Dialog
    private var forCameraSettingDialog = 100

    // for Opening the Gallery Dialog
    private var forGallerySettingDialog = 200

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

    // Camera Permission Launcher
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

    // Gallery result  Launcher
    private var galleryActivityResultLauncher: ActivityResultLauncher<Intent?>? =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri: Uri? = result.data?.data
                binding.ivProfile?.setImageURI(imageUri)
                isProfileSelected = true

//                updateProfile(binding.ivProfile?.drawable?.toBitmap())

            }
        }

    //Camera result Launcher
    private var cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            binding.ivProfile?.setImageBitmap(result.data?.extras?.get("data") as Bitmap?)
            isProfileSelected = true
//                updateProfile(binding.ivProfile?.drawable?.toBitmap())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

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

    // Set FocusChange Listener
    private fun setFocusChangeListener() {

        binding.etFirstName?.let {
            binding.mcvFirstName?.let { it1 ->
                Utils.setFocusChangeListener(
                    requireActivity(),
                    it, it1
                )
            }
        }

        binding.etLastName?.let {
            binding.mcvLastName?.let { it1 ->
                Utils.setFocusChangeListener(
                    requireActivity(),
                    it, it1
                )
            }
        }

        binding.etPhoneNumber?.let {
            binding.mcvPhoneNumber?.let { it1 ->
                Utils.setFocusChangeListener(
                    requireActivity(),
                    it, it1
                )
            }
        }

        binding.etEmail?.let {
            binding.mcvEmail?.let { it1 ->
                Utils.setFocusChangeListener(
                    requireActivity(),
                    it, it1
                )
            }
        }

    }

    // set TextChange Listener
    private fun setTextChangeListener() {

        binding.etFirstName?.let {
            binding.tvFirstNameError?.let { it1 ->
                Utils.setTextChangeListener(
                    it,
                    it1
                )
            }
        }

        binding.etLastName?.let {
            binding.tvLastNameError?.let { it1 ->
                Utils.setTextChangeListener(
                    it,
                    it1
                )
            }
        }
        binding.etPhoneNumber?.let {
            binding.tvPhoneNumberError?.let { it1 ->
                Utils.setTextChangeListener(
                    it,
                    it1
                )
            }
        }
        binding.etEmail?.let {
            binding.tvEmailError?.let { it1 ->
                Utils.setTextChangeListener(
                    it,
                    it1
                )
            }
        }
    }

    // Set Contact RecyclerView
    private fun setContactRecyclerView() {

        val contactList =
            Utils.GetSession().userUUID?.let { Utils.getAllContactThroughUserUUID(it) }

        // Contact list is empty
        if (contactList?.isEmpty() == true || contactList == null) {

            GetProgressBar.getInstance(requireActivity())?.dismiss()
            binding.ivNoContact?.visibility = View.VISIBLE

        } else {

            binding.ivNoContact?.visibility = View.GONE

            contactAdapter =

                ContactAdapter(
                    requireActivity(),
                    contactList,
                    binding,
                    selectedContactData,
                    object : ContactSelectionStatus {

                        override fun contactSelected(contactDataModel: ContactDataModel) {

                            binding.etFirstName?.setText(contactDataModel.firstName)
                            binding.etLastName?.setText(contactDataModel.lastName)
                            binding.etPhoneNumber?.setText(contactDataModel.phoneNumber)
                            binding.etEmail?.setText(contactDataModel.emailAddress)
                            binding.tvType?.text = contactDataModel.type


                            selectedContactData = contactDataModel

                            if (contactDataModel.type == requireActivity().getString(R.string.legal_entity)) {

                                binding.llLegalEntity?.setBackgroundColor(
                                    requireActivity().getColor(
                                        R.color.selected_drop_down_color
                                    )
                                )
                            } else {
                                binding.llPhysicalPerson?.setBackgroundColor(
                                    requireActivity().getColor(
                                        R.color.selected_drop_down_color
                                    )
                                )

                            }
                        }

                        override fun contactUnselected() {

                            selectedContactData = null

                            isProfileSelected = false

                            // Make empty all the edittext
                            removeData()

                        }
                    }
                )

            binding.rvContactList?.layoutManager =
                LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            binding.rvContactList?.adapter = contactAdapter
        }

    }

    private fun init() {

        setFocusChangeListener()

        setTextChangeListener()

        setContactRecyclerView()

        binding.mcvSave?.setOnClickListener(this)

        binding.mcvEditProfile?.setOnClickListener(this)

        binding.mcvBackBtn?.setOnClickListener(this)

        binding.llType?.setOnClickListener(this)

        binding.llLegalEntity?.setOnClickListener(this)

        binding.llPhysicalPerson?.setOnClickListener(this)

    }

    // Add Contact
    private fun addContact() {

        val contactDataModel = ContactDataModel()

        contactDataModel.contactUUID = Utils.generateUUId()

        contactDataModel.firstName = binding.etFirstName?.text.toString().trim()
        contactDataModel.lastName = binding.etLastName?.text.toString().trim()
        contactDataModel.phoneNumber = binding.etPhoneNumber?.text.toString().trim()
        contactDataModel.emailAddress =
            binding.etEmail?.text.toString().trim().toLowerCase(Locale.ROOT)
        contactDataModel.type = binding.tvType?.text.toString().trim()
        contactDataModel.userUUID = Utils.GetSession().userUUID

        lifecycleScope.launch (Dispatchers.IO){
            Utils.insertContact(contactDataModel)
        }

//        Utils.T(requireActivity(),"")

        removeData()

        selectedContactData = contactDataModel

        // Setting Up Contact List Recycler View
        setContactRecyclerView()

        GetProgressBar.getInstance(requireActivity())?.dismiss()

    }

    // Remove Data
    private fun removeData() {

        binding.etFirstName?.setText("")
        binding.etLastName?.setText("")
        binding.etPhoneNumber?.setText("")
        binding.etEmail?.setText("")
        binding.tvType?.text = ""

        binding.ivProfile?.setImageResource(R.drawable.img_default_profile)

        binding.llPhysicalPerson?.setBackgroundColor(requireActivity().getColor(R.color.white))
        binding.llLegalEntity?.setBackgroundColor(requireActivity().getColor(R.color.white))

    }

    // Update Contact
    private suspend fun updateContact() {

        val contactDataModel = ContactDataModel()


        withContext(Dispatchers.Main) {

            contactDataModel.firstName = binding.etFirstName?.text.toString().trim()
            contactDataModel.lastName = binding.etLastName?.text.toString().trim()
            contactDataModel.phoneNumber = binding.etPhoneNumber?.text.toString().trim()
            contactDataModel.emailAddress =
                binding.etEmail?.text.toString().trim().toLowerCase(Locale.ROOT)
            contactDataModel.type = binding.tvType?.text.toString().trim()

        }

        contactDataModel.contactUUID = selectedContactData?.contactUUID

        Utils.updateContactInTheContactTable(contactDataModel)

        selectedContactData = contactDataModel


        withContext(Dispatchers.Main) {

            // Setting Up Contact List Recycler View
            setContactRecyclerView()

            Utils.T(requireActivity(), getString(R.string.contact_updated_successfully))

        }

    }

    // Check whether we have to add new contact or update the contact , and act accordingly
    private  fun checkIfAddNewContactOrUpdateContact() {

        val phoneNumber = binding.etPhoneNumber?.text.toString().trim()
        val emailAddress = binding.etEmail?.text.toString().trim().toLowerCase(Locale.ROOT)

        // Update contact
        if (selectedContactData != null) {

            val isPhoneNumberExist = selectedContactData?.contactUUID?.let {
                Utils.isPhoneNumberExistInContactTableAcceptMine(
                    phoneNumber,
                    it
                )
            }

            if (isPhoneNumberExist == true) {
                showPhoneNumberAlreadyExistError()

            } else {

                val isEmailExist = selectedContactData?.contactUUID?.let {
                    Utils.isEmailExistInContactTableAcceptMine(
                        emailAddress,
                        it
                    )
                }

                if (isEmailExist == true) {

                    GetProgressBar.getInstance(requireActivity())?.dismiss()

                    binding.tvEmailError?.let {
                        Utils.showError(
                            requireActivity(),
                            it, requireActivity().getString(R.string.email_already_exist)
                        )
                    }

                } else {

                    // Update Contact
                    lifecycleScope.launch(Dispatchers.IO) {
                        updateContact()
                    }
                }
            }
        }

        // Add new contact
        else {

            // Check if phone Number exist
            if (!Utils.isPhoneNumberExistInContactTable(phoneNumber)) {

                if (!Utils.isEmailExistInContactTable(emailAddress)) {

                    // Add new Contact
                    lifecycleScope.launch(Dispatchers.Main) {
                        addContact()
                    }

                } else {

                    GetProgressBar.getInstance(requireActivity())?.dismiss()

                    binding.tvEmailError?.let {
                        Utils.showError(
                            requireActivity(),
                            it, requireActivity().getString(R.string.email_already_exist)
                        )
                    }
                }

            } else {

                showPhoneNumberAlreadyExistError()
            }
        }
    }

    // Show phone number already exist error
    private fun showPhoneNumberAlreadyExistError() {

//        binding.progressBar?.visibility  = View.GONE
        GetProgressBar.getInstance(requireActivity())?.dismiss()

        binding.tvPhoneNumberError?.also {

            it.visibility = View.VISIBLE
            it.text = requireActivity().getString(R.string.mobile_number_already_exist)
            it.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.top_to_bottom))
        }
    }

    // Validating Sign Up details
    private suspend fun validate() {

        errorValidationModels.clear()

        errorValidationModels.add(
            ValidationModel(
                Validation.Type.Empty, binding.etFirstName, binding.tvFirstNameError
            )
        )
        errorValidationModels.add(
            ValidationModel(
                Validation.Type.Empty, binding.etLastName, binding.tvLastNameError
            )
        )

        errorValidationModels.add(

            ValidationModel(
                Validation.Type.Phone, binding.etPhoneNumber, binding.tvPhoneNumberError
            )
        )

        errorValidationModels.add(
            ValidationModel(
                Validation.Type.Email, binding.etEmail, binding.tvEmailError
            )
        )

        errorValidationModels.add(
            ValidationModel(
                Validation.Type.EmptyTextView, binding.tvType, binding.tvTypeError
            )
        )

        val validation: Validation? = Validation.instance
        val resultReturn: ResultReturn? =
            validation?.CheckValidation(requireActivity(), errorValidationModels)
        if (resultReturn?.aBoolean == true) {

            val job = lifecycleScope.launch(Dispatchers.Main) {
                GetProgressBar.getInstance(requireActivity())?.show()
            }

            job.join()

            checkIfAddNewContactOrUpdateContact()


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

                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(validation?.EditTextPointer, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    // Edit Profile Dialog
    private fun showEditProfileDialog() {

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

    // Show or hide  type drop down
    private fun showOrHideTypeDropDown() {

        if (binding.mcvTypeList?.visibility == View.VISIBLE) {

            binding.ivType?.let { Utils.rotateView(it, 0f) }

            if (binding.mcvTypeList != null) {
                Utils.collapseView(binding.mcvTypeList!!)
            }

        } else {

            binding.ivType?.let { Utils.rotateView(it, 180f) }
            binding.mcvTypeList?.let { Utils.expandView(it) }
        }
    }

    override fun onClick(v: View?) {

        // When Back button clicked
        when (v) {
            binding.mcvBackBtn -> {

                (requireActivity() as DashboardActivity).navController?.popBackStack()
            }
            binding.mcvSave -> {

                lifecycleScope.launch(Dispatchers.Main) {
                    validate()
                }

            }
            binding.mcvEditProfile -> {

                showEditProfileDialog()
            }
            binding.llType -> {

                showOrHideTypeDropDown()
            }
            binding.llLegalEntity -> {

                binding.llLegalEntity?.setBackgroundColor(requireActivity().getColor(R.color.selected_drop_down_color))
                binding.llPhysicalPerson?.setBackgroundColor(requireActivity().getColor(R.color.white))
                binding.tvType?.text = binding.tvLegalEntity?.text.toString()
                binding.tvTypeError?.visibility = View.GONE

                showOrHideTypeDropDown()

            }
            binding.llPhysicalPerson -> {

                binding.llLegalEntity?.setBackgroundColor(requireActivity().getColor(R.color.white))
                binding.llPhysicalPerson?.setBackgroundColor(requireActivity().getColor(R.color.selected_drop_down_color))
                binding.tvType?.text = binding.tvPhysicalPerson?.text.toString()
                binding.tvTypeError?.visibility = View.GONE

                showOrHideTypeDropDown()

            }
        }

    }

}