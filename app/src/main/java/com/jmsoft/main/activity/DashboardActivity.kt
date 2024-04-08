package com.jmsoft.main.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.basic.UtilityTools.Constants.Companion.arabic
import com.jmsoft.basic.UtilityTools.Constants.Companion.english
import com.jmsoft.basic.UtilityTools.Constants.Companion.information
import com.jmsoft.basic.UtilityTools.IOnBackPressed
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.databinding.ActivityDashboardBinding

/**
 * Dashboard Activity
 *
 * All the fragments load here
 * here is toolbar
 *
 */
class DashboardActivity : BaseActivity(), View.OnClickListener {

    private val activity: Activity = this@DashboardActivity
    private lateinit var binding: ActivityDashboardBinding
    var navController: NavController? = null
    var mcvSearch:MaterialCardView? = null
    var currentState = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = findNavController(R.id.fragmentContainerView)

        mcvSearch = binding.mcvSearch

        //set the Clicks And initialization
        init()
    }

    //setting the selector on material card view
    private fun setFocusChangeLis(editText: EditText, materialCardView: MaterialCardView) {

        editText.setOnFocusChangeListener { _, hasFocus ->

            if (hasFocus) {
                materialCardView.strokeColor = getColor(R.color.theme)
            } else {
                materialCardView.strokeColor = getColor(R.color.text_hint)
            }
        }
    }

    //set the Clicks And initialization
    private fun init() {

        binding.etSearch?.let { binding.mcvSearch?.let { it1 -> setFocusChangeLis(it, it1) } }

        //For Managing Back press
        this.onBackPressedDispatcher.addCallback(onBackPressedCallback)

        // Set Click on language switcher
        binding.ivLanguageSwitcher?.setOnClickListener(this)

        // Set Click on setting icon for navigating to Setting fragment
        binding.ivSetting?.setOnClickListener(this)

        // set Click on Bag Icon for navigating to Home fragment
        binding.ivLogo?.setOnClickListener(this)

        binding.ivCard?.setOnClickListener(this)
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

            // navController?.navigate(R.id.addToCard)
            if(navController?.currentDestination?.id != R.id.setting) {

                navController?.navigate(R.id.setting)

            }
        }

        // navigate to home fragment
        else if (v == binding.ivLogo) {
            navController?.popBackStack(R.id.home, false)
        }

        // navigate to Card fragment
        else if (v == binding.ivCard) {

            // navController?.navigate(R.id.addToCard)
            if(navController?.currentDestination?.id != R.id.card) {

                navController?.navigate(R.id.card)

            }
        }

    }

    //Managing Back press
    val onBackPressedCallback = object : OnBackPressedCallback(true) {

        override fun handleOnBackPressed() {

            if (navController?.currentDestination?.id == R.id.home) {
                finish()
            }
            else if (navController?.currentDestination?.id == R.id.card){

                if (currentState == information){

                    navController?.popBackStack()
                    navController?.navigate(R.id.card)
                    currentState = ""
                }
                else {

                    navController?.popBackStack()
                    currentState = ""

                }
            }
            else {
                navController?.popBackStack()

            }
        }
    }

}