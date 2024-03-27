package com.jmsoft.main.activity

import android.app.Activity
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
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
    var navController:NavController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = findNavController(R.id.fragmentContainerView)

        //set the Clicks And initialization
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

        // Switching language according to current language
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
            navController?.navigate(R.id.setting)
        }

        // navigate to home fragment
        else if (v == binding.ivBag) {
            navController?.navigate(R.id.home)
        }
    }


    // Close the Keyboard when you touch the Screen
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {

        if (currentFocus != null) {
            val imm = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }


}