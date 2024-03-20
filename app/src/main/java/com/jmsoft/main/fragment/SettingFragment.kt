package com.jmsoft.main.fragment

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.basic.Database.UserDataHelper
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.basic.UtilityTools.Utils.deviceName
import com.jmsoft.databinding.FragmentSettingBinding
import com.jmsoft.main.activity.LoginActivity
import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import com.jmsoft.Utility.UtilityTools.BluetoothUtils
import java.io.ByteArrayOutputStream

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
    private val permissionsRequestCode = 200 // You can use any value for the request code

    private var permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

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

    @SuppressLint("SetTextI18n")
    private fun setFullName(){
        val instance = UserDataHelper.instance.list[0]
        binding.tvfullName?.text  = "${instance.firstName}  ${instance.lastName}"
    }

    private fun updateProfile(profile:Bitmap?){

        if (profile != null){

            val instance = UserDataHelper.instance

            //get the Random Image File Name

            val imageFileName = Utils.getImageFileName()

            //Save it to internal Storage
            Utils.saveToInternalStorage(requireActivity(),profile,imageFileName)

            //Update the profile
            instance.updateProfile(imageFileName, instance.list[0].email!!)

            val userDataModel = instance.getUserDetail(instance.list[0].email!!)
            //Update the Session Data
            instance.insertDataInSessionTable(userDataModel)

        }
    }

    private var galleryActivityResultLauncher: ActivityResultLauncher<Intent?>? = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val image_uri: Uri? = result.data?.data
            binding.ivProfile?.setImageURI(image_uri)

            updateProfile(binding.ivProfile?.drawable?.toBitmap())

        }
    }

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

    // Setting the profile picture
    private fun setProfilePicture(){

        val profileName = UserDataHelper.instance.list[0].profileName.toString()

        if (profileName != ""){
            val profile = Utils.getImageFromInternalStorage(requireActivity(),profileName)
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

    //Checks Permission for Camera And Gallery
    private fun hasPermissions():Boolean{

        for (permission in permissions){

            if (ActivityCompat.checkSelfPermission(requireActivity(),permission) == PackageManager.PERMISSION_DENIED){
                return false
            }
        }
        return true
    }

    private fun showEditProfileDialog(){

        val dialog = Dialog(requireActivity())
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.item_dialog_profile)
        dialog.findViewById<MaterialCardView>(R.id.mcvCamera).setOnClickListener {

            //Camera Launcher
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraActivityResultLauncher.launch(cameraIntent)
            dialog.dismiss()

        }
        dialog.findViewById<MaterialCardView>(R.id.mcvGallery).setOnClickListener {

            //Gallery Launcher
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryActivityResultLauncher?.launch(galleryIntent)
            dialog.dismiss()

        }
        dialog.setCancelable(true)
        dialog.show()
    }

    //Request for the camera and gallery permission
    private fun requestPermission(){
        ActivityCompat.requestPermissions(
            context as Activity,
            permissions,
            permissionsRequestCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == permissionsRequestCode) {
            // Check if all permissions are granted
            val allPermissionsGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            if (allPermissionsGranted) {
                // All permissions are granted, show the dialog
                showEditProfileDialog()
                Utils.T(activity, "Permission granted")

            } else {
                Utils.T(activity, "Please allow the Permission For Edit the Profile")
                // Permissions are not granted, handle the scenario
                // For example, show a message to the user or disable Bluetooth functionality
            }
        }
    }

    private fun showLogOutDialog(){

        val dialog = Dialog(requireActivity())
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.item_dialog_logout)
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
        else if(v == binding.mcvEditProfile){

            //Checks for the permission of camera and gallery
            if (hasPermissions()){
                //Show Dialog Box
                showEditProfileDialog()
            }
            else {
                requestPermission()
            }
        }

    }
}