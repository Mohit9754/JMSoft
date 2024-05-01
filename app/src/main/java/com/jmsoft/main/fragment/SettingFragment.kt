package com.jmsoft.main.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.Dialog
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.basic.Database.DatabaseHelper
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Constants.Companion.admin
import com.jmsoft.basic.UtilityTools.Constants.Companion.updateInSession
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.DialogOpenSettingBinding
import com.jmsoft.databinding.FragmentSettingBinding
import com.jmsoft.main.activity.DashboardActivity
import com.jmsoft.main.activity.LoginActivity

class SettingFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentSettingBinding
    private lateinit var editProfileDialog: Dialog

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
            if (result.resultCode == RESULT_OK) {
                val image_uri: Uri? = result.data?.data
                binding.ivProfile?.setImageURI(image_uri)

                updateProfile(binding.ivProfile?.drawable?.toBitmap())

            }
        }

    //Camera result Launcher
    private var cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result != null) {
            if (result.resultCode == RESULT_OK) {
                binding.ivProfile?.setImageBitmap(result.data?.extras?.get("data") as Bitmap?)
                updateProfile(binding.ivProfile?.drawable?.toBitmap())

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment

        binding = FragmentSettingBinding.inflate(layoutInflater)

        // Hide the Search option
        (requireActivity() as DashboardActivity).binding?.mcvSearch?.visibility = View.GONE

        // make current state to verification
        (requireActivity() as DashboardActivity).currentState = Constants.verification

        //set the Clicks And initialization
        init()

        return binding.root
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

    // Setting the full Name
    @SuppressLint("SetTextI18n")
    private fun setFullName() {
        val instance = DatabaseHelper.instance.list[0]
        binding.tvfullName?.text = "${instance.firstName}  ${instance.lastName}"
    }

    // Update the Profile
    private fun updateProfile(profile: Bitmap?) {

        if (profile != null) {

//            val instance = UserDataHelper.instance

            //get the Random Image File Name

            val imageFileName = Utils.getImageFileName()

            //Save it to internal Storage
            Utils.saveToInternalStorage(requireActivity(), profile, imageFileName)

            //Update the profile
            Utils.updateProfileInUserTable(imageFileName, Utils.GetSession().userUUID!!)

            val userDataModel = Utils.getUserDetailsThroughUserUUID(Utils.GetSession().userUUID!!)
            //Update the Session Data
            Utils.insertDataInSessionTable(userDataModel)

        }
    }


    // Setting the profile picture
    private fun setProfilePicture() {

        val profileName = DatabaseHelper.instance.list[0].profileUri.toString()

        if (profileName != "") {
            val profile = Utils.getImageFromInternalStorage(requireActivity(), profileName)
            binding.ivProfile?.setImageBitmap(profile)
        }
    }

    //Check if the User is admin or not. if not then hide the user Management option
    private fun checkUserTypeAndHideUserManagement() {

        if (Utils.GetSession().userType != admin) {
            binding.mcvUserManagement?.visibility = View.GONE
        }
    }

    // set the Clicks , initialization And Setup
    private fun init() {

        //Check if the User is admin or not. if not then hide the user Management option
        checkUserTypeAndHideUserManagement()

        //setFullName from the local Database
        setFullName()

        setProfilePicture()

        //Set Click on Device Management Option
        binding.mcvDeviceManagement?.setOnClickListener(this)

        //Set Click on LogOut Option
        binding.mcvLogOut?.setOnClickListener(this)

        //Set Click on Edit Profile Button
        binding.mcvEditProfile?.setOnClickListener(this)

        //Set Click on User Management
        binding.mcvUserManagement?.setOnClickListener(this)

        //Set Click on Back Button
        binding.mcvBackBtn?.setOnClickListener(this)

        //Set Click on Back Button
        binding.mcvUserName?.setOnClickListener(this)

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

    // Logout Dialog
    private fun showLogOutDialog() {

        val dialog = Dialog(requireActivity())
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_logout)
        dialog.findViewById<MaterialCardView>(R.id.mcvYes).setOnClickListener {

            dialog.dismiss()
            // Remove the session
            Utils.LOGOUT()
            //intent to login activity with Clear back stack
            Utils.I_clear(requireActivity(), LoginActivity::class.java, null)

        }
        dialog.findViewById<MaterialCardView>(R.id.mcvNo).setOnClickListener {

            dialog.dismiss()
        }
        dialog.setCancelable(true)
        dialog.show()
    }

    // Handles All the Clicks
    override fun onClick(v: View?) {

        // Click on Device Management
        if (v == binding.mcvDeviceManagement) {

            (requireActivity() as DashboardActivity).navController?.navigate(R.id.deviceManagement)

        }

        // Click on User Management
        else if (v == binding.mcvUserManagement) {

            (requireActivity() as DashboardActivity).navController?.navigate(R.id.userManagement)
        }

        // Click on Back Button
        else if (v == binding.mcvBackBtn) {

            (requireActivity() as DashboardActivity).navController?.popBackStack(R.id.home, false)
        }
        // Click on Username
        else if (v == binding.mcvUserName) {

            //Navigate to Edit Profile
            val bundle = Bundle()
            Utils.GetSession().userUUID?.let { bundle.putString(Constants.userUUID, it) }
            bundle.putBoolean(updateInSession, true)

            (context as DashboardActivity).navController?.navigate(R.id.editProfile, bundle)
        }

        // When LogOut button Clicked
        else if (v == binding.mcvLogOut) {

            //Showing the LogOut Dialog
            showLogOutDialog()
        }

        //When Edit Profile Clicked
        else if (v == binding.mcvEditProfile) {
            showEditProfileDialog()
        }

    }
}