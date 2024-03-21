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
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.basic.Database.UserDataHelper
import com.jmsoft.basic.UtilityTools.Constants.Companion.cameraMessage
import com.jmsoft.basic.UtilityTools.Constants.Companion.camera_Permission_Denied
import com.jmsoft.basic.UtilityTools.Constants.Companion.galleryMessage
import com.jmsoft.basic.UtilityTools.Constants.Companion.photo_Library_Permission_Denied
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.FragmentSettingBinding
import com.jmsoft.main.activity.LoginActivity

/**
 * Setting Fragment
 *
 * Showing the Vendors and Customers option
 * Showing the Device Management option
 * Shows profile picture ,name And also edit it
 * Removing the Session when logOut button clicked
 *
 */
class SettingFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentSettingBinding
    private lateinit var editProfileDialog: Dialog
    private var forCameraSettingDialog = 100
    private var forGallerySettingDialog = 200

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
            if (result.resultCode == RESULT_OK) {
                val image_uri: Uri? = result.data?.data
                binding.ivProfile?.setImageURI(image_uri)

                updateProfile(binding.ivProfile?.drawable?.toBitmap())

            }
        }

    //Camera Launcer
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

        //set the Clicks And initalize
        init()

        return binding.root
    }

    private fun showOpenSettingDialog(dialogCode: Int) {

        val dialog = Dialog(requireActivity())
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_open_setting)

        dialog.findViewById<TextView>(R.id.tvTitle).text =
            if (dialogCode == forCameraSettingDialog) {
                camera_Permission_Denied
            } else {
                photo_Library_Permission_Denied
            }
        dialog.findViewById<TextView>(R.id.tvMessage).text =
            if (dialogCode == forCameraSettingDialog) {
                cameraMessage
            } else {
                galleryMessage
            }

        dialog.findViewById<MaterialCardView>(R.id.mcvCancel).setOnClickListener {

            dialog.dismiss()
        }
        dialog.findViewById<MaterialCardView>(R.id.mcvOpenSetting).setOnClickListener {

            dialog.dismiss()
            Utils.openAppSettings(requireActivity())
        }

        dialog.setCancelable(true)
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun setFullName() {
        val instance = UserDataHelper.instance.list[0]
        binding.tvfullName?.text = "${instance.firstName}  ${instance.lastName}"
    }

    private fun updateProfile(profile: Bitmap?) {

        if (profile != null) {

            val instance = UserDataHelper.instance

            //get the Random Image File Name

            val imageFileName = Utils.getImageFileName()

            //Save it to internal Storage
            Utils.saveToInternalStorage(requireActivity(), profile, imageFileName)

            //Update the profile
            instance.updateProfile(imageFileName, instance.list[0].email!!)

            val userDataModel = instance.getUserDetailThroughEmail(instance.list[0].email!!)
            //Update the Session Data
            instance.insertDataInSessionTable(userDataModel)

        }
    }


    // Setting the profile picture
    private fun setProfilePicture() {

        val profileName = UserDataHelper.instance.list[0].profileName.toString()

        if (profileName != "") {
            val profile = Utils.getImageFromInternalStorage(requireActivity(), profileName)
            binding.ivProfile?.setImageBitmap(profile)
        }
    }

    private fun init() {

        //setFullName from the local Database
        setFullName()

        setProfilePicture()

        //Set Click on Device Management Option
        binding.mcvDeviceManagement?.setOnClickListener(this)

        //Set Click on LogOut Option
        binding.mcvLogOut?.setOnClickListener(this)

        //Set Click on Edit Profile Button
        binding.mcvEditProfile?.setOnClickListener(this)

    }

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

    private fun showLogOutDialog() {

        val dialog = Dialog(requireActivity())
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_logout)
        dialog.findViewById<MaterialCardView>(R.id.mcvYes).setOnClickListener {

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

    //Handles All the Clicks
    override fun onClick(v: View?) {

        //Navigate to Device Management Fragment
        if (v == binding.mcvDeviceManagement) {

            val navController = findNavController()
            navController.navigate(R.id.deviceManagement)
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