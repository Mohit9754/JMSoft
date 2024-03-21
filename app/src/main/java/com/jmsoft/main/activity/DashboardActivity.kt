package com.jmsoft.main.activity

import android.app.Activity
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.jmsoft.R
import com.jmsoft.basic.UtilityTools.Constants.Companion.arabic
import com.jmsoft.basic.UtilityTools.Constants.Companion.english
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.ActivityDashboardBinding

/**
 * Dashboard Activity
 *
 * All the fragments load here
 * here is toolbar
 *
 */
class DashboardActivity : AppCompatActivity(), View.OnClickListener {

    private val activity: Activity = this@DashboardActivity
    private lateinit var binding: ActivityDashboardBinding
    private val permissionsRequestCodeForCamera = 200 // You can use any value for the request code
    private val permissionsRequestCodeForBluetooth = 100 // You can use any value for the request code

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //set the Clicks And initalize
        init()
    }


    private fun init() {

        // Set Click on language switcher
        binding.ivLanguageSwitcher?.setOnClickListener(this)

        // Set Click on setting icon for navigating to Setting fragment
        binding.ivSetting?.setOnClickListener(this)

        // set Click on Bag Icon for navigating to Home fragment
        binding.ivBag?.setOnClickListener(this)
    }

    //Handles All the Clicks
    override fun onClick(v: View?) {

        // Switching language accourding to current language
        if (v == binding.ivLanguageSwitcher) {

            val lang = Utils.getCurrentLanguage()

            if (lang == english) {
                Utils.setLocale(activity, arabic)
                activity.recreate()
            } else {
                Utils.setLocale(activity, english)
                activity.recreate()
            }

            // navigate to Setting fragment
        } else if (v == binding.ivSetting) {

            val navController = findNavController(R.id.fragmentContainerView)
            navController.navigate(R.id.setting)
        }
        // navigate to home fragment
        else if (v == binding.ivBag) {

            val navController = findNavController(R.id.fragmentContainerView)
            navController.navigate(R.id.home)
        }
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray,
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        // Check if all permissions are granted
//        val allPermissionsGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
//
//        // Check for request Code  for Camera And Gallery
//        if (requestCode == permissionsRequestCodeForCamera) {
//
//            if (allPermissionsGranted) {
//                // All permissions are granted, show the dialog
//                Utils.T(activity, "Permission granted")
//
//            } else {
//                // Permissions are not granted, handle the scenario
//                showOpenSettingDialog(permissionsRequestCodeForCamera)
//
//
//            }
//        }
//        // Check for request Code  for Bluetooth
//        else if(requestCode == permissionsRequestCodeForBluetooth){
//
//            if (allPermissionsGranted) {
//                // All permissions are granted, show the dialog
//                Utils.T(activity, "Permission granted")
//
//            } else {
//                // Permissions are not granted, handle the scenario
//                showOpenSettingDialog(permissionsRequestCodeForBluetooth)
//
//            }
//
//        }
//    }

//    private fun showEditProfileDialog(){
//
//        val dialog = Dialog(activity)
//        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog.setCanceledOnTouchOutside(true)
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.setContentView(R.layout.item_dialog_profile)
//        dialog.findViewById<MaterialCardView>(R.id.mcvCamera).setOnClickListener {
//
//            //Camera Launcher
////            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
////            cameraActivityResultLauncher.launch(cameraIntent)
//            dialog.dismiss()
//
//        }
//        dialog.findViewById<MaterialCardView>(R.id.mcvGallery).setOnClickListener {
//
//            //Gallery Launcher
////            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
////            galleryActivityResultLauncher?.launch(galleryIntent)
//            dialog.dismiss()
//
//        }
//        dialog.setCancelable(true)
//        dialog.show()
//    }



}