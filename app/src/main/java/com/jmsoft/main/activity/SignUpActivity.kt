package com.jmsoft.main.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.basic.Database.UserDataHelper
import com.jmsoft.basic.Database.UserDataModel
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Constants.Companion.admin
import com.jmsoft.basic.UtilityTools.Constants.Companion.arabic
import com.jmsoft.basic.UtilityTools.Constants.Companion.email

import com.jmsoft.basic.UtilityTools.Constants.Companion.english
import com.jmsoft.basic.UtilityTools.Constants.Companion.firstName
import com.jmsoft.basic.UtilityTools.Constants.Companion.lastName

import com.jmsoft.basic.UtilityTools.Constants.Companion.password
import com.jmsoft.basic.UtilityTools.Constants.Companion.phoneNumber
import com.jmsoft.basic.UtilityTools.Constants.Companion.user
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.basic.validation.ResultReturn
import com.jmsoft.basic.validation.Validation
import com.jmsoft.basic.validation.ValidationModel
import com.jmsoft.databinding.ActivitySignUpBinding
import java.util.ArrayList

/**
 * SignUp Activity
 *
 *  Validating the signUp details
 *  Store signUp details in the database
 *  intent to login activity
 */

class SignUpActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivitySignUpBinding
    private val activity = this@SignUpActivity
    // for Showing the Error
    private var errorValidationModels: MutableList<ValidationModel> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)

        // Setting the savedInstanceState Data when language switches
        binding.etFirstName!!.setText(savedInstanceState?.getString(firstName) ?: "")
        binding.etLastName!!.setText(savedInstanceState?.getString(lastName) ?: "")
        binding.etPhoneNumber!!.setText(savedInstanceState?.getString(phoneNumber) ?: "")
        binding.etEmailAddress!!.setText(savedInstanceState?.getString(email) ?: "")
        binding.etPassword!!.setText(savedInstanceState?.getString(password) ?: "")

        //set the Clicks And initalize
        init()
        setContentView(binding.root)
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

    private fun init() {

        //Set Image for current Language
        Utils.setImageForCurrentLanguage(binding.ivJewellery!!)
        Utils.toSetPasswordAsLanguage(binding.etPassword)
        Utils.toSetPasswordAsLanguage(binding.etConformPassword)
        //Setting  Click on SignUp Button
        binding.mcvSignUp?.setOnClickListener(activity)

        //Removing the Error When text Entered
        setTextChangeLis(binding.etFirstName!!, binding.tvFirstNameError!!)
        setTextChangeLis(binding.etLastName!!, binding.tvLastNameError!!)
        setTextChangeLis(binding.etPhoneNumber!!, binding.tvPhoneNumberError!!)
        setTextChangeLis(binding.etEmailAddress!!, binding.tvEmailAddressError!!)
        setTextChangeLis(binding.etPassword!!, binding.tvPasswordError!!)
        setTextChangeLis(binding.etConformPassword!!, binding.tvConformPasswordError!!)


       //Setting the Selector on Material Card View When EditText has focus
        setFocusChangeLis(binding.etFirstName!!, binding.mcvFirstName!!)
        setFocusChangeLis(binding.etLastName!!, binding.mcvLastName!!)
        setFocusChangeLis(binding.etPhoneNumber!!, binding.mcvPhoneNumber!!)
        setFocusChangeLis(binding.etEmailAddress!!, binding.mcvEmailAddress!!)
        setFocusChangeLis(binding.etPassword!!, binding.mcvPassword!!)
        setFocusChangeLis(binding.etConformPassword!!, binding.mcvConformPassword!!)

        //Setting Click on Login Button
        binding.llLogin?.setOnClickListener(activity)

        //Setting Click on language Switcher
        binding.ivLanguageSwitcher?.setOnClickListener(activity)

        //Setting Click on password Visibility icon
        binding.ivPasswordVisibility?.setOnClickListener(activity)

        //Setting Click on Conform password Visibility icon
        binding.ivConformPasswordVisibility?.setOnClickListener(activity)

    }

    //Handles All the Clicks
    override fun onClick(v: View?) {

        //when SignUp button Click
        if (v == binding.mcvSignUp) {
            validate()
        }

        // Switching language accourding to current language
        else if (v == binding.ivLanguageSwitcher) {

            val lang = Utils.getCurrentLanguage()

            if (lang == english) {
                Utils.setLocale(activity, arabic)

            } else {
                Utils.setLocale(activity, english)
            }
        } else if (v == binding.llLogin) {

            Utils.I_clear(activity,LoginActivity::class.java,null)
        }

        //show And hide Password
        else if (v == binding.ivPasswordVisibility) {

            showAndHidePassword(binding.etPassword!!, binding.ivPasswordVisibility!!)
        }

        //show And hide Conform Password
        else if (v == binding.ivConformPasswordVisibility) {

            showAndHidePassword(binding.etConformPassword!!, binding.ivConformPasswordVisibility!!)
        }
    }

    // Show And hide , Password And Conform password
    private fun showAndHidePassword(editText: EditText, imageView: ImageView) {

        if (editText.transformationMethod == HideReturnsTransformationMethod.getInstance()) {
            imageView.setImageResource(R.drawable.icon_show)
            editText.transformationMethod = PasswordTransformationMethod.getInstance()

        } else {
            imageView.setImageResource(R.drawable.icon_hide)
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
        }

        editText.setSelection(editText.length())
    }

    // Store the data in the local database
    private fun signUp() {

        //Checks if Phone Number Already Exist
        if (!Utils.isPhoneNumberExist(binding.etPhoneNumber?.text.toString().trim())){

            //Checks if Email Already Exist
            if (!Utils.isEmailExist(binding.etEmailAddress?.text.toString().trim())){

                val userDataModel = UserDataModel()

                //Checks if User Table Empty
                userDataModel.userType = if (Utils.isUserTableEmpty()) admin  else user

                userDataModel.firstName = binding.etFirstName?.text.toString().trim()
                userDataModel.lastName = binding.etLastName?.text.toString().trim()
                //Store Email in the lower Case letter
                userDataModel.email = binding.etEmailAddress?.text.toString().trim().toLowerCase()
                userDataModel.phoneNumber = binding.etPhoneNumber?.text.toString().trim()
                userDataModel.profileName = ""
                userDataModel.token = ""
                userDataModel.password = binding.etPassword?.text.toString().trim()

                //Insert Data in the User Table
                Utils.insetDataInUserTable(userDataModel)
                //Intent to Login Activity
                Utils.T(activity, getString(R.string.signup_successfully))

                Utils.I_clear(activity,LoginActivity::class.java,null)

            }
            else {
                //Showing Email Already Exist Error
                showAlreadyExistError(binding.tvEmailAddressError!!,
                    getString(R.string.email_already_exist))
            }
        }
        else {
            //Showing Mobile Number Already Exist Error
            showAlreadyExistError(binding.tvPhoneNumberError!!,
                getString(R.string.mobile_number_already_exist))
        }
    }
    // Showing email and phone number already exist error
    fun showAlreadyExistError(textView: TextView,msg:String){

        textView.visibility  = View.VISIBLE
        textView.text  = msg
        textView.startAnimation(AnimationUtils.loadAnimation(activity,R.anim.top_to_bottom))
    }

    //Validating Sign Up details
    private fun validate() {

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
                Validation.Type.Email, binding.etEmailAddress, binding.tvEmailAddressError
            )
        )

        errorValidationModels.add(
            ValidationModel(
                Validation.Type.PasswordStrong, binding.etPassword, binding.tvPasswordError
            )
        )

        errorValidationModels.add(
            ValidationModel(
                Validation.Type.PasswordMatch,
                binding.etPassword,
                binding.etConformPassword,
                binding.tvConformPasswordError
            )
        )

        val validation: Validation? = Validation.instance
        val resultReturn: ResultReturn? =
            validation?.CheckValidation(activity, errorValidationModels)
        if (resultReturn?.aBoolean == true) {
            signUp()

        } else {
            resultReturn?.errorTextView?.visibility = View.VISIBLE
            if (resultReturn?.type === Validation.Type.EmptyString) {
                resultReturn.errorTextView?.text = resultReturn.errorMessage
            } else {
                resultReturn?.errorTextView?.text = validation?.errorMessage
                val animation =
                    AnimationUtils.loadAnimation(applicationContext, R.anim.top_to_bottom)
                resultReturn?.errorTextView?.startAnimation(animation)
//                validation?.EditTextPointer?.requestFocus()

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(validation?.EditTextPointer, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    // For recreating the activity data store
    override fun onSaveInstanceState(outState: Bundle) {

        outState.putString(firstName, binding.etFirstName?.text.toString())
        outState.putString(lastName, binding.etLastName?.text.toString())
        outState.putString(phoneNumber, binding.etPhoneNumber?.text.toString())
        outState.putString(email, binding.etEmailAddress?.text.toString())
        outState.putString(password, binding.etPassword?.text.toString())

        super.onSaveInstanceState(outState)
    }

    // Close the Keyboard when you touch the Screen
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {

        if (currentFocus != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

}