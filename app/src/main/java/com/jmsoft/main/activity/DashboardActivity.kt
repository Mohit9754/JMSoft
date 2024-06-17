package com.jmsoft.main.activity

import android.app.Activity
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.Utility.UtilityTools.GetProgressBar
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Constants.Companion.appLang
import com.jmsoft.basic.UtilityTools.Constants.Companion.arabic
import com.jmsoft.basic.UtilityTools.Constants.Companion.english
import com.jmsoft.basic.UtilityTools.Constants.Companion.information
import com.jmsoft.basic.UtilityTools.Constants.Companion.lang
import com.jmsoft.basic.UtilityTools.Constants.Companion.verification
import com.jmsoft.basic.UtilityTools.KeyboardUtils.hideKeyboard
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

    var binding: ActivityDashboardBinding? = null
    var navController: NavController? = null

    // flag variable for back press management of cart fragment
    var currentState = ""

    var mcvSearch:MaterialCardView? = null
    var etSearch:EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        mcvSearch = binding?.mcvSearch
        etSearch = binding?.etSearch

        navController = findNavController(R.id.fragmentContainerView)

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

        // Set app language
        Utils.setAppLanguage(activity)

        binding?.etSearch?.let { binding?.mcvSearch?.let { it1 -> setFocusChangeLis(it, it1) } }

        //For Managing Back press
        this.onBackPressedDispatcher.addCallback(onBackPressedCallback)

        // Set Click on language switcher
        binding?.ivLanguageSwitcher?.setOnClickListener(this)

        // Set Click on setting icon for navigating to Setting fragment
        binding?.ivSetting?.setOnClickListener(this)

        // set Click on Bag Icon for navigating to Home fragment
        binding?.ivLogo?.setOnClickListener(this)

        binding?.ivCard?.setOnClickListener(this)

        binding?.toolbar?.setOnClickListener(this)

    }

    //Handles All the Clicks
    override fun onClick(v: View?) {

        // Switching language according to current language
        if (v == binding?.ivLanguageSwitcher) {

            val lang = Utils.getCurrentLanguage()

            if (lang == english) {
                Utils.setLocale(activity, arabic)
//                activity.recreate()
            } else {
                Utils.setLocale(activity, english)
//                Utils.storeLang(activity,english)
//                activity.recreate()
            }

        // navigate to Setting fragment
        } else if (v == binding?.ivSetting) {

            // navController?.navigate(R.id.addToCard)
            if (navController?.currentDestination?.id != R.id.setting) {

                navController?.navigate(R.id.setting)

            }
        }

        // navigate to home fragment
        else if (v == binding?.ivLogo) {
            navController?.popBackStack(R.id.home, false)
        }

        // navigate to Card fragment
        else if (v == binding?.ivCard) {

            // navController?.navigate(R.id.addToCard)
            if(navController?.currentDestination?.id != R.id.cart) {

                navController?.navigate(R.id.cart)

            }
        }

        else if (v == binding?.toolbar){

            hideKeyboard(this)
        }
    }

    //Dispatch Touch Event
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            // remove focus from edit text on click outside
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    //Managing Back press
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {

        override fun handleOnBackPressed() {

            if (navController?.currentDestination?.id == R.id.home) {
                finish()
            }
            else if (navController?.currentDestination?.id == R.id.cart){

                if (currentState == information){

                    navController?.popBackStack()
                    navController?.navigate(R.id.cart)
                }
                else {
                    navController?.popBackStack()
                }

                currentState = verification
            }
            else {

                GetProgressBar.getInstance(activity)?.show()

                navController?.popBackStack()

            }
        }
    }
}