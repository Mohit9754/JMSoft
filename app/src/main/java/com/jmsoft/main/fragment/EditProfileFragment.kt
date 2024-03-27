package com.jmsoft.main.fragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.jmsoft.R
import com.jmsoft.basic.Database.UserDataModel
import com.jmsoft.basic.UtilityTools.Constants
import com.jmsoft.basic.UtilityTools.Constants.Companion.updateInSession
import com.jmsoft.basic.UtilityTools.Utils
import com.jmsoft.basic.validation.ResultReturn
import com.jmsoft.basic.validation.Validation
import com.jmsoft.basic.validation.ValidationModel
import com.jmsoft.databinding.FragmentEditProfileBinding
import java.util.ArrayList
import com.jmsoft.basic.UtilityTools.Constants.Companion.userId
import com.jmsoft.main.activity.DashboardActivity
import java.util.Locale

class EditProfileFragment : Fragment(), View.OnClickListener {

    lateinit var binding: FragmentEditProfileBinding
    private var errorValidationModels: MutableList<ValidationModel> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment

        binding = FragmentEditProfileBinding.inflate(layoutInflater)

        init()

        return binding.root
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
                materialCardView.strokeColor = requireActivity().getColor(R.color.theme)
            } else {
                materialCardView.strokeColor = requireActivity().getColor(R.color.text_hint)
            }
        }
    }

    // Setting the User Details through the Database
    private fun setUserDetails(userId: Int) {

        val userDataModel = Utils.getUserDetailsThroughUserId(userId)
        binding.etFirstName?.setText(userDataModel.firstName)
        binding.etLastName?.setText(userDataModel.lastName)
        binding.etPhoneNumber?.setText(userDataModel.phoneNumber)
        binding.etEmailAddress?.setText(userDataModel.email)
        binding.etPassword?.setText(userDataModel.password)
    }

    private fun init() {

        // getting the userId
        val userId = arguments?.getInt(userId)

        //if userId is not null and not 0 then we have to update the user details
        if (userId != null && userId != 0) {
            // Setting the user details
            setUserDetails(userId)
        }

        //Setting  Click on Save Button
        binding.mcvSave?.setOnClickListener(this)

        //Removing the Error When text Entered
        setTextChangeLis(binding.etFirstName!!, binding.tvFirstNameError!!)
        setTextChangeLis(binding.etLastName!!, binding.tvLastNameError!!)
        setTextChangeLis(binding.etPhoneNumber!!, binding.tvPhoneNumberError!!)
        setTextChangeLis(binding.etEmailAddress!!, binding.tvEmailAddressError!!)
        setTextChangeLis(binding.etPassword!!, binding.tvPasswordError!!)

        //Setting the Selector on Material Card View When EditText has focus
        setFocusChangeLis(binding.etFirstName!!, binding.mcvFirstName!!)
        setFocusChangeLis(binding.etLastName!!, binding.mcvLastName!!)
        setFocusChangeLis(binding.etPhoneNumber!!, binding.mcvPhoneNumber!!)
        setFocusChangeLis(binding.etEmailAddress!!, binding.mcvEmailAddress!!)
        setFocusChangeLis(binding.etPassword!!, binding.mcvPassword!!)

        //Setting Click on password Visibility icon
        binding.ivPasswordVisibility?.setOnClickListener(this)

        //set Click on Back Btn
        binding.mcvBackBtn?.setOnClickListener(this)

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


        val validation: Validation? = Validation.instance
        val resultReturn: ResultReturn? =
            validation?.CheckValidation(requireActivity(), errorValidationModels)
        if (resultReturn?.aBoolean == true) {

            val userId = arguments?.getInt(userId)

            //if userId is not null and not 0 then we have to update the user details
            if (userId != null && userId != 0) {
                updateUserDetails(userId)
            }
            //Register new user
            else {
                registerNewUser()
            }

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

    // Updating the data in the local database
    private fun updateUserDetails(userId: Int) {

        //this method checks if any user has this phone number
        if (!Utils.isAnyUserHasThisPhoneNumber(
                binding.etPhoneNumber?.text.toString().trim(), userId
            )
        ) {

            //this method checks if any user has this email
            if (!Utils.isAnyUserHasThisEmail(
                    binding.etEmailAddress?.text.toString().trim(),
                    userId
                )
            ) {

                val userDataModel = UserDataModel()

                userDataModel.userId = userId // userId is necessary for updating the details
                userDataModel.firstName = binding.etFirstName?.text.toString().trim()
                userDataModel.lastName = binding.etLastName?.text.toString().trim()
                //Store Email in the lower Case letter
                userDataModel.email = binding.etEmailAddress?.text.toString().trim()
                    .lowercase(Locale.getDefault())
                userDataModel.phoneNumber = binding.etPhoneNumber?.text.toString().trim()

                userDataModel.password = binding.etPassword?.text.toString().trim()

                //Update user details in the User Table
                Utils.updateUserDetails(userDataModel)
                Utils.T(activity, getString(R.string.updated_successfully))

                val updateInSession = arguments?.getBoolean(updateInSession)

                if (updateInSession != null){

                    if (updateInSession){

                        //Updating user details in the session table
                        val userDataModel = Utils.getUserDetailsThroughUserId(userId)
                        Utils.insertDataInSessionTable(userDataModel)
                    }
                }
                (context as DashboardActivity).navController?.popBackStack()


            } else {
                //Showing Email Already Exist Error
                showAlreadyExistError(
                    binding.tvEmailAddressError!!, getString(R.string.email_already_exist)
                )
            }
        } else {
            //Showing Mobile Number Already Exist Error
            showAlreadyExistError(
                binding.tvPhoneNumberError!!, getString(R.string.mobile_number_already_exist)
            )
        }
    }

    // Showing email and phone number already exist error
    fun showAlreadyExistError(textView: TextView, msg: String) {

        textView.visibility = View.VISIBLE
        textView.text = msg
        textView.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.top_to_bottom))
    }

    override fun onClick(v: View?) {

        //when Save button Click
        if (v == binding.mcvSave) {
            validate()
        }
        //show And hide Password
        else if (v == binding.ivPasswordVisibility) {

            showAndHidePassword(binding.etPassword!!, binding.ivPasswordVisibility!!)
        }

        else if(v == binding.mcvBackBtn){

            (requireActivity() as DashboardActivity).navController?.popBackStack()
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

    //Register New User in the User Table
    private fun registerNewUser() {

        //Check if phone Number exist
        if (!Utils.isPhoneNumberExist(binding.etPhoneNumber?.text.toString().trim())) {

            //Checks if Email Already Exist
            if (!Utils.isEmailExist(binding.etEmailAddress?.text.toString().trim())) {

                val userDataModel = UserDataModel()

                userDataModel.userType = Constants.user
                userDataModel.firstName = binding.etFirstName?.text.toString().trim()
                userDataModel.lastName = binding.etLastName?.text.toString().trim()
                //Store Email in the lower Case letter
                userDataModel.email = binding.etEmailAddress?.text.toString().trim()
                    .lowercase(Locale.getDefault())
                userDataModel.phoneNumber = binding.etPhoneNumber?.text.toString().trim()
                userDataModel.password = binding.etPassword?.text.toString().trim()
                userDataModel.profileName = ""

                //insert new user in the User Table
                Utils.insetDataInUserTable(userDataModel)

                //Clearing the data
                binding.etFirstName?.setText("")
                binding.etLastName?.setText("")
                binding.etEmailAddress?.setText("")
                binding.etPhoneNumber?.setText("")
                binding.etPassword?.setText("")

                Utils.T(activity, getString(R.string.new_user_registered_successfully))

                //Navigate to user management
                (requireActivity() as DashboardActivity).navController?.popBackStack()


            } else {
                //Showing Email Already Exist Error
                showAlreadyExistError(
                    binding.tvEmailAddressError!!, getString(R.string.email_already_exist)
                )
            }
        } else {
            //Showing Mobile Number Already Exist Error
            showAlreadyExistError(
                binding.tvPhoneNumberError!!, getString(R.string.mobile_number_already_exist)
            )
        }

    }

}