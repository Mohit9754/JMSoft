package com.jmsoft.main.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.Utility.Database.CategoryDataModel
import com.jmsoft.basic.Database.UserDataHelper
import com.jmsoft.basic.UtilityTools.Constants.Companion.arabic
import com.jmsoft.basic.UtilityTools.Constants.Companion.email
import com.jmsoft.basic.UtilityTools.Constants.Companion.english
import com.jmsoft.basic.UtilityTools.Constants.Companion.password
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.basic.validation.ResultReturn
import com.jmsoft.basic.validation.Validation
import com.jmsoft.basic.validation.ValidationModel
import com.jmsoft.databinding.ActivityLoginBinding

/**
 * Login Activity
 *
 *  Validating the login details
 *  Store the login response to local database
 */
class LoginActivity : BaseActivity(), View.OnClickListener {

    private val activity: Activity = this@LoginActivity

    // For Showing the error
    private var errorValidationModels: MutableList<ValidationModel> = ArrayList()
    private lateinit var binding: ActivityLoginBinding
    private var isPasswordVisible = false

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        // Setting the savedInstanceState Data when language switches
//        binding.etEmailAddress?.setText(savedInstanceState?.getString(email) ?: "")
//        binding.etPassword?.setText(savedInstanceState?.getString(password) ?: "")

        setContentView(binding.root)

        //set the Clicks And initialization
        init()

        val categoryDataModel = CategoryDataModel()
        categoryDataModel.categoryUUID = Utils.generateUUId()
        categoryDataModel.categoryName = "Ring"


        val categoryDataModel2 = CategoryDataModel()
        categoryDataModel2.categoryUUID = Utils.generateUUId()
        categoryDataModel2.categoryName = "necklace"


        val categoryDataModel3 = CategoryDataModel()
        categoryDataModel3.categoryUUID = Utils.generateUUId()
        categoryDataModel3.categoryName = "earrings"


        Utils.insertCategoryInCategoryTable(categoryDataModel)
        Utils.insertCategoryInCategoryTable(categoryDataModel2)
        Utils.insertCategoryInCategoryTable(categoryDataModel3)


        val instance = UserDataHelper.instance

        instance.insertProduct("Ring","Royal Gold Ring",12000)
        instance.insertProduct("necklace","Royal Gold necklace",24000)
        instance.insertProduct("earrings","Royal Gold earrings",30000)
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

    // Removing Error when text entered
    private fun setTextChangeLis(editText: EditText, textView: TextView) {

        editText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (s.toString().isNotEmpty()) {
                    textView.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {}

        })
    }

    private fun init() {

        //Set Image for current Language
        binding.ivJewellery?.let { Utils.setImageForCurrentLanguage(it) }

        // Setting the Selector on Material Card View when edittext has focus
        binding.ivJewellery?.setImageDrawable(null)
        binding.ivJewellery?.setImageResource(R.drawable.img_jewellery)

        binding.etEmailAddress?.let { binding.mcvEmailAddress?.let { it1 ->
            setFocusChangeLis(it,
                it1
            )
        } }
        binding.etPassword?.let { binding.mcvPassword?.let { it1 -> setFocusChangeLis(it, it1) } }

        //set Password for  current Language
        Utils.toSetPasswordAsLanguage(binding.etPassword,activity)

        // Removing the error when text Entered
        binding.etEmailAddress?.let { binding.tvEmailAddressError?.let { it1 ->
            setTextChangeLis(it,
                it1
            )
        } }

        binding.etPassword?.let { binding.tvPasswordError?.let { it1 -> setTextChangeLis(it, it1) } }

        //Set Click on login Button
        binding.mcvLogin?.setOnClickListener(this)

        //Set Click on language Switcher
        binding.ivLanguageSwitcher?.setOnClickListener(this)

        //Set Click on SignUp textView
        binding.llSignUp?.setOnClickListener(this)

        //Set Click on password Visibility icon
        binding.ivPasswordVisibility?.setOnClickListener(this)

    }

    //Validating Login details
    private fun validate() {

        errorValidationModels.clear()
        errorValidationModels.add(
            ValidationModel(
                Validation.Type.Email,
                binding.etEmailAddress,
                binding.tvEmailAddressError
            )
        )
        errorValidationModels.add(
            ValidationModel(
                Validation.Type.Empty,
                binding.etPassword,
                binding.tvPasswordError
            )
        )

        val validation: Validation? = Validation.instance
        val resultReturn: ResultReturn? =
            validation?.CheckValidation(activity, errorValidationModels)
        if (resultReturn?.aBoolean == true) {

            //Check if user is valid
            if (Utils.isValidUser(
                    binding.etEmailAddress?.text.toString().trim().lowercase(),
                    binding.etPassword?.text.toString().trim()
                )
            ) {

                // Getting User Details through email and password
                val userDataModel = Utils.getUserThroughEmailAndPassword(
                    binding.etEmailAddress?.text.toString().trim().lowercase(),
                    binding.etPassword?.text.toString().trim()
                )
                //Store User Details in the Session Table
                Utils.insertDataInSessionTable(userDataModel)

                //Intent to Dashboard Activity
                Utils.I_clear(activity, DashboardActivity::class.java, null)
            } else {
                // Invalid user
                Utils.T(activity, getString(R.string.invalid_credentials))
            }

        } else {
            resultReturn?.errorTextView?.visibility = View.VISIBLE
            if (resultReturn?.type === Validation.Type.EmptyString) {
                resultReturn.errorTextView?.text = resultReturn.errorMessage
            } else {
                resultReturn?.errorTextView?.text = validation?.errorMessage
                val animation =
                    AnimationUtils.loadAnimation(applicationContext, R.anim.top_to_bottom)
                resultReturn?.errorTextView?.startAnimation(animation)
                validation?.EditTextPointer?.requestFocus()

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(validation?.EditTextPointer, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    //Handles All the Clicks
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onClick(v: View?) {

        //When login button Click
        if (v == binding.mcvLogin) {
            validate()
        }

        // Switching language according to current language
        else if (v == binding.ivLanguageSwitcher) {

            val lang = Utils.getCurrentLanguage()

            if (lang == english) {
                Utils.setLocale(activity, arabic)

            } else {
                Utils.setLocale(activity, english)
            }
        } else if (v == binding.llSignUp) {
            Utils.I(activity, SignUpActivity::class.java, null)
        }

        //Show or Hide password
        else if (v == binding.ivPasswordVisibility) {

            if (!isPasswordVisible) {
                isPasswordVisible = true
                binding.ivPasswordVisibility?.setImageResource(R.drawable.icon_hide)
                binding.etPassword?.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                binding.etPassword?.setSelection(binding.etPassword!!.length())

            } else {
                isPasswordVisible = false
                binding.ivPasswordVisibility?.setImageResource(R.drawable.icon_show)
                binding.etPassword?.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                binding.etPassword?.setSelection(binding.etPassword!!.length())
            }

        }
    }

    // For recreating the activity data store
    override fun onSaveInstanceState(outState: Bundle) {

        outState.putString(email, binding.etEmailAddress?.text.toString())
        outState.putString(password, binding.etPassword?.text.toString())
        super.onSaveInstanceState(outState)
    }



}